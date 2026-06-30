import axiosInstance from './axiosInstance.js'

// POST /api/questions/{questionId}/files - 이미지 첨부 (jpg/jpeg/png, 5MB 이하)
export const uploadFile = (questionId, formData) =>
  axiosInstance.post(`/api/questions/${questionId}/files`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

// GET /api/files/{fileId} - 첨부 파일 조회
export const getFile = (fileId) =>
  axiosInstance.get(`/api/files/${fileId}`)

// DELETE /api/files/{fileId} - 첨부 파일 삭제 (soft delete)
export const deleteFile = (fileId) =>
  axiosInstance.delete(`/api/files/${fileId}`)