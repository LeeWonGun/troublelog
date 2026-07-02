import axiosInstance from './axiosInstance.js'

// POST /api/teams - 팀 생성 (생성자 LEADER 자동 등록)
// data: { name, description }
export const createTeam = (data) =>
  axiosInstance.post('/api/teams', data)

// GET /api/teams/code/{teamCode} - 팀 참여 전 팀 코드 유효성 확인
export const getTeamByCode = (teamCode) =>
  axiosInstance.get(`/api/teams/code/${encodeURIComponent(teamCode)}`)

// POST /api/teams/join - 팀 코드로 팀 참여
export const joinTeam = (teamCode) =>
  axiosInstance.post('/api/teams/join', { teamCode })

// GET /api/teams/my - 내 팀 목록 (탈퇴/삭제 팀 제외)
export const getMyTeams = () =>
  axiosInstance.get('/api/teams/my', { skipAuthRedirect: true })

// GET /api/teams/{teamId} - 팀 상세 조회
export const getTeam = (teamId) =>
  axiosInstance.get(`/api/teams/${teamId}`)

// GET /api/teams/{teamId}/members - 팀원 목록
export const getTeamMembers = (teamId) =>
  axiosInstance.get(`/api/teams/${teamId}/members`)

// PATCH /api/teams/{teamId}/leave - 팀 탈퇴 (MEMBER만 가능)
export const leaveTeam = (teamId) =>
  axiosInstance.patch(`/api/teams/${teamId}/leave`)

// DELETE /api/teams/{teamId} - 팀 삭제 (LEADER만, soft delete)
export const deleteTeam = (teamId) =>
  axiosInstance.delete(`/api/teams/${teamId}`)