import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { LOGIN } from '../constants/actionTypes.js'
import { login } from '../api/authApi.js'
import { requestHandler } from '../util/requestHandler.js'

const initialState = { id: '', password: '', error: '' }

function reducer(state, action) {
  switch (action.type) {
    case LOGIN.SET_FIELD: return { ...state, [action.field]: action.value, error: '' }
    case LOGIN.SET_ERROR: return { ...state, error: action.payload }
    default: return state
  }
}

function LoginPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)

  const set = (field) => (e) =>
    dispatch({ type: LOGIN.SET_FIELD, field, value: e.target.value })

  async function handleLogin() {
    if (!state.id) {
      dispatch({ type: LOGIN.SET_ERROR, payload: '아이디를 입력해 주세요' })
      return
    }
    if (!state.password) {
      dispatch({ type: LOGIN.SET_ERROR, payload: '비밀번호를 입력해 주세요' })
      return
    }

    await requestHandler(() => login({ email: state.id, password: state.password }), {
      onSuccess: () => navigate('/'),
      onFail: () => dispatch({ type: LOGIN.SET_ERROR, payload: '이메일 또는 비밀번호가 올바르지 않습니다.' }),
    })
  }

  async function handleGoogleLogin() {
    let baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    window.location.href = `${baseUrl}/oauth2/authorization/google`
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="auth-logo">
          <span className="prompt">&gt;_</span> 트러블로그
        </div>
        <div className="auth-tabs">
          <button className="auth-tab active">로그인</button>
          <button className="auth-tab" onClick={() => navigate('/signup')}>회원가입</button>
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <div className="form-group">
          <input
            className="input" type="email" placeholder="아이디 입력"
            value={state.id}
            onChange={set('id')}
          />
        </div>
        <div className="form-group">
          <input
            className="input" type="password" placeholder="비밀번호 입력"
            value={state.password}
            onChange={set('password')}
            onKeyDown={e => e.key === 'Enter' && handleLogin()}
          />
        </div>

        <button className="btn btn-primary btn-block" onClick={handleLogin}>로그인</button>

        <div className="auth-divider">간편 로그인</div>
        <button className="btn-social" onClick={handleGoogleLogin}>
          <svg width="16" height="16" viewBox="0 0 48 48">
            <path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.1 8 3l5.7-5.7C34.5 6 29.5 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.7-.4-3.5z" />
            <path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.5 16 18.9 13 24 13c3.1 0 5.9 1.1 8 3l5.7-5.7C34.5 6 29.5 4 24 4c-7.7 0-14.3 4.4-17.7 10.7z" />
            <path fill="#4CAF50" d="M24 44c5.3 0 10.1-1.8 13.8-5l-6.4-5.4C29.4 35.4 26.8 36 24 36c-5.3 0-9.7-3.3-11.3-8l-6.6 5C9.6 39.6 16.2 44 24 44z" />
            <path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-.8 2.2-2.2 4.1-4 5.6l6.4 5.4C41.5 36 44 30.6 44 24c0-1.3-.1-2.7-.4-3.5z" />
          </svg>
          Google 로그인
        </button>

        <div className="auth-links">
          <button onClick={() => navigate('/reset-password')}>비밀번호를 잊었습니다.</button>
        </div>

        <div className="auth-foot">
          회원이 아니신가요? <button onClick={() => navigate('/signup')}>회원가입</button>
        </div>
      </div>
    </div>
  )
}

export default LoginPage