import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { SIGNUP } from '../constants/actionTypes.js'
import { signup } from '../api/authApi.js'

const initialState = {
  email: '',
  emailCode: '',
  showCodeSection: false,  // 인증 코드 입력 섹션 표시 여부
  emailVerified: false,    // 이메일 인증 완료 여부
  password: '',
  passwordConfirm: '',
  nickname: '',
  emailError: '',          // 이메일 형식 오류 메시지
  passwordError: '',       // 비밀번호 불일치 오류 메시지
  error: '',               // 일반 오류 메시지
}

function reducer(state, action) {
  switch (action.type) {
    case SIGNUP.SET_FIELD:
      return { ...state, [action.field]: action.value, emailError: '', passwordError: '', error: '' }
    case SIGNUP.SET_ERROR:
      return { ...state, error: action.payload }
    case SIGNUP.SHOW_CODE:
      return { ...state, showCodeSection: true }
    case SIGNUP.SET_VERIFIED:
      return { ...state, emailVerified: action.payload }
    default:
      return state
  }
}

function SignupPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: SIGNUP.SET_FIELD, field, value: e.target.value })

  // TODO: 이메일 인증 코드 발송 요청 api 확인
  async function handleSendCode() {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(state.email)) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'emailError', value: '이메일 형식이 아닙니다.' })
      return
    }

    try {
      await checkEmail({ userId: state.email, password: state.password })
      dispatch({ type: SIGNUP.SHOW_CODE })
    } catch {
      dispatch({ type: LOGIN.SET_ERROR, payload: '이메일 또는 비밀번호가 올바르지 않습니다.' })
    }

  }

  async function handleSignup() {
    if (state.password !== state.passwordConfirm) {
      dispatch({ type: SIGNUP.SET_FIELD, field: 'passwordError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }

    try {
      await signup({ userId: state.email, password: state.password })
      navigate('/')
    } catch {
      dispatch({ type: LOGIN.SET_ERROR, payload: '이메일 또는 비밀번호가 올바르지 않습니다.' })
    }
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

        {/* ① 이메일 (아이디) + 인증 요청 */}
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

        {/* ② 인증 코드 입력 — 인증 요청 후 노출 */}
        {state.showCodeSection && (
          <div className="form-group">
            <div className="input-row">
              <input
                className="input"
                placeholder="이메일 인증 코드"
                value={state.emailCode}
                onChange={set('emailCode')}
              />
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => dispatch({ type: SIGNUP.SET_VERIFIED, payload: true })}
              >
                인증 확인
              </button>
            </div>
          </div>
        )}

        {/* ③ 비밀번호 */}
        <div className="form-group">
          <input
            className="input"
            type="password"
            placeholder="비밀번호 입력"
            value={state.password}
            onChange={set('password')}
          />
          <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
        </div>

        {/* ④ 비밀번호 확인 */}
        <div className="form-group">
          <input
            className="input"
            type="password"
            placeholder="비밀번호 확인"
            value={state.passwordConfirm}
            onChange={set('passwordConfirm')}
          />
          {state.passwordError && <div className="error-msg">{state.passwordError}</div>}
        </div>

        {/* ⑤ 닉네임 + 중복 확인 */}
        <div className="form-group">
          <div className="input-row">
            <input
              className="input"
              placeholder="닉네임 입력"
              value={state.nickname}
              onChange={set('nickname')}
            />
            <button className="btn btn-ghost btn-sm">중복확인</button>
          </div>
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <button className="btn btn-primary btn-block" onClick={handleSignup}>회원가입</button>

        <div className="auth-foot">
          이미 계정이 있으신가요? <button onClick={() => navigate('/login')}>로그인</button>
        </div>
      </div>
    </div>
  )
}

export default SignupPage
