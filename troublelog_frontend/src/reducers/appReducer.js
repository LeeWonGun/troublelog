import { APP } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'

// 전역 앱 상태: 인증 정보, 팀 목록, 모달, 상세 검색 필터 관리
export const initialState = {
  isLoggedIn: false,   // 로그인 여부 (사이드바 비회원/회원 분기에 사용)
  nickname: null,
  email: null,
  authProvider: null,  // 'LOCAL' | 'GOOGLE'
  userSince: null,
  loading: true,       // 초기 인증 체크 완료 전 true

  teams: [],
  teamListOpen: true,
  activeTeam: null,          // null: 전체 게시판

  modal: null,
  modalTeamTarget: null,     // 팀 관리/탈퇴 대상 팀 id
  createdTeamCode: null,

  // 상세 검색 필터 (기술 스택 + 상태만, 이미지 필터 없음)
  searchKeyword: '',
  searchStackToggles: {},   // 'language-Java' -> bool 형태
  searchStatus: 'all',      // all | solved | unsolved
}

const appReducer = (state, action) => {
  switch (action.type) {
    case APP.SET_LOADING:
      return { ...state, loading: action.payload }

    case APP.SET_USER:
      return {
        ...state,
        isLoggedIn: true,
        nickname: action.payload.nickname,
        email: action.payload.email,
        authProvider: action.payload.authProvider,
        userSince: action.payload.userSince ?? null,
        loading: false,
      }

    // 로그아웃 시 사용자 정보 초기화
    case APP.CLEAR_USER:
      return {
        ...initialState,
        loading: false,
      }

    case APP.SET_TEAMS:
      return { ...state, teams: action.payload }

    case APP.SET_NICKNAME:
      return { ...state, nickname: action.payload }

    case APP.TOGGLE_TEAM_LIST:
      return { ...state, teamListOpen: !state.teamListOpen }

    case APP.SET_ACTIVE_TEAM:
      return { ...state, activeTeam: action.payload }

    case APP.OPEN_MODAL:
      return {
        ...state,
        modal: action.payload.modal,
        modalTeamTarget: action.payload.teamId ?? state.modalTeamTarget,
      }

    case APP.CLOSE_MODAL:
      return { ...state, modal: null }

    case APP.ADD_TEAM: {
      const newTeam = { id: Date.now(), name: action.payload.name, role: 'leader' }
      return {
        ...state,
        teams: [...state.teams, newTeam],
        modal: MODAL.CREATE_TEAM_SUCCESS,
        createdTeamCode: action.payload.code ?? 'Q7K2P9',
      }
    }

    case APP.REMOVE_TEAM:
      return { ...state, teams: state.teams.filter(t => t.id !== action.payload), modal: null }

    case APP.LEAVE_TEAM:
      return { ...state, teams: state.teams.filter(t => t.id !== action.payload), modal: null }

    case APP.SET_SEARCH_KEYWORD:
      return { ...state, searchKeyword: action.payload }

    case APP.TOGGLE_SEARCH_STACK:
      return {
        ...state,
        searchStackToggles: {
          ...state.searchStackToggles,
          [action.payload]: !state.searchStackToggles[action.payload],
        },
      }

    case APP.SET_SEARCH_STATUS:
      return { ...state, searchStatus: action.payload }

    // 검색 필터 초기화 (이미지 필터 없음, 목업 기준)
    case APP.RESET_SEARCH_FILTERS:
      return { ...state, searchStackToggles: {}, searchStatus: 'all', searchKeyword: '' }

    default:
      return state
  }
}

export default appReducer
