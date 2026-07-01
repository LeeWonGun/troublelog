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
const guard = (requestFn) => (...args) =>
  AUTH_API_ENABLED ? requestFn(...args) : mockAuthSuccess(dummyData)


/* ----------- 로그인 / 로그아웃 -------------- */

// GET /api/auth/me - 현재 로그인 사용자 조회
// 응답: { userId, nickname, authProvider }
export const getMe = () => 
    axiosInstance.get('/api/auth/me', { skipAuthRedirect: true })

// POST /api/auth/login - 이메일 로그인 → JWT 발급
// data: { userId, password }
export const login = guard((data) =>
  axiosInstance.post('/api/auth/login', data))

// POST /api/auth/logout - 로그아웃
export const logout = guard(() =>
  axiosInstance.post('/api/auth/logout'))

/* ----------- 회원가입 -------------- */

// POST /api/auth/signup - 회원가입
// data: { email, password, nickname, verificationCode }
export const signup = guard((data) =>
  axiosInstance.post('/api/auth/signup', data))

// POST /signup/send-code - 회원가입 > 이메일 인증코드 전송
// data: { email }
export const authEmail = guard((data) =>
  axiosInstance.post('/api/auth/signup/send-code', data))

// POST /signup/send-code - 회원가입 > 이메일 인증코드 확인
// data: { email }
export const authCheckEmail = guard((data) =>
  axiosInstance.post('/api/auth/signup/verify-code', data))

// GET /api/auth/check-email?email=xxx - 이메일 중복 확인
export const checkEmail = guard((email) =>
  axiosInstance.get('/api/auth/check-email', { params: { email } }))

// GET /api/auth/check-nickname?nickname=xxx - 닉네임 중복 확인
export const checkNickname = guard((nickname) =>
  axiosInstance.get('/api/auth/check-nickname', { params: { nickname } }))

/* ----------- 비밀번호 변경 -------------- */

// POST /api/auth/password-reset/send-code - 비밀번호 재설정 인증코드 전송
// data: { email }
export const sendPasswordResetCode = guard((data) =>
  axiosInstance.post('/api/auth/password-reset/send-code', data))

// POST /api/auth/password-reset/verify-code - 비밀번호 재설정 인증코드 확인
// data: { email, code }
export const verifyPasswordResetCode = guard((data) =>
  axiosInstance.post('/api/auth/password-reset/verify-code', data))

// PATCH /api/auth/password-reset - 비밀번호 재설정 (비로그인 상태, 이메일 인증 기반)
// data: { email, verificationCode, newPassword }
export const resetPasswordByEmail = guard((data) =>
  axiosInstance.patch('/api/auth/password-reset', data))