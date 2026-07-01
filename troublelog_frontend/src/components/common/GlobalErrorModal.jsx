import { useAppContext } from '../../context/AppContext.jsx'
import { APP } from '../../constants/actionTypes.js'
import '../../styles/error-modal.css'

// 전역 에러 모달
// requestHandler(showGlobalError: true)로 알려진 실패 메시지를 화면 최상단에 표시한다.
// state.modal(팀 생성/참여 등 폼 모달)과는 별개의 state.errorModal을 사용하므로
// 다른 모달이 열려 있는 상태 위에도 겹쳐서 뜰 수 있다.
function GlobalErrorModal() {
  const { state, dispatch } = useAppContext()

  if (!state.errorModal) return null

  const close = () => dispatch({ type: APP.HIDE_ERROR })

  function handleBackdrop(e) {
    if (e.target === e.currentTarget) close()
  }

  return (
    <div className="error-modal-overlay" onClick={handleBackdrop}>
      <div className="error-modal">
        <div className="error-modal-icon">!</div>
        <h3>요청을 처리하지 못했습니다</h3>
        <p className="error-modal-message">{state.errorModal}</p>
        <div className="modal-actions">
          <button className="btn btn-primary" onClick={close}>확인</button>
        </div>
      </div>
    </div>
  )
}

export default GlobalErrorModal