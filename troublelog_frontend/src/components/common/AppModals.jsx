import { useAppContext } from '../../context/AppContext.jsx'
import { MODAL_COMPONENTS } from '../modals/modalRegistry.js'

// 전역 모달 디스패처.
// 현재 열려 있는 모달 타입(state.modal)에 맞는 컴포넌트를 레지스트리에서 찾아 렌더링만 한다.
// 각 모달의 상태/로직은 components/modals/ 아래 개별 컴포넌트에 캡슐화되어 있음.
function AppModals() {
  const { state, dispatch } = useAppContext()

  if (!state.modal) return null

  const ModalComponent = MODAL_COMPONENTS[state.modal]
  if (!ModalComponent) return null

  return <ModalComponent state={state} dispatch={dispatch} />
}

export default AppModals