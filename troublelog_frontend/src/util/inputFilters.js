// 입력 필드 값 실시간 필터링 유틸

// 숫자만 입력받아야 하는 필드에서 재사용.
export const onlyDigits = (value) => value.replace(/\D/g, '')

// 영문/숫자/특수문자를 각 1개 이상 포함하고 8자 이상. PW 검증
export const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/
export const isValidPassword = (value) => PASSWORD_PATTERN.test(value)