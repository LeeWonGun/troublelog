import { APP } from '../../constants/actionTypes.js'
import { leaveTeam } from '../../api/teamApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function LeaveConfirmModal({ state, dispatch }) {
  const { modalTeamTarget } = state
  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  async function leaveTeamFn() {
    requestHandler(() => leaveTeam(modalTeamTarget), {
      onSuccess: () => {
        dispatch({ type: APP.LEAVE_TEAM, payload: modalTeamTarget })
        close()
      },
      onFail: (message) => console.warn('[LeaveConfirmModal] 팀 탈퇴 실패:', message),
      showGlobalError: true,
    })
  }

  return (
    <ModalOverlay onClose={close}>
      <h3>팀 탈퇴</h3>
      <p className="danger-text">
        이 팀에서 탈퇴하시겠습니까?<br />
        탈퇴 시 팀 게시판에 접근할 수 없습니다.
      </p>
      <div className="modal-actions">
        <button className="btn btn-ghost" onClick={close}>취소</button>
        <button className="btn-danger-filled btn" onClick={leaveTeamFn}>
          탈퇴
        </button>
      </div>
    </ModalOverlay>
  )
}

export default LeaveConfirmModal