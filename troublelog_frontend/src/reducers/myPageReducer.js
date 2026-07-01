import { MYPAGE } from '../constants/actionTypes.js'

export const initialState = {
  activeTab: 'questions',
  pages: { questions: 1, answers: 1, comments: 1 },
}

const myPageReducer = (state, action) => {
  switch (action.type) {
    case MYPAGE.SET_TAB:
      return { ...state, activeTab: action.payload }
    case MYPAGE.SET_PAGE:
      return { ...state, pages: { ...state.pages, [state.activeTab]: action.payload } }
    default:
      return state
  }
}

export default myPageReducer