// 리듀서별 action type 상수 - string literal 오타 방지 및 중앙 관리
export const APP = {
  SET_NICKNAME:            'APP/SET_NICKNAME',
  TOGGLE_TEAM_LIST:        'APP/TOGGLE_TEAM_LIST',
  SET_ACTIVE_TEAM:         'APP/SET_ACTIVE_TEAM',
  OPEN_MODAL:              'APP/OPEN_MODAL',
  CLOSE_MODAL:             'APP/CLOSE_MODAL',
  ADD_TEAM:                'APP/ADD_TEAM',
  JOIN_TEAM:               'APP/JOIN_TEAM',
  REMOVE_TEAM:             'APP/REMOVE_TEAM',
  LEAVE_TEAM:              'APP/LEAVE_TEAM',
  SET_SEARCH_KEYWORD:      'APP/SET_SEARCH_KEYWORD',
  TOGGLE_SEARCH_STACK:     'APP/TOGGLE_SEARCH_STACK',
  SET_SEARCH_STATUS:       'APP/SET_SEARCH_STATUS',
  RESET_SEARCH_FILTERS:    'APP/RESET_SEARCH_FILTERS',
  CLEAR_USER:              'APP/CLEAR_USER',  // 로그아웃 시 사용자 정보 초기화

  // API 연동
  SET_USER:                'APP/SET_USER',    // 로그인 사용자 정보 세팅
  SET_TEAMS:               'APP/SET_TEAMS',   // 팀 목록 세팅
  SET_GLOBAL_LOADING:      'APP/SET_GLOBAL_LOADING', // axios 요청 진행 중 전역 로딩 오버레이

  // 전역 에러 모달
  SHOW_ERROR:              'APP/SHOW_ERROR', // 전역 에러 모달 표시
  HIDE_ERROR:              'APP/HIDE_ERROR', // 전역 에러 모달 닫기

  SET_TECH_STACKS:         'APP/SET_TECH_STACKS', // 기술 스택 목록 세팅 (작성/검색 공용)
}

export const QLIST = {
  SET_SORT:          'QLIST/SET_SORT',
  TOGGLE_TAG_FILTER: 'QLIST/TOGGLE_TAG_FILTER',
  SET_STATUS_FILTER: 'QLIST/SET_STATUS_FILTER',
  RESET_FILTERS:     'QLIST/RESET_FILTERS',
  SET_PAGE:          'QLIST/SET_PAGE',
  SET_KEYWORD:       'QLIST/SET_KEYWORD',   // 검색 실행(Enter) 시 확정된 검색어를 반영

  // API 연동
  SET_LOADING:       'QLIST/SET_LOADING',   // 목록/검색 API 요청 시작
  SET_POSTS:         'QLIST/SET_POSTS',     // 목록/검색 API 요청 성공
  SET_ERROR:         'QLIST/SET_ERROR',     // 목록/검색 API 요청 실패
  APPLY_FILTERS:     'QLIST/APPLY_FILTERS', // 전역 검색 조건(검색어+상태+스택)을 한 번에 확정 반영
}

export const QDETAIL = {
  TOGGLE_POST_LIKE:   'QDETAIL/TOGGLE_POST_LIKE',
  ACCEPT_ANSWER:      'QDETAIL/ACCEPT_ANSWER',
  TOGGLE_ANSWER_LIKE: 'QDETAIL/TOGGLE_ANSWER_LIKE',
  SET_COMMENT_INPUT:  'QDETAIL/SET_COMMENT_INPUT',

  // API 연동
  SET_QUESTION:       'QDETAIL/SET_QUESTION',
  SET_ANSWERS:        'QDETAIL/SET_ANSWERS',
  SET_ERROR:          'QDETAIL/SET_ERROR',
  SET_LIKE:           'QDETAIL/SET_LIKE',           // 좋아요 등록/취소 API 응답 반영
  SET_DELETE_CONFIRM: 'QDETAIL/SET_DELETE_CONFIRM', // 게시글 삭제 확인 모달 열기/닫기
}

export const WRITE = {
  SET_FIELD:     'WRITE/SET_FIELD',
  SET_VISIBILITY:'WRITE/SET_VISIBILITY',
  TOGGLE_STACK:  'WRITE/TOGGLE_STACK',
  ADD_IMAGE:     'WRITE/ADD_IMAGE',
  REMOVE_IMAGE:  'WRITE/REMOVE_IMAGE',
  RESET:         'WRITE/RESET',
  SET_ERROR:     'WRITE/SET_ERROR', // 폼 유효성/제출 실패 에러 메시지
}

export const MYPAGE = {
  SET_TAB:  'MYPAGE/SET_TAB',
  SET_PAGE: 'MYPAGE/SET_PAGE',

  // API 연동
  SET_LOADING: 'MYPAGE/SET_LOADING', // 내 질문 목록 요청 시작
  SET_DATA:    'MYPAGE/SET_DATA',
  SET_ERROR:   'MYPAGE/SET_ERROR',
}

export const LOGIN = {
  SET_FIELD: 'LOGIN/SET_FIELD',
  SET_ERROR: 'LOGIN/SET_ERROR',
}

export const SIGNUP = {
  SET_FIELD:    'SIGNUP/SET_FIELD',
  SET_ERROR:    'SIGNUP/SET_ERROR',
  SHOW_CODE:    'SIGNUP/SHOW_CODE',    // 이메일 인증 코드 입력 섹션 표시
  SET_VERIFIED: 'SIGNUP/SET_VERIFIED', // 이메일 인증 완료 여부 세팅
  SET_NICKNAME_CHECKED: 'SIGNUP/SET_NICKNAME_CHECKED',
}

export const CHANGE_PASS = {
  SET_FIELD:    'CHANGE_PASS/SET_FIELD',
  SET_ERROR:    'CHANGE_PASS/SET_ERROR',
  SHOW_CODE:    'CHANGE_PASS/SHOW_CODE',    // 이메일 인증 코드 입력 섹션 표시
  SET_VERIFIED: 'CHANGE_PASS/SET_VERIFIED', // 이메일 인증 완료 여부 세팅
}

export const EDIT_PROFILE = {
  SET_TAB:              'EDIT_PROFILE/SET_TAB',              // 닉네임/비밀번호 탭 전환
  SET_FIELD:            'EDIT_PROFILE/SET_FIELD',
  SET_ERROR:            'EDIT_PROFILE/SET_ERROR',
  SHOW_CODE:            'EDIT_PROFILE/SHOW_CODE',            // 이메일 인증 코드 입력 섹션 표시
  SET_VERIFIED:         'EDIT_PROFILE/SET_VERIFIED',         // 이메일 인증 완료 여부
  SET_NICKNAME_CHECKED: 'EDIT_PROFILE/SET_NICKNAME_CHECKED', // 닉네임 중복확인 완료 여부
}