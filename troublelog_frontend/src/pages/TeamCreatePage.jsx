import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { APP } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'

// 팀 생성은 모달로 처리하므로 이 페이지는 모달을 즉시 열고 홈으로 리다이렉트
function TeamCreatePage() {
  const navigate = useNavigate()
  const { dispatch } = useAppContext()

  useEffect(() => {
    dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.CREATE_TEAM } })
    navigate('/', { replace: true })
  }, [])

  return null
}

export default TeamCreatePage