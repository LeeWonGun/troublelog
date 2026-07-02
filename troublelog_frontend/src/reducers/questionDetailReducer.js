import { QDETAIL } from '../constants/actionTypes.js'

// 게시글 상세 페이지 상태
export const initialState = {
  question: null,   // GET /api/questions/{id} 응답 (QuestionDetailResponse)
  error: null,

  liked: false,      // 현재 사용자의 좋아요 여부 (SET_QUESTION 시 서버 응답의 likedByMe로 채워짐)
  likeCount: 0,      // SET_QUESTION 시 서버 응답의 likeCount로 채워짐

  deleteConfirmOpen: false,  // 게시글 삭제 확인 모달 표시 여부

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
        liked: !!action.payload.likedByMe,
        acceptedAnswerId: action.payload.acceptedAnswerId,
        error: null,
      }

    case QDETAIL.SET_ERROR:
      return { ...state, error: action.payload }

    // 좋아요 등록/취소 API 응답(LikeResponse: { liked, likeCount })을 그대로 반영한다.
    // 클라이언트 추측값이 아닌 서버 확정값을 쓰므로 연타/중복 요청에도 상태가 어긋나지 않는다.
    case QDETAIL.SET_LIKE:
      return { ...state, liked: action.payload.liked, likeCount: action.payload.likeCount }

    // 삭제 확인 모달 열기/닫기 (payload: bool)
    case QDETAIL.SET_DELETE_CONFIRM:
      return { ...state, deleteConfirmOpen: action.payload }

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