import GlobalLoadingOverlay from './components/common/GlobalLoadingOverlay.jsx'
import ApiRouter from './routes/ApiRouter.jsx'

function App() {
  return (
    <>
      <ApiRouter />
      <GlobalLoadingOverlay />
    </>
  )
}

export default App