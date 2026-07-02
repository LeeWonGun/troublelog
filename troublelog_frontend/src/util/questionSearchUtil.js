// 게시판 목록/검색 API 파라미터 빌더 (QuestionListPage 전용 유틸)

// 상세 검색 상태 필터: 화면 값(all | solved | unsolved) -> 백엔드 status enum(SOLVED | UNSOLVED)
const STATUS_PARAM = {
  solved: 'SOLVED',
  unsolved: 'UNSOLVED',
}

// 확정된 검색 조건이 하나라도 있으면 true -> 검색 API, 없으면 목록 API 사용
export const hasSearchCondition = (state) =>
  state.keyword.trim().length > 0
  || state.filterStatus !== 'all'
  || state.filterTags.length > 0

// 목록 API(getPublicQuestions / getTeamQuestions) 공통 파라미터
export const buildQuestionListParams = (state, pageSize) => ({
  sort: state.sortBy,          // LATEST | POPULAR | SOLVED | UNSOLVED
  page: state.currentPage - 1, // 화면은 1-based, API는 0-based
  size: pageSize,
})

// 검색 API(searchPublicQuestions / searchTeamQuestions) 파라미터
export const buildQuestionSearchParams = (state, pageSize) => ({
  ...buildQuestionListParams(state, pageSize),
  keyword: state.keyword.trim() || undefined,
  status: STATUS_PARAM[state.filterStatus],
  
  techStackIds: state.filterTags.length > 0 ? state.filterTags.join(',') : undefined,
})