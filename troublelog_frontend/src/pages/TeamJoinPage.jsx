import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { APP } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'

function TeamJoinPage() {
  const navigate = useNavigate()
  const { dispatch } = useAppContext()

  useEffect(() => {
    dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.JOIN_TEAM } })
    navigate('/', { replace: true })
  }, [])

  return null
}

export default TeamJoinPage