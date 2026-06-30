import axiosInstance from './axiosInstance.js'

// POST /api/questions/{questionId}/likes - 질문 좋아요 (target_type=QUE)
export const likeQuestion = (questionId) =>
  axiosInstance.post(`/api/questions/${questionId}/likes`)

// DELETE /api/questions/{questionId}/likes - 질문 좋아요 취소
export const unlikeQuestion = (questionId) =>
  axiosInstance.delete(`/api/questions/${questionId}/likes`)

// POST /api/answers/{answerId}/likes - 답변 좋아요 (target_type=ANS, depth=0만)
export const likeAnswer = (answerId) =>
  axiosInstance.post(`/api/answers/${answerId}/likes`)

// DELETE /api/answers/{answerId}/likes - 답변 좋아요 취소
export const unlikeAnswer = (answerId) =>
  axiosInstance.delete(`/api/answers/${answerId}/likes`)