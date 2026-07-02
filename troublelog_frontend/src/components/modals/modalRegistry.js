import { MODAL } from '../../constants/modalTypes.js'
import CreateTeamModal from './CreateTeamModal.jsx'
import CreateTeamSuccessModal from './CreateTeamSuccessModal.jsx'
import JoinTeamModal from './JoinTeamModal.jsx'
import TeamManageModal from './TeamManageModal.jsx'
import DeleteConfirmModal from './DeleteConfirmModal.jsx'
import LeaveConfirmModal from './LeaveConfirmModal.jsx'
import EditProfileModal from './EditProfileModal.jsx'
import SearchDetailModal from './SearchDetailModal.jsx'

// 모달 타입 -> 컴포넌트 매핑 (팩토리 역할).
// 새 모달을 추가할 때는 컴포넌트 파일을 만들고 이 객체에 한 줄만 추가하면 됨.
export const MODAL_COMPONENTS = {
  [MODAL.CREATE_TEAM]: CreateTeamModal,
  [MODAL.CREATE_TEAM_SUCCESS]: CreateTeamSuccessModal,
  [MODAL.JOIN_TEAM]: JoinTeamModal,
  [MODAL.TEAM_MANAGE]: TeamManageModal,
  [MODAL.DELETE_CONFIRM]: DeleteConfirmModal,
  [MODAL.LEAVE_CONFIRM]: LeaveConfirmModal,
  [MODAL.EDIT_PROFILE]: EditProfileModal,
  [MODAL.SEARCH_DETAIL]: SearchDetailModal,
}