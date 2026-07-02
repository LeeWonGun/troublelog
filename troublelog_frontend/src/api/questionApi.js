import axiosInstance from './axiosInstance.js'

// GET /api/questions/public - 전체 공개 질문 목록 (비회원 가능, 페이징 적용)
// params: { page, size, sort } - sort: LATEST | POPULAR | SOLVED | UNSOLVED (기본값 LATEST)
// 응답 data: PageResponse<QuestionListResponse> = { content, page, size, totalElements, totalPages, hasNext }
export const getPublicQuestions = (params) =>
  axiosInstance.get('/api/questions/public', { params, skipAuthRedirect: true })

// GET /api/questions/popular - 인기 게시글 (좋아요 순)
export const getPopularQuestions = (params) =>
  axiosInstance.get('/api/questions/popular', { params, skipAuthRedirect: true })

// GET /api/questions/search - 전체 공개 게시판 질문 검색 (페이징 적용)
// params: { keyword, status, techStackIds, sort, page, size }
export const searchPublicQuestions = (params) =>
  axiosInstance.get('/api/questions/search', { params, skipAuthRedirect: true })

// GET /api/questions/{questionId} - 질문 상세
export const getQuestion = (questionId) =>
  axiosInstance.get(`/api/questions/${questionId}`)

// POST /api/questions - 질문 작성
export const createQuestion = (data) =>
  axiosInstance.post('/api/questions', data)

// PUT /api/questions/{questionId} - 질문 수정 (작성자 본인만, 서버에서 검증)
export const updateQuestion = (questionId, data) =>
  axiosInstance.put(`/api/questions/${questionId}`, data)

// DELETE /api/questions/{questionId} - 질문 삭제 (soft delete, 작성자 본인만)
export const deleteQuestion = (questionId) =>
  axiosInstance.delete(`/api/questions/${questionId}`)

// GET /api/teams/{teamId}/questions - 팀 질문 목록 (팀원만, 서버에서 검증)
export const getTeamQuestions = (teamId, params) =>
  axiosInstance.get(`/api/teams/${teamId}/questions`, { params })

// GET /api/teams/{teamId}/questions/search - 팀 게시판 질문 검색 (팀원만)
// params: { keyword, status, techStackIds, sort, page, size }
export const searchTeamQuestions = (teamId, params) =>
  axiosInstance.get(`/api/teams/${teamId}/questions/search`, { params })

// GET /api/users/me/questions - 내가 작성한 질문
export const getMyQuestions = (params) =>
  axiosInstance.get('/api/users/me/questions', { params })

// PATCH /api/questions/{questionId}/status - 질문 상태 변경
// TODO: 답변 채택 브랜치에서 연동 예정
export const updateQuestionStatus = (questionId, status) =>
  axiosInstance.patch(`/api/questions/${questionId}/status`, { status })