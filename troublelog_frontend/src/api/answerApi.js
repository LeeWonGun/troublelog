import axiosInstance from './axiosInstance.js'

// GET /api/questions/{questionId}/answers - 답변/댓글/대댓글 계층 조회
export const getAnswers = (questionId) =>
  axiosInstance.get(`/api/questions/${questionId}/answers`)

// POST /api/questions/{questionId}/answers - 답변 작성 (depth=0)
export const createAnswer = (questionId, data) =>
  axiosInstance.post(`/api/questions/${questionId}/answers`, data)

// POST /api/answers/{answerId}/comments - 댓글 작성 (depth=1)
export const createComment = (answerId, data) =>
  axiosInstance.post(`/api/answers/${answerId}/comments`, data)

// POST /api/answers/{answerId}/replies - 대댓글 작성 (depth=2)
export const createReply = (answerId, data) =>
  axiosInstance.post(`/api/answers/${answerId}/replies`, data)

// PUT /api/answers/{answerId} - 답변/댓글/대댓글 수정 (채택 답변 불가)
export const updateAnswer = (answerId, data) =>
  axiosInstance.put(`/api/answers/${answerId}`, data)

// DELETE /api/answers/{answerId} - 답변/댓글/대댓글 삭제 (채택 답변 불가)
export const deleteAnswer = (answerId) =>
  axiosInstance.delete(`/api/answers/${answerId}`)

// POST /api/answers/{answerId}/accept - 답변 채택 (질문 작성자만, depth=0만)
export const acceptAnswer = (answerId) =>
  axiosInstance.post(`/api/answers/${answerId}/accept`)

// GET /api/users/me/answers - 내가 작성한 답변
export const getMyAnswers = () =>
  axiosInstance.get('/api/users/me/answers')

// GET /api/users/me/comments - 내가 작성한 댓글/대댓글
export const getMyComments = () =>
  axiosInstance.get('/api/users/me/comments')