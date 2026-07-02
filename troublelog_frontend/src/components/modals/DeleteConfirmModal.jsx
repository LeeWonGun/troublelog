import { APP } from '../../constants/actionTypes.js'
import { MODAL } from '../../constants/modalTypes.js'
import { deleteTeam } from '../../api/teamApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function DeleteConfirmModal({ state, dispatch }) {
  const { modalTeamTarget } = state
  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  async function deleteTeamFn() {
    requestHandler(() => deleteTeam(modalTeamTarget), {
      onSuccess: () => {
        dispatch({ type: APP.REMOVE_TEAM, payload: modalTeamTarget })
        close()
      },
      onFail: (message) => console.warn('[DeleteConfirmModal] 팀 삭제 실패:', message),
      showGlobalError: true,
    })
  }

  return (
    <ModalOverlay onClose={close}>
      <h3>팀 삭제</h3>
      <p className="danger-text">
        정말 이 팀을 삭제하시겠습니까?<br />
        삭제 시 모든 팀원이 게시판에 접근할 수 없으며 복구할 수 없습니다.
      </p>
      <div className="modal-actions">
        <button className="btn btn-ghost" onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.TEAM_MANAGE } })}>
          취소
        </button>
        <button className="btn-danger-filled btn" onClick={deleteTeamFn}>
          삭제
        </button>
      </div>
    </ModalOverlay>
  )
}

export default DeleteConfirmModal