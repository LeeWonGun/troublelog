import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'

// 팀 생성은 모달로 처리하므로 이 페이지는 모달을 즉시 열고 홈으로 리다이렉트
import { useEffect } from 'react'

function TeamCreatePage() {
  const navigate = useNavigate()
  const { dispatch } = useAppContext()

  useEffect(() => {
    dispatch({ type: 'OPEN_MODAL', payload: { modal: 'create-team' } })
    navigate('/', { replace: true })
  }, [])

  return null
}

export default TeamCreatePage