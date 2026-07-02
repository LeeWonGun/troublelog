import { useEffect, useRef, useState } from 'react'
import { APP } from '../../constants/actionTypes.js'
import { getTeamByCode, joinTeam } from '../../api/teamApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function JoinTeamModal({ state, dispatch }) {
    const teamCodeRef = useRef(null)
    const [teamCodeError, setTeamCodeError] = useState(false)
    const [teamCodeErrorMsg, setTeamCodeErrorMsg] = useState('')
    const [verifiedTeam, setVerifiedTeam] = useState(null)

    const close = () => dispatch({ type: APP.CLOSE_MODAL })

    // JOIN_TEAM 모달이 오픈될 때마다 이전 검증 상태를 초기화
    useEffect(() => {
        setVerifiedTeam(null)
        setTeamCodeError(false)
        setTeamCodeErrorMsg('')
    }, [state.modal])

    // 백엔드가 이미 가입된 팀에 대해서는 role을 채워서 응답하므로, role 존재 여부로 기가입 여부를 판단
    const alreadyJoined = !!verifiedTeam?.role

    async function verifyTeamCodeFn() {
        const teamCode = teamCodeRef.current?.value?.trim()
        if (!teamCode || teamCode.length > 50) {
            setTeamCodeError(true)
            setTeamCodeErrorMsg('팀 코드를 입력하세요.')
            setVerifiedTeam(null)
            return
        }
        setTeamCodeError(false)
        setTeamCodeErrorMsg('')

        requestHandler(() => getTeamByCode(teamCode), {
            onSuccess: (data) => setVerifiedTeam(data),
            onFail: (message) => {
                setVerifiedTeam(null)
                setTeamCodeError(true)
                setTeamCodeErrorMsg(message)
            },
        })
    }

    async function joinTeamFn() {
        if (!verifiedTeam || verifiedTeam.role) return

        requestHandler(() => joinTeam(verifiedTeam.teamCode), {
            onSuccess: (data) => {
                dispatch({ type: APP.JOIN_TEAM, payload: data })
                close()
            },
            onFail: (message) => {
                setVerifiedTeam(null)
                setTeamCodeError(true)
                setTeamCodeErrorMsg(message)
            },
            showGlobalError: true,
        })
    }

    return (
        <ModalOverlay onClose={close}>
            <h3>팀 참여</h3>

            {!verifiedTeam ? (
                // 1단계: 팀 코드 입력 후 유효성 확인
                <>
                    <p className="modal-subtitle">팀장에게 공유받은 코드를 입력하여 팀에 참가하세요.</p>
                    <div className="form-group form-group--mt">
                        <input
                            className="input text-mono"
                            placeholder="팀 코드 입력"
                            ref={teamCodeRef}
                            maxLength={50}
                            onChange={() => { setTeamCodeError(false); setTeamCodeErrorMsg('') }}
                        />
                        {teamCodeError && <div className="error-msg">{teamCodeErrorMsg || '팀 코드를 확인해 주세요.'}</div>}
                    </div>
                    <div className="modal-actions">
                        <button className="btn btn-ghost" onClick={close}>취소</button>
                        <button className="btn btn-primary" onClick={verifyTeamCodeFn}>확인</button>
                    </div>
                </>
            ) : (
                // 2단계: 검증된 팀 정보를 보여주고 최종 참여 여부를 확인받는다.
                <>
                    <div className="team-info-box form-group--mt">
                        <div className="form-group">
                            <div className="field-label">팀 이름</div>
                            <div className="team-code-value">{verifiedTeam.name}</div>
                        </div>
                        {verifiedTeam.description &&
                            <div className="form-group">
                                <div className="field-label">팀 설명</div>
                                <p className="modal-subtitle">{verifiedTeam.description}</p>
                            </div>
                        }
                    </div>
                    <p className="join-confirm-text">팀에 참여하시겠습니까?</p>
                    {alreadyJoined && <div className="error-msg">이미 참여 중인 팀입니다.</div>}
                    {!alreadyJoined && teamCodeError && <div className="error-msg">{teamCodeErrorMsg}</div>}
                    <div className="modal-actions">
                        <button className="btn btn-ghost" onClick={() => setVerifiedTeam(null)}>다시 입력</button>
                        <button className="btn btn-primary" onClick={joinTeamFn} disabled={alreadyJoined}>참여</button>
                    </div>
                </>
            )}
        </ModalOverlay>
    )
}

export default JoinTeamModal