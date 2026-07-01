import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { CHANGE_PASS } from '../constants/actionTypes'
import { sendPasswordResetCode, verifyPasswordResetCode, resetPasswordByEmail } from '../api/authApi'
import { requestHandler } from '../util/requestHandler'
import { onlyDigits } from '../util/inputFilters'
import { isValidPassword } from '../util/inputFilters.js'

const initialState = {
  email: '',
  emailCode: '',
  showCodeSection: false,  // 인증 코드 입력 섹션 표시 여부
  emailVerified: false,    // 이메일 인증 완료 여부
  newPassword: '',
  newPasswordConfirm: '',
  emailError: '',
  emailCodeError: '',      // 인증 코드 오류 메시지
  passwordError: '',
  error: '',
}

function reducer(state, action) {
  switch (action.type) {
    case CHANGE_PASS.SET_FIELD: {
      const base = { ...state, error: '', [action.field]: action.value, }

      // 이메일이 바뀌면 인증 상태 전체 초기화 (재인증 필요)
      if (action.field === 'email') {
        return { ...base, emailCode: '', emailVerified: false, showCodeSection: false }
      }

      return base
    }
    case CHANGE_PASS.SET_ERROR:
      return { ...state, error: action.payload }
    case CHANGE_PASS.SHOW_CODE:
      return { ...state, showCodeSection: true }
    case CHANGE_PASS.SET_VERIFIED:
      return { ...state, emailVerified: action.payload }
    default:
      return state
  }
}

function ResetPasswordPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: CHANGE_PASS.SET_FIELD, field, value: e.target.value })

  const newPasswordInvalid = state.newPassword.length > 0 && !isValidPassword(state.newPassword)
  const newPasswordMismatch = state.newPasswordConfirm.length > 0 && state.newPassword !== state.newPasswordConfirm

  // "비밀번호 변경" 버튼 활성화 조건: 이메일 인증 성공 + 새 비밀번호 정책 통과 + 확인값 일치
  const canSubmit = state.emailVerified && isValidPassword(state.newPassword) && !newPasswordMismatch

  // 이메일 인증 코드 발송 요청
  async function handleSendCode() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(state.email)) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'emailError', value: '올바른 이메일 형식으로 입력하세요.' })
      return
    }

    await requestHandler(() => sendPasswordResetCode({ email: state.email }), {
      onSuccess: () => {
        dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'emailError', value: '' })
        dispatch({ type: CHANGE_PASS.SHOW_CODE })
      },
      onFail: (message) => dispatch({ type: CHANGE_PASS.SET_ERROR, payload: message }),
      fallbackMessage: '이메일 인증 중 에러가 발생했습니다.',
    })
  }

  // 이메일 인증 코드 확인
  async function handleVerifyCode() {
    if (!state.emailCode) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'emailCodeError', value: '인증 코드를 입력하세요.' })
      return
    }

    await requestHandler(() => verifyPasswordResetCode({ email: state.email, code: state.emailCode }), {
      onSuccess: () => {
        dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'emailCodeError', value: '' })
        dispatch({ type: CHANGE_PASS.SET_VERIFIED, payload: true })
      },
      onFail: (message) => dispatch({ type: CHANGE_PASS.SET_ERROR, payload: message }),
      fallbackMessage: '인증 코드 확인 중 에러가 발생했습니다.',
    })
  }

  async function handleResetPassword() {
    if (!state.emailVerified) {
      dispatch({ type: CHANGE_PASS.SET_ERROR, payload: '이메일 인증을 먼저 완료해 주세요.' })
      return
    }

    if (!isValidPassword(state.newPassword)) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'passwordError', value: '비밀번호는 영문, 숫자, 특수문자를 모두 포함해 8자 이상이어야 합니다.' })
      return
    }

    if (state.newPassword !== state.newPasswordConfirm) {
      dispatch({ type: CHANGE_PASS.SET_FIELD, field: 'passwordError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }

    await requestHandler(() => resetPasswordByEmail({
      email: state.email,
      verificationCode: state.emailCode,
      newPassword: state.newPassword,
    }), {
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
                className={`input${state.emailVerified ? ' input--success' : ''}`}
                placeholder="이메일 인증 코드"
                value={state.emailCode}
                onChange={e => dispatch({
                  type: CHANGE_PASS.SET_FIELD,
                  field: 'emailCode',
                  value: onlyDigits(e.target.value).slice(0, 6),
                })}
                inputMode="numeric"
                pattern="[0-9]*"
                maxLength={6}
                disabled={state.emailVerified}  // 인증 완료 후 재입력 방지
              />
               <button
                className="btn btn-ghost btn-sm"
                onClick={handleVerifyCode}
                disabled={state.emailVerified}
              >
                {state.emailVerified ? '인증 완료' : '인증 확인'}
              </button>
            </div>
          </div>
        )}

        {/* 새 비밀번호 */}
        <div className="form-group">
          <label>새 비밀번호</label>
          <input
            className={`input${newPasswordInvalid ? ' input--error' : ''}`}
            type="password"
            placeholder="새 비밀번호 입력"
            value={state.newPassword}
            onChange={set('newPassword')}
          />
          <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
          {newPasswordInvalid && (
            <div className="error-msg">영문, 숫자, 특수문자를 모두 포함해 8자 이상 입력하세요.</div>
          )}
        </div>

        {/* 새 비밀번호 확인 */}
        <div className="form-group">
          <label>새 비밀번호 확인</label>
          <input
            className={`input${newPasswordMismatch ? ' input--error' : ''}`}
            type="password"
            placeholder="새 비밀번호 확인"
            value={state.newPasswordConfirm}
            onChange={set('newPasswordConfirm')}
          />
          {(newPasswordMismatch || state.passwordError) && (
            <div className="error-msg">
              {state.passwordError || '비밀번호 확인이 일치하지 않습니다.'}
            </div>
          )}
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <button
          className="btn btn-primary btn-block"
          onClick={handleResetPassword}
          disabled={!canSubmit}
        >
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