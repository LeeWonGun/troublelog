import { useAppContext } from '../../context/AppContext.jsx'
import { APP } from '../../constants/actionTypes.js'

function getActiveFilterCount(state) {
  const stackCount = Object.values(state.searchStackToggles).filter(Boolean).length
  const statusCount = state.searchStatus !== 'all' ? 1 : 0
  const imageCount = state.searchHasImage ? 1 : 0
  return stackCount + statusCount + imageCount
}

function SearchBar({ placeholder = '에러 메시지, 키워드로 검색해보세요', onSearch }) {
  const { state, dispatch } = useAppContext()
  const filterCount = getActiveFilterCount(state)

  return (
    <div className="topbar">
      <div className="search-box">
        🔍
        <input
          placeholder={placeholder}
          value={state.searchKeyword}
          onChange={e => dispatch({ type: APP.SET_SEARCH_KEYWORD, payload: e.target.value })}
          onKeyDown={e => e.key === 'Enter' && onSearch?.()}
        />
        <span className="key">Enter ↵</span>
      </div>
      <button
        className={`btn btn-ghost btn-detail-search ${filterCount > 0 ? 'has-filters' : ''}`}
        onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: 'search-detail' } })}
      >
        상세 검색
        {filterCount > 0 && <span className="filter-count">{filterCount}</span>}
      </button>
    </div>
  )
}

export default SearchBar