import axiosInstance from './axiosInstance.js'

// GET /api/tech-stacks - 기술 스택 목록 조회 (active=1만)
export const getTechStacks = () =>
  axiosInstance.get('/api/tech-stacks')

// GET /api/tech-stacks/categories - 카테고리별 기술 스택 조회
export const getTechStackCategories = () =>
  axiosInstance.get('/api/tech-stacks/categories')