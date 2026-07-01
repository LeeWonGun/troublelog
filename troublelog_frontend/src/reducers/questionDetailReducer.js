import { QDETAIL } from '../constants/actionTypes.js'

// 게시글 상세 페이지 상태
export const initialState = {
  question: null,   // GET /api/questions/{id} 응답 (QuestionDetailResponse)
  error: null,

  liked: false,
  likeCount: 0,      // SET_QUESTION 시 서버 응답의 likeCount로 채워짐

  acceptedAnswerId: null,    // 채택된 답변 id (답변 API 연동 전까지 목업 유지)
  answerLikes: {},           // answerId -> bool
  commentInputs: {},         // answerId -> string
}

const questionDetailReducer = (state, action) => {
  switch (action.type) {
    case QDETAIL.SET_QUESTION:
      return {
        ...state,
        question: action.payload,
        likeCount: action.payload.likeCount,
        acceptedAnswerId: action.payload.acceptedAnswerId,
        error: null,
      }

    case QDETAIL.SET_ERROR:
      return { ...state, error: action.payload }

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