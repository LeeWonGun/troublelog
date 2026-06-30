import { WRITE } from '../constants/actionTypes.js'

// 게시글 작성 페이지 상태
export const initialState = {
  title: '',
  situation: '',
  errorMessage: '',
  triedMethods: '',
  visibility: 'PUBLIC',   // PUBLIC | TEAM
  selectedTeamId: '',
  stackToggles: {},        // 'language-Java' -> bool
  images: [],              // 첨부 이미지 (최대 4개)
}

const writeReducer = (state, action) => {
  switch (action.type) {
    case WRITE.SET_FIELD:
      return { ...state, [action.field]: action.value }
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
      if (state.images.length >= 4) return state
      return { ...state, images: [...state.images, action.payload] }
    case WRITE.REMOVE_IMAGE:
      return { ...state, images: state.images.filter((_, i) => i !== action.payload) }
    case WRITE.RESET:
      return { ...initialState }
    default:
      return state
  }
}

export default writeReducer