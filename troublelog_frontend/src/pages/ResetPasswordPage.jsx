import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { CHANGE_PASS } from '../constants/actionTypes'
import { authEmail } from '../api/authApi'
import { updatePassword } from '../api/userApi'
import { requestHandler } from '../util/requestHandler'

const initialState = {
  email: '',
  emailCode: '',
  showCodeSection: false, // 인증 코드 입력 섹션 표시 여부
  emailVerified: false,    // 이메일 인증 완료 여부
  newPassword: '',
  newPasswordConfirm: '',
  emailError: '',
  passwordError: '',
  error: '',
}

function reducer(state, action) {
  switch (action.type) {
    case CHANGE_PASS.SET_FIELD: {
      const base = { ...state, error: '', [action.field]: action.value, }

      // 이메일이 바뀌면 인증 상태 초기화 (재인증 필요)
      if (action.field === 'email') {
        return { ...base, emailVerified: false, showCodeSection: false }
      }

      return base
    }
    case CHANGE_PASS.SET_ERROR:
      return { ...state, error: action.payload }
    case CHANGE_PASS.SHOW_CODE:
      return { ...state, showCodeSection: true }
    default:
      return state
  }
}

function ResetPasswordPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: 'SET_FIELD', field, value: e.target.value })

  // 이메일 인증 코드 발송 요청
  async function handleSendCode() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(state.email)) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'emailError', value: '올바른 이메일 형식으로 입력하세요.' })
      return
    }

    await requestHandler(() => authEmail({ userId: state.email, password: state.password }), {
      onSuccess: () => dispatch({ type: CHANGE_PASS.SHOW_CODE }),
      onFail: (message) => dispatch({ type: CHANGE_PASS.SET_ERROR, payload: message }),
      fallbackMessage: '이메일 인증 중 에러가 발생했습니다.',
    })
  }

  async function handleResetPassword() {
    if (state.newPassword !== state.newPasswordConfirm) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'passwordError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }

    await requestHandler(() => updatePassword({ newPassword: state.newPassword }), {
      onSuccess: () => navigate('/login'),
      onFail: (message) => dispatch({ type: CHANGE_PASS.SET_ERROR, payload: message }),
      fallbackMessage: '비밀번호 변경 중 에러가 발생했습니다.',
    })
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-logo">
          <span className="prompt">&gt;_</span> 트러블로그
        </div>
        <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 18 }}>비밀번호 변경</div>

        {/* 이메일 입력 + 인증 요청 */}
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

        {/* 인증 코드 입력 — 인증 요청 후 노출 */}
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

        {/* 새 비밀번호 */}
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

        {/* 새 비밀번호 확인 */}
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
