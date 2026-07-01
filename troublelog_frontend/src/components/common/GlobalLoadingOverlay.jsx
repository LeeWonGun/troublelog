import { useAppContext } from '../../context/AppContext.jsx'
import '../../styles/loading-overlay.css'

function GlobalLoadingOverlay() {
  const { state } = useAppContext()

  if (!state.globalLoading) return null

  return (
    <div className="loading-overlay" aria-live="polite" aria-label="로딩 중">
      <div className="loading-spinner">
        <span className="spinner-ring" />
        <p className="spinner-text">로딩 중...</p>
      </div>
    </div>
  )
}

export default GlobalLoadingOverlay