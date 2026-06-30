import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'

const initialState = {
  email: '',
  emailCode: '',
  showCodeSection: false, // 인증 코드 입력 섹션 표시 여부
  newPassword: '',
  newPasswordConfirm: '',
  emailError: '',
  passwordError: '',
  error: '',
}

function reducer(state, action) {
  switch (action.type) {
    case 'SET_FIELD':
      return { ...state, [action.field]: action.value, emailError: '', passwordError: '', error: '' }
    case 'SET_EMAIL_ERROR':
      return { ...state, emailError: action.payload }
    case 'SET_PASSWORD_ERROR':
      return { ...state, passwordError: action.payload }
    case 'SET_ERROR':
      return { ...state, error: action.payload }
    case 'SHOW_CODE':
      return { ...state, showCodeSection: true }
    default:
      return state
  }
}

/**
 * P-FE-LI-15 비밀번호 재설정 페이지
 * 로그인 페이지의 "비밀번호를 잊었습니다." 링크에서 진입
 */
function ResetPasswordPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: 'SET_FIELD', field, value: e.target.value })

  // 이메일 인증 코드 발송 요청
  function handleSendCode() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(state.email)) {
      dispatch({ type: 'SET_EMAIL_ERROR', payload: '이메일 형식이 아닙니다.' })
      return
    }
    // TODO: API 연동 - axiosInstance.post('/auth/password-reset/send-code', { email: state.email })
    dispatch({ type: 'SHOW_CODE' })
  }

  async function handleResetPassword() {
    if (state.newPassword !== state.newPasswordConfirm) {
      dispatch({ type: 'SET_PASSWORD_ERROR', payload: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }
    // TODO: API 연동 - axiosInstance.post('/auth/password-reset/confirm', { email, code, newPassword })
    navigate('/login')
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-logo">
          <span className="prompt">&gt;_</span> 트러블로그
        </div>
        <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 18 }}>비밀번호 변경</div>

        {/* ① 이메일 입력 + 인증 요청 */}
        <div className="form-group">
          <label>아이디 (이메일 형식)</label>
          <div className="input-row">
            <input
              className="input"
              type="email"
              placeholder="이메일 입력"
              value={state.email}
              onChange={set('email')}
            />
            <button className="btn btn-ghost btn-sm" onClick={handleSendCode}>이메일 인증</button>
          </div>
          {state.emailError && <div className="error-msg">{state.emailError}</div>}
        </div>

        {/* ② 인증 코드 입력 — 인증 요청 후 노출 */}
        {state.showCodeSection && (
          <div className="form-group">
            <label>이메일 인증 코드</label>
            <div className="input-row">
              <input
                className="input"
                placeholder="인증 코드 입력"
                value={state.emailCode}
                onChange={set('emailCode')}
              />
              <button className="btn btn-ghost btn-sm">인증 확인</button>
            </div>
          </div>
        )}

        {/* ③ 새 비밀번호 */}
        <div className="form-group">
          <label>새 비밀번호</label>
          <input
            className="input"
            type="password"
            placeholder="새 비밀번호 입력"
            value={state.newPassword}
            onChange={set('newPassword')}
          />
          <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
        </div>

        {/* ④ 새 비밀번호 확인 */}
        <div className="form-group">
          <label>새 비밀번호 확인</label>
          <input
            className="input"
            type="password"
            placeholder="새 비밀번호 확인"
            value={state.newPasswordConfirm}
            onChange={set('newPasswordConfirm')}
          />
          {state.passwordError && <div className="error-msg">{state.passwordError}</div>}
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <button className="btn btn-primary btn-block" onClick={handleResetPassword}>
          비밀번호 변경
        </button>

        <div className="auth-foot">
          <button onClick={() => navigate('/login')}>← 로그인으로 돌아가기</button>
        </div>
      </div>
    </div>
  )
}

export default ResetPasswordPage

/*
 * [고려한 예외 처리 및 보안 사항]
 * - 이메일 형식 클라이언트 검증 후 서버 전송 (서버 측 재검증 필수)
 * - 인증 코드 발송 후 코드 섹션 노출 (초기엔 hidden)
 * - 비밀번호 확인 불일치 시 UI 오류 메시지 표시
 * - 실제 구현 시: 인증 코드 만료 시간(5분), Rate Limiting, 토큰 기반 재설정 등 서버 정책 적용 필요
 */
