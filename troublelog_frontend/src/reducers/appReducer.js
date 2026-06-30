import { APP } from '../constants/actionTypes.js'

// 전역 앱 상태: 인증 정보, 팀 목록, 모달, 상세 검색 필터 관리
export const initialState = {
  nickname: '오지민',
  userSince: '2024.05.01',

  teams: [
    { id: 1, name: '1번 팀', role: '팀장' },
    { id: 2, name: '2번 팀', role: '팀원' },
    { id: 3, name: '3번 팀', role: '팀원' },
    { id: 4, name: '4번 팀', role: '팀원' },
  ],
  teamListOpen: true,
  activeTeam: null,          // null: 전체 게시판

  /* 모달 식별자: null | 'create-team' | 'create-team-success' |
   *            'join-team' | 'team-manage' | 'delete-confirm' |
   *            'leave-confirm' | 'edit-nickname' | 'search-detail'
   */
  modal: null,
  modalTeamTarget: null,     // 팀 관리/탈퇴 대상 팀 id
  createdTeamCode: 'Q7K2P9',

  // 상세 검색 필터
  searchKeyword: '',
  searchStackToggles: {},   // 'language-Java' -> bool 형태
  searchStatus: 'all',      // all | solved | unsolved
  searchHasImage: false,
}

const appReducer = (state, action) => {
  switch (action.type) {
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
      const newTeam = { id: Date.now(), name: action.payload.name, role: '팀장' }
      return {
        ...state,
        teams: [...state.teams, newTeam],
        modal: 'create-team-success',
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
    case APP.TOGGLE_SEARCH_HAS_IMAGE:
      return { ...state, searchHasImage: !state.searchHasImage }
    case APP.RESET_SEARCH_FILTERS:
      return { ...state, searchStackToggles: {}, searchStatus: 'all', searchHasImage: false, searchKeyword: '' }
    default:
      return state
  }
}

export default appReducer