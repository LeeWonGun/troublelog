import { useEffect, useRef, useState } from 'react'
import { APP } from '../../constants/actionTypes.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function CreateTeamSuccessModal({ state, dispatch }) {
    const [copied, setCopied] = useState(false)
    const copyTimeoutRef = useRef(null)

    const close = () => dispatch({ type: APP.CLOSE_MODAL })

    // 언마운트 시 타이머 정리 (setState on unmounted component 방지)
    useEffect(() => {
        return () => {
            if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
        }
    }, [])

    async function copyToClipboard(text) {
        try {
            await navigator.clipboard?.writeText(text)
            setCopied(true)
            if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
            copyTimeoutRef.current = setTimeout(() => setCopied(false), 1500)
        } catch (e) {
            console.warn('[CreateTeamSuccessModal] 클립보드 복사 실패:', e)
        }
    }

    return (
        <ModalOverlay onClose={close}>
            <div className="modal-success-header">
                <div className="modal-success-icon">&#10003;</div>
                <div className="modal-success-text">
                    <h3>팀이 성공적으로 생성되었습니다</h3>
                    <p className="modal-subtitle">아래 코드를 공유해서 팀원을 초대하세요.</p>
                </div>
            </div>
            <div className="code-display">{state.createdTeamCode}</div>
            <div className="modal-actions modal-actions--spread">
                <div className="copy-action-group">
                    <button className="btn btn-ghost" onClick={() => copyToClipboard(state.createdTeamCode)}>복사</button>
                    {copied && <span className="copy-feedback" role="status">복사되었습니다</span>}
                </div>
                <button className="btn btn-primary" onClick={close}>확인</button>
            </div>
        </ModalOverlay>
    )
}

export default CreateTeamSuccessModal