import { useEffect, useRef, useState } from 'react'
import { APP } from '../../constants/actionTypes.js'
import { MODAL } from '../../constants/modalTypes.js'
import { getTeamMembers } from '../../api/teamApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import { formatDateTime } from '../../util/dateUtil.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function TeamManageModal({ state, dispatch }) {
  const { modalTeamTarget, teams, nickname } = state
  const targetTeam = teams.find(t => t.teamId === modalTeamTarget)

  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  const [copied, setCopied] = useState(false)
  const copyTimeoutRef = useRef(null)

  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  useEffect(() => {
    return () => {
      if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
    }
  }, [])

  useEffect(() => {
    if (!modalTeamTarget) {
      setLoading(false)
      setLoadError('팀 정보를 찾을 수 없습니다.')
      return
    }

    let cancelled = false // 언마운트/팀 전환 이후 늦게 도착한 응답이 상태를 덮어쓰지 않도록 하는 가드
    setLoading(true)
    setLoadError('')

    requestHandler(() => getTeamMembers(modalTeamTarget), {
      isCancelled: () => cancelled,
      onSuccess: (data) => {
        setMembers(data)
        setLoading(false)
      },
      onFail: (message) => {
        setLoadError(message)
        setLoading(false)
      },
      fallbackMessage: '팀원 목록을 불러오지 못했습니다.',
    })

    return () => { cancelled = true }
  }, [modalTeamTarget])

  async function copyToClipboard(text) {
    if (!text) return
    try {
      await navigator.clipboard?.writeText(text)
      setCopied(true)
      if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
      copyTimeoutRef.current = setTimeout(() => setCopied(false), 1500)
    } catch (e) {
      console.warn('[TeamManageModal] 클립보드 복사 실패:', e)
    }
  }

  return (
    <ModalOverlay onClose={close}>
      <h3>{targetTeam?.name ?? '팀'}</h3>
      <div className="field-label modal-team-code-label">
        팀 코드: <span className="team-code-value">{targetTeam?.teamCode ?? '-'}</span>{' '}
        <button className="text-link" onClick={() => copyToClipboard(targetTeam?.teamCode)}>
          {copied ? '복사됨' : '복사'}
        </button>
      </div>

      <div className="field-label">팀원{!loading && !loadError ? ` (${members.length})` : ''}</div>

      {loading && <div className="modal-subtitle">팀원 목록을 불러오는 중입니다...</div>}
      {!loading && loadError && <div className="error-msg">{loadError}</div>}

      {!loading && !loadError && (
        <div className="modal-member-list">
          {members.map(m => (
            <div key={m.userId} className="member-row">
              <span className="mname">
                {m.nickname}
                <span className={`role-tag ${m.role === 'LEADER' ? 'leader' : 'member'}`}>
                  {m.role === 'LEADER' ? '팀장' : '팀원'}
                </span>
              </span>

              <span className="mmeta">
                {formatDateTime(m.joinedAt).slice(0, 10)} 가입{m.nickname === nickname ? ' (나)' : ''}
              </span>
            </div>
          ))}
        </div>
      )}

      <div className="modal-actions modal-actions--spread">
        <button
          className="btn btn-danger"
          onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.DELETE_CONFIRM, teamId: modalTeamTarget } })}
        >
          팀 삭제
        </button>
        <button className="btn btn-primary" onClick={close}>확인</button>
      </div>
    </ModalOverlay>
  )
}

export default TeamManageModal