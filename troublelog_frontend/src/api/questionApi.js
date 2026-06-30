import axiosInstance from './axiosInstance.js'

// GET /api/questions/public - 전체 공개 질문 목록 (비회원 가능)
// params: { page, size, sort }
export const getPublicQuestions = (params) =>
  axiosInstance.get('/api/questions/public', { params })

// GET /api/questions/popular - 인기 게시글 (좋아요 순)
export const getPopularQuestions = () =>
  axiosInstance.get('/api/questions/popular')

// GET /api/questions/search - 질문 검색
// params: { keyword, stacks, status, page, size }
export const searchQuestions = (params) =>
  axiosInstance.get('/api/questions/search', { params })

// GET /api/questions/{questionId} - 질문 상세
export const getQuestion = (questionId) =>
  axiosInstance.get(`/api/questions/${questionId}`)

// POST /api/questions - 질문 작성
// data: { title, situation, errorMessage, triedMethods, visibility, teamId?, techStackIds[] }
export const createQuestion = (data) =>
  axiosInstance.post('/api/questions', data)

// PUT /api/questions/{questionId} - 질문 수정
export const updateQuestion = (questionId, data) =>
  axiosInstance.put(`/api/questions/${questionId}`, data)

// DELETE /api/questions/{questionId} - 질문 삭제 (soft delete)
export const deleteQuestion = (questionId) =>
  axiosInstance.delete(`/api/questions/${questionId}`)

// GET /api/teams/{teamId}/questions - 팀 질문 목록
export const getTeamQuestions = (teamId, params) =>
  axiosInstance.get(`/api/teams/${teamId}/questions`, { params })

// GET /api/users/me/questions - 내가 작성한 질문
export const getMyQuestions = () =>
  axiosInstance.get('/api/users/me/questions')

// PATCH /api/questions/{questionId}/status - 질문 상태 변경 
export const updateQuestionStatus = (questionId, status) =>
  axiosInstance.patch(`/api/questions/${questionId}/status`, { status })