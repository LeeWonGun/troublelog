import { createContext, useContext, useEffect, useReducer } from 'react'
import appReducer, { initialState } from '../reducers/appReducer.js'
import { APP } from '../constants/actionTypes.js'
import { getMe } from '../api/authApi.js'
import { getMyTeams } from '../api/teamApi.js'


// 전역 상태 (인증, 팀, 모달, 검색필터)

const AppContext = createContext(null)

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState)

  useEffect(() => {
    const initUser = async () => {
      try {
        const [meRes, teamsRes] = await Promise.all([
          getMe(),
          getMyTeams(),
        ])
        dispatch({ type: APP.SET_USER,  payload: meRes.data })
        dispatch({ type: APP.SET_TEAMS, payload: teamsRes.data })
      } catch {
        // 401이면 axiosInstance 인터셉터에서 /login 리다이렉트 처리
        dispatch({ type: APP.SET_LOADING, payload: false })
      }
    }

    initUser()
  }, [])

  return (
    <AppContext.Provider value={{ state, dispatch }}>
      {children}
    </AppContext.Provider>
  )
}

// 커스텀 훅 - AppProvider 외부 사용 시 오류 발생
export function useAppContext() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useAppContext must be used within AppProvider')
  return ctx
}