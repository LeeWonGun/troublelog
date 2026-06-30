import { useReducer } from 'react'
import { useAppContext } from '../context/AppContext.jsx'
import { QLIST } from '../constants/actionTypes.js'
import SearchBar from '../components/common/SearchBar.jsx'
import PostRow from '../components/common/PostRow.jsx'
import Pagination from '../components/common/Pagination.jsx'
import questionListReducer, { initialState } from '../reducers/questionListReducer.js'
import { MOCK_BOARD_POSTS } from '../constants/mockData.js'

const FILTER_TAGS = ['JAVA', 'React', 'Docker', 'Spring', 'Next.js']
const PAGE_SIZE = 5

function QuestionListPage() {
  const { state: appState } = useAppContext()
  const { activeTeam, teams } = appState
  const [state, dispatch] = useReducer(questionListReducer, initialState)

  const teamInfo = teams.find(t => t.id === activeTeam)

  const filtered = MOCK_BOARD_POSTS
    .filter(p => activeTeam ? p.team === activeTeam : p.visibility === 'PUBLIC')
    .filter(p => {
      if (state.filterStatus === 'solved') return p.solved
      if (state.filterStatus === 'unsolved') return !p.solved
      return true
    })
    .filter(p => state.filterTags.length === 0 || state.filterTags.some(t => p.tags.includes(t)))

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const paginated = filtered.slice((state.currentPage - 1) * PAGE_SIZE, state.currentPage * PAGE_SIZE)

  return (
    <div className="main">
      <SearchBar placeholder="제목, 내용으로 검색" />

      <div className="page-head">
        <h1>
          <span className="eyebrow">board</span>
          {teamInfo ? `${teamInfo.name} 게시판` : '전체 게시판'}
        </h1>
      </div>

      <div className="filter-bar">
        {[['all', '전체'], ['solved', '해결됨'], ['unsolved', '미해결']].map(([val, label]) => (
          <span
            key={val}
            className={`filter-pill ${state.filterStatus === val ? 'active' : ''}`}
            onClick={() => dispatch({ type: QLIST.SET_STATUS_FILTER, payload: val })}
          >
            {label}
          </span>
        ))}
        {FILTER_TAGS.map(tag => (
          <span
            key={tag}
            className={`filter-pill ${state.filterTags.includes(tag) ? 'active' : ''}`}
            onClick={() => dispatch({ type: QLIST.TOGGLE_TAG_FILTER, payload: tag })}
          >
            {tag}
          </span>
        ))}
      </div>

      <div className="sort-row">
        {[['latest', '최신순'], ['popular', '인기순'], ['solved', '해결된 글'], ['unsolved', '미해결 글']].map(([val, label]) => (
          <span
            key={val}
            className={state.sortBy === val ? 'active' : ''}
            onClick={() => dispatch({ type: QLIST.SET_SORT, payload: val })}
          >
            {label}
          </span>
        ))}
      </div>

      {paginated.length > 0
        ? paginated.map(p => <PostRow key={p.id} post={p} />)
        : <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>등록된 게시글이 없습니다.</div>
      }

      <Pagination
        current={state.currentPage}
        total={totalPages}
        onChange={p => dispatch({ type: QLIST.SET_PAGE, payload: p })}
      />
    </div>
  )
}

export default QuestionListPage