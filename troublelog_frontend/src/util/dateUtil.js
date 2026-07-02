/**
 * 백엔드 LocalDateTime(ISO 문자열)을 "YYYY.MM.DD HH:mm" 형태로 변환한다.
 * 질문/답변/댓글 등 작성일시 표시에 공통으로 사용하기 위한 모듈이다.
 */
export const formatDateTime = (isoString) => {
  if (!isoString) return ''

  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return '' // 잘못된 날짜 문자열 방어

  const pad = (n) => String(n).padStart(2, '0')

  const y = date.getFullYear()
  const m = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const h = pad(date.getHours())
  const mi = pad(date.getMinutes())

  return `${y}.${m}.${d} ${h}:${mi}`
}

/**
 * 백엔드 LocalDateTime(ISO 문자열)을 "YYYY-MM-DD" 형태로 변환한다.
 * 목록/랭킹처럼 시간 없이 날짜만 표시할 때 사용한다.
 */
export const formatDate = (isoString) => {
  if (!isoString) return ''

  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return '' // 잘못된 날짜 문자열 방어

  const pad = (n) => String(n).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}