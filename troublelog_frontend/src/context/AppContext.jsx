import { createContext, useContext, useReducer } from 'react'
import appReducer, { initialState } from '../reducers/appReducer.js'


// 전역 상태 (인증, 팀, 모달, 검색필터)

const AppContext = createContext(null)

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(appReducer, initialState)
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