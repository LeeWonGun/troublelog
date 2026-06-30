import { Outlet } from 'react-router-dom'
import Sidebar from '../common/Sidebar.jsx'
import AppModals from '../common/AppModals.jsx'

function AppLayout() {
  return (
    <div className="app">
      <Sidebar />
      <Outlet />
      {/* 모든 모달은 전역 상태(AppContext.modal)에 따라 AppModals에서 렌더링 */}
      <AppModals />
    </div>
  )
}

export default AppLayout