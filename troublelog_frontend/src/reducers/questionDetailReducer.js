import { QDETAIL } from '../constants/actionTypes.js'

// 게시글 상세 페이지 상태
export const initialState = {
  liked: false,
  likeCount: 32,
  acceptedAnswerId: null,    // 채택된 답변 id
  answerLikes: {},           // answerId -> bool
  commentInputs: {},         // answerId -> string
}

const questionDetailReducer = (state, action) => {
  switch (action.type) {
    case QDETAIL.TOGGLE_POST_LIKE:
      return {
        ...state,
        liked: !state.liked,
        likeCount: state.liked ? state.likeCount - 1 : state.likeCount + 1,
      }
    case QDETAIL.ACCEPT_ANSWER:
      if (state.acceptedAnswerId) return state
      return { ...state, acceptedAnswerId: action.payload }
    case QDETAIL.TOGGLE_ANSWER_LIKE:
      return {
        ...state,
        answerLikes: {
          ...state.answerLikes,
          [action.payload]: !state.answerLikes[action.payload],
        },
      }
    case QDETAIL.SET_COMMENT_INPUT:
      return {
        ...state,
        commentInputs: { ...state.commentInputs, [action.answerId]: action.value },
      }
    default:
      return state
  }
}

export default questionDetailReducer