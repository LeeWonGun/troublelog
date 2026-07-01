import axiosInstance from './axiosInstance.js'

// ─────────────────────────────────────────────────────────
// TODO: (BE 연동 대기): 인증 관련 API가 아직 서버에 없어 임시로 호출을 막아둔다.
// 백엔드에 /api/auth/* 가 배포되면 아래 AUTH_API_ENABLED 를 true로 바꾸거나
// 이 플래그/guard 블록을 통째로 제거하면 즉시 원상 복구된다.
// ─────────────────────────────────────────────────────────
const AUTH_API_ENABLED = true
const DUMMY_USER = {
  nickname: '더미유저',
  email: 'dummy@troublelog.com',
  authProvider: 'LOCAL',
  userSince: '2026.01.01',
}

// 비활성화 상태에서 실제 axios 요청 없이 더미 데이터 응답
const mockAuthSuccess = (data = null) =>
  Promise.resolve({ success: true, data, message: null })

// 플래그가 꺼져있으면 axiosInstance 호출 자체를 하지 않도록 감싸는 헬퍼
// @param {Function} requestFn - 실제 axios 요청 함수
// @param {any} dummyData - 비활성화 상태일 때 돌려줄 더미 data
const guard = (requestFn) => (...args) =>
  AUTH_API_ENABLED ? requestFn(...args) : mockAuthSuccess(dummyData)


// POST /api/auth/signup - 회원가입
// data: { userId, password, nickname }
export const signup = guard((data) =>
  axiosInstance.post('/api/auth/signup', data))

// POST /api/auth/login - 이메일 로그인 → JWT 발급
// data: { userId, password }
export const login = guard((data) =>
  axiosInstance.post('/api/auth/login', data))

// POST /api/auth/logout - 로그아웃
export const logout = guard(() =>
  axiosInstance.post('/api/auth/logout'))

// GET /api/auth/me - 현재 로그인 사용자 조회
// 응답: { userId, nickname, authProvider }
export const getMe = () => 
    axiosInstance.get('/api/auth/me', { skipAuthRedirect: true })

// TODO: 임시로 사용. 실제 api 확인 필요
// GET /api/auth/email?email=xxx - 이메일 중복 확인
export const authEmail = guard((email) =>
  axiosInstance.get('/api/auth/email', { params: { email } }))

// GET /api/auth/check-email?email=xxx - 이메일 중복 확인
export const checkEmail = guard((email) =>
  axiosInstance.get('/api/auth/check-email', { params: { email } }))

// GET /api/auth/check-nickname?nickname=xxx - 닉네임 중복 확인
export const checkNickname = guard((nickname) =>
  axiosInstance.get('/api/auth/check-nickname', { params: { nickname } }))

