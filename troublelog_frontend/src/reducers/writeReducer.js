import { WRITE } from '../constants/actionTypes.js'

// 게시글 작성/수정 페이지 상태
export const initialState = {
  title: '',
  situation: '',
  errorLanguage: '',     // 오류 코드 언어 선택 (Java, Python, JavaScript 등)
  errorCode: '',         // 오류 코드 내용
  errorMessage: '',      // 오류 메시지 로그
  triedMethods: '',
  visibility: 'PUBLIC',  // PUBLIC | TEAM
  selectedTeamId: '',
  stackToggles: {},      // techStackId -> bool
  images: [],            // 첨부 이미지 (최대 4개)
  error: '',             // 폼 유효성/제출 실패 에러 메시지 (빈 문자열이면 비표시)

  // 수정 페이지 전용: 기존 게시글 조회 상태 (작성 페이지는 항상 false/'')
  loading: false,
  loadError: '',
}

const writeReducer = (state, action) => {
  switch (action.type) {
    case WRITE.SET_FIELD:
      return { ...state, [action.field]: action.value }

    // 수정 페이지: GET 응답으로 폼 상태 pre-fill 완료
    case WRITE.PREFILL:
      return { ...state, ...action.payload, loading: false, loadError: '' }

    // 수정 페이지: 기존 게시글 조회 실패
    case WRITE.LOAD_FAILED:
      return { ...state, loading: false, loadError: action.payload }

    case WRITE.SET_VISIBILITY:
      return {
        ...state,
        visibility: action.payload,
        selectedTeamId: action.payload === 'PUBLIC' ? '' : state.selectedTeamId,
      }

    case WRITE.TOGGLE_STACK: {
      const current = !!state.stackToggles[action.payload]
      return { ...state, stackToggles: { ...state.stackToggles, [action.payload]: !current } }
    }

    case WRITE.ADD_IMAGE:
      // 최대 4개 제한
      if (state.images.length >= 4) return state
      return { ...state, images: [...state.images, action.payload] }

    case WRITE.REMOVE_IMAGE:
      return { ...state, images: state.images.filter((_, i) => i !== action.payload) }

    case WRITE.RESET:
      return { ...initialState }

    case WRITE.SET_ERROR:
      return { ...state, error: action.payload }

    default:
      return state
  }
}

export default writeReducer