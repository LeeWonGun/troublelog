import { useEffect, useReducer, useRef } from 'react'
import { useAppContext } from '../context/AppContext.jsx'
import { QLIST } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'
import SearchBar from '../components/common/SearchBar.jsx'
import PostRow from '../components/common/PostRow.jsx'
import Pagination from '../components/common/Pagination.jsx'
import questionListReducer, { initialState } from '../reducers/questionListReducer.js'
import {
  getPublicQuestions,
  searchPublicQuestions,
  getTeamQuestions,
  searchTeamQuestions,
} from '../api/questionApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { mapQuestionListItem } from '../util/questionMapper.js'
import {
  hasSearchCondition,
  buildQuestionListParams,
  buildQuestionSearchParams,
} from '../util/questionSearchUtil.js'

const PAGE_SIZE = 5

function QuestionListPage() {
  const { state: appState } = useAppContext()
  const { activeTeam, teams } = appState
  const [state, dispatch] = useReducer(questionListReducer, initialState)

  const teamInfo = teams.find(t => t.teamId === activeTeam)

  // 전역 상태(SearchBar 입력 + 상세 검색 모달 필터)를 목록 리듀서의 "확정 검색 조건"으로 복사한다.
  // 입력 즉시가 아니라 Enter / 모달 닫힘 시에만 반영해 타이핑 중 불필요한 API 호출을 막는다.
  const applyFilters = () => {
    dispatch({
      type: QLIST.APPLY_FILTERS,
      payload: {
        keyword: appState.searchKeyword,
        filterStatus: appState.searchStatus,
        // searchStackToggles: { techStackId: bool } -> 선택된 id 배열
        filterTags: Object.entries(appState.searchStackToggles)
          .filter(([, on]) => on)
          .map(([id]) => Number(id)),
      },
    })
  }

  // 상세 검색 모달이 닫히는 시점("검색 적용" 버튼/backdrop 클릭)에 필터를 확정한다.
  const prevModalRef = useRef(appState.modal)
  useEffect(() => {
    const closedSearchModal =
      prevModalRef.current === MODAL.SEARCH_DETAIL && appState.modal === null
    prevModalRef.current = appState.modal
    if (closedSearchModal) applyFilters()
  }, [appState.modal]) // eslint-disable-line react-hooks/exhaustive-deps

  // 게시판(전체 <-> 팀) 전환 시 1페이지부터 다시 조회
  useEffect(() => {
    dispatch({ type: QLIST.SET_PAGE, payload: 1 })
  }, [activeTeam])

  // 목록/검색 조회 - 확정된 검색 조건, 정렬, 페이지가 바뀔 때마다 재조회
  useEffect(() => {
    let ignore = false // 늦게 도착한 응답이 최신 상태를 덮어쓰지 않도록 막는 가드

    dispatch({ type: QLIST.SET_LOADING })

    const searching = hasSearchCondition(state)
    const params = searching
      ? buildQuestionSearchParams(state, PAGE_SIZE)
      : buildQuestionListParams(state, PAGE_SIZE)

    // 팀 게시판이면 팀 전용 API(팀원 여부는 서버가 검증), 아니면 공개 API.
    // 검색 조건 유무로 목록/검색 API를 분기한다.
    const apiCall = activeTeam
      ? () => (searching ? searchTeamQuestions(activeTeam, params) : getTeamQuestions(activeTeam, params))
      : () => (searching ? searchPublicQuestions(params) : getPublicQuestions(params))

    requestHandler(apiCall, {
      isCancelled: () => ignore,
      onSuccess: (data) => dispatch({
        type: QLIST.SET_POSTS,
        payload: {
          posts: (data.content ?? []).map(mapQuestionListItem),
          totalPages: data.totalPages,
        },
      }),
      onFail: (message) => dispatch({ type: QLIST.SET_ERROR, payload: message }),
      fallbackMessage: '게시글 목록을 불러오지 못했습니다.',
    })

    return () => { ignore = true }
  }, [activeTeam, state.sortBy, state.currentPage, state.keyword, state.filterStatus, state.filterTags])

  return (
    <div className="main">
      <SearchBar
        placeholder="제목, 내용으로 검색"
        onSearch={applyFilters}
      />

      <div className="page-head">
        <h1>
          <span className="eyebrow">board</span>
          {teamInfo ? `${teamInfo.name} 게시판` : '전체 게시판'}
        </h1>
      </div>

      <div className="sort-row">
        {[['LATEST', '최신순'], ['POPULAR', '인기순'], ['SOLVED', '해결된 글'], ['UNSOLVED', '미해결 글']].map(([val, label]) => (
          <span
            key={val}
            className={state.sortBy === val ? 'active' : ''}
            onClick={() => dispatch({ type: QLIST.SET_SORT, payload: val })}
          >
            {label}
          </span>
        ))}
      </div>

      {state.loading ? (
        <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>불러오는 중...</div>
      ) : state.error ? (
        <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>{state.error}</div>
      ) : state.posts.length > 0 ? (
        state.posts.map(p => <PostRow key={p.id} post={p} />)
      ) : (
        <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>등록된 게시글이 없습니다.</div>
      )}

      <Pagination
        current={state.currentPage}
        total={state.totalPages}
        onChange={p => dispatch({ type: QLIST.SET_PAGE, payload: p })}
      />
    </div>
  )
}

export default QuestionListPage