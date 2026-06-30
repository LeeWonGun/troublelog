import { QLIST } from '../constants/actionTypes.js'

// 게시판 목록 페이지 상태
export const initialState = {
  sortBy: 'latest',      // latest | popular | solved | unsolved
  filterStatus: 'all',   // all | solved | unsolved
  filterTags: [],        // ['JAVA', 'React', ...]
  currentPage: 1,

  posts: [],             // 게시글 목록
  totalPages: 1,
  loading: false,
  error: null,
}

const questionListReducer = (state, action) => {
  switch (action.type) {
    case QLIST.SET_LOADING:
      return { ...state, loading: action.payload, error: null }

    case QLIST.SET_ERROR:
      return { ...state, loading: false, error: action.payload }

    case QLIST.SET_POSTS:
      return {
        ...state,
        posts: action.payload.posts,
        totalPages: action.payload.totalPages,
        loading: false,
        error: null,
      }

    case QLIST.SET_SORT:
      return { ...state, sortBy: action.payload, currentPage: 1 }

    case QLIST.TOGGLE_TAG_FILTER: {
      const tags = state.filterTags.includes(action.payload)
        ? state.filterTags.filter(t => t !== action.payload)
        : [...state.filterTags, action.payload]
      return { ...state, filterTags: tags, currentPage: 1 }
    }

    case QLIST.SET_STATUS_FILTER:
      return { ...state, filterStatus: action.payload, currentPage: 1 }

    case QLIST.RESET_FILTERS:
      return { ...state, filterTags: [], filterStatus: 'all', currentPage: 1 }

    case QLIST.SET_PAGE:
      return { ...state, currentPage: action.payload }

    default:
      return state
  }
}

export default questionListReducer