import { createContext, useContext, useEffect, useReducer } from 'react'
import appReducer, { initialState } from '../reducers/appReducer.js'
import { APP } from '../constants/actionTypes.js'
import { AUTH_PAGE_PATHS } from '../constants/routePaths.js'
import { getMe } from '../api/authApi.js'
import { getMyTeams } from '../api/teamApi.js'
import { registerLoadingCallback } from '../api/axiosInstance.js'
import { requestHandler } from '../util/requestHandler.js'
import { useLocation } from 'react-router-dom'


// 전역 상태 (인증, 팀, 모달, 검색필터)

const AppContext = createContext(null)

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState)
  const location = useLocation()

  const isOnAuthPage = AUTH_PAGE_PATHS.includes(location.pathname)

  // axios 요청 카운트 변화 시 전역 로딩 상태 동기화
  useEffect(() => {
    registerLoadingCallback((isLoading) => {
      dispatch({ type: APP.SET_GLOBAL_LOADING, payload: isLoading })
    })
  }, [])

  useEffect(() => {
    if (isOnAuthPage) return

    requestHandler(getMe, {
      onSuccess: (data) => {
        dispatch({ type: APP.SET_USER, payload: data })

        requestHandler(getMyTeams, {
          onSuccess: (teams) => dispatch({ type: APP.SET_TEAMS, payload: teams }),
          onFail: (message) => console.warn('[AppContext] 팀 목록 조회 실패:', message),
        })
      },
      onFail: (message) => console.warn('[AppContext] 사용자 정보 조회 실패:', message),
    })
  }, [isOnAuthPage])

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