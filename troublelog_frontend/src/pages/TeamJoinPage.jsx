import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'

function TeamJoinPage() {
  const navigate = useNavigate()
  const { dispatch } = useAppContext()

  useEffect(() => {
    dispatch({ type: 'OPEN_MODAL', payload: { modal: 'join-team' } })
    navigate('/', { replace: true })
  }, [])

  return null
}

export default TeamJoinPage