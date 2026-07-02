import axiosInstance from './axiosInstance.js'

// GET /api/users/me - 내 정보 조회 (email, nickname, authProvider)
export const getMyProfile = () =>
  axiosInstance.get('/api/users/me')

// PATCH /api/users/me/nickname - 닉네임 변경
export const updateNickname = (nickname) =>
  axiosInstance.patch('/api/users/me/nickname', { nickname })

// PATCH /api/users/me/password - 비밀번호 변경 (LOCAL 사용자만)
export const updatePassword = (data) =>
  axiosInstance.patch('/api/users/me/password', data)

// DELETE /api/users/me - 회원 탈퇴 (soft delete)
export const deleteAccount = () =>
  axiosInstance.delete('/api/users/me')