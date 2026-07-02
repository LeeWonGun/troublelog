import { createContext, useContext } from 'react'

/**
 * 전역 AppContext의 "객체"와 소비 훅만 분리한 모듈.
 *
 * Context 객체를 컴포넌트 파일(AppContext.jsx)에 함께 두면
 * Vite HMR(Fast Refresh) 시 그 파일이 재평가되면서 createContext가 다시 실행돼
 * 새 Context 객체가 만들어지고, 이미 마운트된 Provider(이전 객체)와 어긋나
 * "useAppContext must be used within AppProvider" 오류가 발생한다.
 * 컴포넌트가 없는 이 모듈은 재평가 대상이 아니므로 Context 객체가 항상 하나로 유지된다.
 */
export const AppContext = createContext(null)

// 커스텀 훅 - AppProvider 외부 사용 시 오류 발생
export function useAppContext() {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useAppContext must be used within AppProvider')
  return ctx
}