import { useEffect, useReducer } from 'react'
import appReducer, { initialState } from '../reducers/appReducer.js'
import { APP } from '../constants/actionTypes.js'
import { AUTH_PAGE_PATHS } from '../constants/routePaths.js'
import { getMe } from '../api/authApi.js'
import { getMyTeams } from '../api/teamApi.js'
import { registerLoadingCallback } from '../api/axiosInstance.js'
import { requestHandler, registerGlobalErrorHandler } from '../util/requestHandler.js'
import { useLocation } from 'react-router-dom'
import { getTechStacks } from '../api/techStackApi.js'
import { AppContext } from './appContextCore.js'

// 전역 상태 (인증, 팀, 모달, 검색필터)
// Context 객체/훅은 HMR 안정성을 위해 appContextCore.js로 분리 (아래 re-export는 기존 import 경로 호환용)
export { useAppContext } from './appContextCore.js'

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

  // requestHandler(showGlobalError: true)가 호출하는 전역 에러 모달 콜백 등록
  useEffect(() => {
    registerGlobalErrorHandler((message) => {
      dispatch({ type: APP.SHOW_ERROR, payload: message })
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

  // 기술 스택 목록은 공개 API이고 화면 여러 곳(작성/검색)에서 쓰므로 앱 마운트 시 1회만 조회
  useEffect(() => {
    requestHandler(getTechStacks, {
      onSuccess: (data) => dispatch({ type: APP.SET_TECH_STACKS, payload: data }),
      onFail: (message) => console.warn('[AppContext] 기술 스택 조회 실패:', message),
    })
  }, [])

  return (
    <AppContext.Provider value={{ state, dispatch }}>
      {children}
    </AppContext.Provider>
  )
}