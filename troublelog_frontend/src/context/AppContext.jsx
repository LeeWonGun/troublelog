import { createContext, useContext, useEffect, useReducer } from 'react'
import appReducer, { initialState } from '../reducers/appReducer.js'
import { APP } from '../constants/actionTypes.js'
import { getMe } from '../api/authApi.js'
import { getMyTeams } from '../api/teamApi.js'
import { registerLoadingCallback } from '../api/axiosInstance.js'


// 전역 상태 (인증, 팀, 모달, 검색필터)

const AppContext = createContext(null)

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState)

  // axios 요청 카운트 변화 시 전역 로딩 상태 동기화
  useEffect(() => {
    registerLoadingCallback((isLoading) => {
      dispatch({ type: APP.SET_GLOBAL_LOADING, payload: isLoading })
    })
  }, [])
  
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
        console.log("user init fail!");
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