import axiosInstance from './axiosInstance.js'

// POST /api/auth/signup - 회원가입 
// data: { userId, password, nickname }
export const signup = (data) =>
  axiosInstance.post('/api/auth/signup', data)

// POST /api/auth/login - 이메일 로그인 → JWT 발급 
// data: { userId, password }
export const login = (data) =>
  axiosInstance.post('/api/auth/login', data)

// POST /api/auth/logout - 로그아웃
export const logout = () =>
  axiosInstance.post('/api/auth/logout')

// GET /api/auth/me - 현재 로그인 사용자 조회 
// 응답: { userId, nickname, authProvider }
export const getMe = () => 
    axiosInstance.get('/api/auth/me')

// GET /api/auth/check-email?email=xxx - 이메일 중복 확인
export const checkEmail = (email) => 
    axiosInstance.get('/api/auth/check-email', { params: { email } })

// GET /api/auth/check-nickname?nickname=xxx - 닉네임 중복 확인 
export const checkNickname = (nickname) => 
    axiosInstance.get('/api/auth/check-nickname', { params: { nickname } })