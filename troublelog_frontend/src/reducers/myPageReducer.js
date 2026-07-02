import { MYPAGE } from '../constants/actionTypes.js'

export const initialState = {
  activeTab: 'questions',
  pages: { questions: 1, answers: 1, comments: 1 },

  // 내 질문 탭 - 서버 페이징 기반 API 연동 상태
  myQuestions: {
    loading: false,
    error: null,
    items: [],       // mapQuestionListItem으로 변환된 질문 목록
    totalPages: 1,   // 서버가 계산한 전체 페이지 수
    totalCount: 0,   // 탭 카운트 표시용 전체 질문 수 (totalElements)
  },
}

const myPageReducer = (state, action) => {
  switch (action.type) {
    case MYPAGE.SET_TAB:
      return { ...state, activeTab: action.payload }
    case MYPAGE.SET_PAGE:
      return { ...state, pages: { ...state.pages, [state.activeTab]: action.payload } }

    case MYPAGE.SET_LOADING:
      return { ...state, myQuestions: { ...state.myQuestions, loading: true, error: null } }
    case MYPAGE.SET_DATA:
      return {
        ...state,
        myQuestions: {
          loading: false,
          error: null,
          items: action.payload.items,
          totalPages: action.payload.totalPages,
          totalCount: action.payload.totalCount,
        },
      }
    case MYPAGE.SET_ERROR:
      return { ...state, myQuestions: { ...state.myQuestions, loading: false, error: action.payload } }
    default:
      return state
  }
}

export default myPageReducer