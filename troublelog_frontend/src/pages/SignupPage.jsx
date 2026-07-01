import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { SIGNUP } from '../constants/actionTypes.js'
import { authCheckEmail, authEmail, checkNickname, signup } from '../api/authApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { onlyDigits } from '../util/inputFilters.js'
import { isValidPassword } from '../util/inputFilters.js'

const initialState = {
  email: '',
  emailCode: '',
  showCodeSection: false,  // 인증 코드 입력 섹션 표시 여부
  emailVerified: false,    // 이메일 인증 완료 여부
  password: '',
  passwordConfirm: '',
  nickname: '',
  nicknameChecked: false,   // 닉네임 중복확인 완료 여부
  emailError: '',          // 이메일 형식 오류 메시지
  emailCodeError: '',      // 이메일 코드 미입력 메시지
  passwordError: '',       // 비밀번호 불일치 오류 메시지
  error: '',               // 일반 오류 메시지
}

function reducer(state, action) {
  switch (action.type) {
    case SIGNUP.SET_FIELD: {
      const base = { ...state, error: '', [action.field]: action.value, }

      // 이메일이 바뀌면 인증 상태 초기화 (재인증 필요)
      if (action.field === 'email') {
        return { ...base, emailCode: '', emailVerified: false, showCodeSection: false }
      }
      // 닉네임이 바뀌면 중복확인 초기화 (재확인 필요)
      if (action.field === 'nickname') {
        return { ...base, nicknameChecked: false }
      }

      return base
    }
    case SIGNUP.SET_ERROR:
      return { ...state, error: action.payload }
    case SIGNUP.SHOW_CODE:
      return { ...state, showCodeSection: true }
    case SIGNUP.SET_VERIFIED:
      return { ...state, emailVerified: action.payload }
    case SIGNUP.SET_NICKNAME_CHECKED:
      return { ...state, nicknameChecked: action.payload }
    default:
      return state
  }
}

function SignupPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: SIGNUP.SET_FIELD, field, value: e.target.value })

  const passwordInvalid = state.password.length > 0 && !isValidPassword(state.password)
  const passwordMismatch = state.passwordConfirm.length > 0 && state.password !== state.passwordConfirm
  const canSubmit = state.emailVerified && state.nicknameChecked && isValidPassword(state.password) && !passwordMismatch

  async function handleSendCode() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(state.email)) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'emailError', value: '올바른 이메일 형식으로 입력하세요.' })
      return
    }

    await requestHandler(() => authEmail({ email: state.email }), {
      onSuccess: () => {
        dispatch({ type: SIGNUP.SET_FIELD, field: 'emailError', value: '' })
        dispatch({ type: SIGNUP.SHOW_CODE })
      },
      onFail: (message) => dispatch({ type: SIGNUP.SET_ERROR, payload: message }),
      fallbackMessage: '이메일 인증 중 에러가 발생했습니다.',
    })
  }

  async function handleAuthCode() {
    if (!state.emailCode) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'emailCodeError', value: '인증 코드를 입력하세요.' })
      return
    }

    await requestHandler(() => authCheckEmail({ email: state.email, code: state.emailCode }), {
      onSuccess: () => {
        dispatch({ type: SIGNUP.SET_FIELD, field: 'emailCodeError', value: '' })
        dispatch({ type: SIGNUP.SET_VERIFIED, payload: true })
      },
      onFail: (message) => dispatch({ type: SIGNUP.SET_ERROR, payload: message }),
      fallbackMessage: '인증 코드 확인 중 에러가 발생했습니다.',
    })
  }

  async function handleCheckNickname() {
    if (!state.nickname) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'error', value: '닉네임을 입력하세요.' })
      return
    }

    await requestHandler(() => checkNickname(state.nickname), {
      onSuccess: (available, res) => {
        if (available) {
          dispatch({ type: SIGNUP.SET_FIELD, field: 'error', value: '' })
          dispatch({ type: SIGNUP.SET_NICKNAME_CHECKED, payload: true })
        } else {
          dispatch({ type: SIGNUP.SET_ERROR, payload: res.message })
        }
      },
      onFail: (message) => dispatch({ type: SIGNUP.SET_ERROR, payload: message }),
      fallbackMessage: '닉네임 중복 확인 중 에러가 발생했습니다.',
    })
  }

  async function handleSignup() {
    if (!isValidPassword(state.password)) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'passwordError', value: '비밀번호는 영문, 숫자, 특수문자를 모두 포함해 8자 이상이어야 합니다.' })
      return
    }

    if (state.password !== state.passwordConfirm) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'passwordError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }

    await requestHandler(() => signup({ email: state.email, password: state.password, nickname: state.nickname, verificationCode: state.emailCode}), {
      onSuccess: () => navigate('/'),
      onFail: (message) => dispatch({ type: SIGNUP.SET_ERROR, payload: message }),
      fallbackMessage: '회원가입 중 에러가 발생했습니다.',
    })
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-logo">
          <span className="prompt">&gt;_</span> 트러블로그
        </div>
        <div className="auth-tabs">
          <button className="auth-tab" onClick={() => navigate('/login')}>로그인</button>
          <button className="auth-tab active">회원가입</button>
        </div>

        {/* 이메일 (아이디) + 인증 요청 */}
        <div className="form-group">
          <div className="input-row">
            <input
              className="input"
              type="email"
              placeholder="아이디 입력 (이메일 형식)"
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
            <div className="input-row">
              <input
                className={`input${state.emailVerified ? ' input--success' : ''}`}
                placeholder="이메일 인증 코드"
                value={state.emailCode}
                onChange={e => dispatch({
                  type: SIGNUP.SET_FIELD,
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
                onClick={handleAuthCode}
                disabled={state.emailVerified}
              >
                {state.emailVerified ? '인증 완료' : '인증 확인'}
              </button>
            </div>
            {state.emailCodeError && <div className="error-msg">{state.emailCodeError}</div>}
          </div>
        )}

        {/* 비밀번호 */}
        <div className="form-group">
          <input
            className={`input${passwordInvalid ? ' input--error' : ''}`}
            type="password"
            placeholder="비밀번호 입력"
            value={state.password}
            onChange={set('password')}
          />
          <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
          {passwordInvalid && (
            <div className="error-msg">영문, 숫자, 특수문자를 모두 포함해 8자 이상 입력하세요.</div>
          )}
        </div>

        {/* 비밀번호 확인 */}
        <div className="form-group">
          <input
            className={`input${passwordMismatch ? ' input--error' : ''}`}
            type="password"
            placeholder="비밀번호 확인"
            value={state.passwordConfirm}
            onChange={set('passwordConfirm')}
          />
          {(passwordMismatch || state.passwordError) && (
            <div className="error-msg">
              {state.passwordError || '비밀번호 확인이 일치하지 않습니다.'}
            </div>
          )}
        </div>

        {/* 닉네임 + 중복 확인 */}
        <div className="form-group">
          <div className="input-row">
            <input
              className="input"
              placeholder="닉네임 입력"
              value={state.nickname}
              onChange={set('nickname')}
            />
            <button className="btn btn-ghost btn-sm" onClick={handleCheckNickname}>중복확인</button>
          </div>
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <button className="btn btn-primary btn-block" onClick={handleSignup} disabled={!canSubmit}>회원가입</button>

        <div className="auth-foot">
          이미 계정이 있으신가요? <button onClick={() => navigate('/login')}>로그인</button>
        </div>
      </div>
    </div>
  )
}

export default SignupPage
