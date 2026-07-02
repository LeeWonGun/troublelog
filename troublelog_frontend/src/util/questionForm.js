import { toDisplayLanguage } from '../constants/languageOptions.js'

/**
 * 게시글 작성/수정 폼 공통 유틸 (QuestionCreatePage / QuestionEditPage 공용)
 */

// 유효성 검증 - 실패 시 에러 메시지, 통과 시 null 반환
export const validateQuestionForm = (state) => {
  // 제목/내용 필수 (공백만 입력한 경우도 미입력으로 처리)
  if (!state.title.trim() || !state.situation.trim()) {
    return '제목과 내용은 필수 입력 항목입니다.'
  }
  // TEAM 공개인데 팀 미선택
  if (state.visibility === 'TEAM' && !state.selectedTeamId) {
    return '소속된 팀 중 하나를 고르세요.'
  }
  return null
}

// writeReducer 상태 -> 백엔드 QuestionCreateRequest/QuestionUpdateRequest 필드 매핑
export const buildQuestionPayload = (state) => {
  const hasCode = state.errorCode.trim().length > 0

  return {
    title: state.title.trim(),
    content: state.situation.trim(),

    // 코드 입력이 없으면 언어만 선택돼 있어도 보내지 않는다 (백엔드가 code 기준으로 조합)
    codeLanguage: hasCode ? state.errorLanguage || null : null,
    code: hasCode ? state.errorCode : null,
    errorMessage: state.errorMessage.trim() || null,
    tried: state.triedMethods.trim() || null,
    visibility: state.visibility,

    // PUBLIC이면 teamId를 보내지 않는다
    teamId: state.visibility === 'TEAM' ? Number(state.selectedTeamId) : null,

    // stackToggles: { techStackId: bool } -> 선택된 id 배열
    techStackIds: Object.entries(state.stackToggles)
      .filter(([, on]) => on)
      .map(([id]) => Number(id)),
  }
}

// 백엔드 QuestionDetailResponse -> writeReducer 폼 상태 매핑 (수정 페이지 pre-fill용)
// buildQuestionPayload의 역방향 변환이므로 두 함수는 항상 짝으로 유지한다.
export const buildFormStateFromDetail = (detail) => ({
  title: detail.title ?? '',
  situation: detail.content ?? '',
  // 서버는 소문자 언어 태그('java')를 내려주므로 셀렉트 옵션값('Java')으로 역매핑
  errorLanguage: toDisplayLanguage(detail.codeLanguage),
  errorCode: detail.code ?? '',
  errorMessage: detail.errorMessage ?? '',
  triedMethods: detail.tried ?? '',
  visibility: detail.visibility ?? 'PUBLIC',
  // select value는 문자열이므로 String 변환 (PUBLIC이면 teamId가 null)
  selectedTeamId: detail.teamId != null ? String(detail.teamId) : '',
  // techStacks: [{ techStackId, name, category }] -> { techStackId: true }
  stackToggles: Object.fromEntries(
    (detail.techStacks ?? []).map(stack => [stack.techStackId, true]),
  ),
})