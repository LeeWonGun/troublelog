import { useReducer } from 'react'
import { useAppContext } from '../context/AppContext.jsx'
import { QLIST } from '../constants/actionTypes.js'
import SearchBar from '../components/common/SearchBar.jsx'
import PostRow from '../components/common/PostRow.jsx'
import Pagination from '../components/common/Pagination.jsx'
import questionListReducer, { initialState } from '../reducers/questionListReducer.js'
import { getPublicQuestions, searchPublicQuestions } from '../api/questionApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { mapQuestionListItem } from '../util/questionMapper.js'
import { MOCK_BOARD_POSTS } from '../constants/mockData.js'

const PAGE_SIZE = 5

function QuestionListPage() {
  const { state: appState } = useAppContext()
  const { activeTeam, teams } = appState
  const [state, dispatch] = useReducer(questionListReducer, initialState)

  const teamInfo = teams.find(t => t.id === activeTeam)

  //TODO: 팀 목록/검색 API 연동 필요
  useEffect(() => {
    if (activeTeam) return

    let ignore = false // 정렬/검색어/페이지가 빠르게 바뀔 때 늦게 도착한 응답이 최신 상태를 덮어쓰지 않도록 막는 가드

    dispatch({ type: QLIST.SET_LOADING })

    const trimmedKeyword = state.keyword.trim()
    const commonParams = {
      sort: state.sortBy.toUpperCase(), // 'latest' -> 'LATEST' (백엔드 sort enum: LATEST | POPULAR | SOLVED | UNSOLVED)
      page: state.currentPage - 1,      // 화면은 1-based, API는 0-based
      size: PAGE_SIZE,
    }

    // 검색어가 있으면 검색 API(searchPublicQuestions), 없으면 공개 목록 API(getPublicQuestions)를 호출한다.
    const apiCall = trimmedKeyword
      ? () => searchPublicQuestions({ ...commonParams, keyword: trimmedKeyword })
      : () => getPublicQuestions(commonParams)

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
  }, [activeTeam, state.sortBy, state.currentPage, state.keyword])

  // 목업 기반 팀 게시판 (팀 전용 API 연동 전까지 유지)
  const mockFiltered = MOCK_BOARD_POSTS
    .filter(p => activeTeam ? p.team === activeTeam : p.visibility === 'PUBLIC')
  const mockTotalPages = Math.max(1, Math.ceil(mockFiltered.length / PAGE_SIZE))
  const mockPaginated = mockFiltered.slice((state.currentPage - 1) * PAGE_SIZE, state.currentPage * PAGE_SIZE)

  const totalPages = activeTeam ? mockTotalPages : state.totalPages

  return (
    <div className="main">
      <SearchBar
        placeholder="제목, 내용으로 검색"
        onSearch={() => dispatch({ type: QLIST.SET_KEYWORD, payload: searchKeyword })}
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

      {activeTeam ? (
        mockPaginated.length > 0
          ? mockPaginated.map(p => <PostRow key={p.id} post={p} />)
          : <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>등록된 게시글이 없습니다.</div>
      ) : state.loading ? (
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
        total={totalPages}
        onChange={p => dispatch({ type: QLIST.SET_PAGE, payload: p })}
      />
    </div>
  )
}

export default QuestionListPage
