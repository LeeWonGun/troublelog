// 오류 코드 언어 선택 옵션 (게시글 작성/수정 공통 - SSOT)
export const LANG_OPTIONS = ['Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin', 'Go', 'SQL', '기타']

// 화면 표시용 언어명 -> Monaco 에디터 언어 ID 매핑
// 매핑에 없는 값('기타', 미선택)은 plaintext로 처리해 하이라이팅 오류를 방지
export const MONACO_LANGUAGE_MAP = {
  Java: 'java',
  Python: 'python',
  JavaScript: 'javascript',
  TypeScript: 'typescript',
  Kotlin: 'kotlin',
  Go: 'go',
  SQL: 'sql',
}

// 서버 저장값 -> 화면 표시 언어명 역매핑 (수정 페이지 pre-fill용)
export const toDisplayLanguage = (serverLanguage) => {
  if (!serverLanguage) return ''

  const matched = LANG_OPTIONS.find(
    option => option.toLowerCase() === serverLanguage.toLowerCase(),
  )

  // 옵션에 없는 언어 태그는 '기타'로 표시 (코드 내용 자체는 그대로 유지됨)
  return matched ?? '기타'
}