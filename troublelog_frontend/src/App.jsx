import GlobalLoadingOverlay from './components/common/GlobalLoadingOverlay.jsx'
import GlobalErrorModal from './components/common/GlobalErrorModal.jsx'
import ApiRouter from './routes/ApiRouter.jsx'

function App() {
  return (
    <>
      <ApiRouter />
      <GlobalLoadingOverlay />
      <GlobalErrorModal />
    </>
  )
}

export default App