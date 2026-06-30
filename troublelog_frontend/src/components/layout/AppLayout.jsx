import { Outlet } from 'react-router-dom'
import Sidebar from '../common/Sidebar.jsx'
import AppModals from '../common/AppModals.jsx'
import GlobalLoadingOverlay from '../common/GlobalLoadingOverlay.jsx'

function AppLayout() {
  return (
    <div className="app">
      <Sidebar />
      <Outlet />
      {/* 모든 모달은 전역 상태(AppContext.modal)에 따라 AppModals에서 렌더링 */}
      <AppModals />
      {/* axios 요청 진행 중 전체 화면 오버레이 */}
      <GlobalLoadingOverlay />
    </div>
  )
}

export default AppLayout