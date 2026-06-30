import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'

const initialState = { email: '', password: '', passwordConfirm: '', nickname: '' }

function reducer(state, action) {
  switch (action.type) {
    case 'SET_FIELD': return { ...state, [action.field]: action.value }
    default: return state
  }
}

function SignupPage() {
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(reducer, initialState)
  const set = field => e => dispatch({ type: 'SET_FIELD', field, value: e.target.value })

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

        <div className="form-group">
          <div className="input-row">
            <input className="input" type="email" placeholder="이메일 입력 (아이디로 사용)" value={state.email} onChange={set('email')} />
            <button className="btn btn-ghost btn-sm">중복확인</button>
          </div>
        </div>
        <div className="form-group">
          <input className="input" type="password" placeholder="비밀번호 입력 (8자 이상, 영문+숫자+특수문자)" value={state.password} onChange={set('password')} />
        </div>
        <div className="form-group">
          <input className="input" type="password" placeholder="비밀번호 확인" value={state.passwordConfirm} onChange={set('passwordConfirm')} />
        </div>
        <div className="form-group">
          <div className="input-row">
            <input className="input" placeholder="닉네임 입력" value={state.nickname} onChange={set('nickname')} />
            <button className="btn btn-ghost btn-sm">중복확인</button>
          </div>
        </div>

        {/* TODO: API 연동 - axiosInstance.post('/auth/signup', { ...state }) */}
        <button className="btn btn-primary btn-block" onClick={() => navigate('/login')}>회원가입</button>

        <div className="auth-foot">
          이미 계정이 있으신가요? <button onClick={() => navigate('/login')}>로그인</button>
        </div>
      </div>
    </div>
  )
}

export default SignupPage