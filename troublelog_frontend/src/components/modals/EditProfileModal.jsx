import { useReducer } from 'react'
import { APP, EDIT_PROFILE } from '../../constants/actionTypes.js'
import ModalOverlay from '../common/ModalOverlay.jsx'
import { updateNickname, updatePassword } from '../../api/userApi.js'
import { checkNickname, sendPasswordResetCode, verifyPasswordResetCode } from '../../api/authApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import { onlyDigits, isValidPassword } from '../../util/inputFilters.js'

const initialState = {
  activeTab: 'nickname',   // 'nickname' | 'password'

  // 닉네임 변경 탭
  nickname: '',            // 새 닉네임 입력값
  nicknameChecked: false,  // 중복확인 완료 여부
  nicknameError: '',       // 닉네임 필드 검증 오류 메시지

  // 비밀번호 변경 탭
  emailCode: '',
  showCodeSection: false,  // 인증 코드 입력 섹션 표시 여부
  emailVerified: false,    // 이메일 인증 완료 여부
  emailCodeError: '',

  oldPassword: '',
  oldPassError: '',

  password: '',
  passwordConfirm: '',
  passwordError: '',

  error: '',               // 일반 오류 메시지 (alert-banner)
}

function init(currentNickname) {
  return { ...initialState, nickname: currentNickname ?? '' }
}

function reducer(state, action) {
  switch (action.type) {
    case EDIT_PROFILE.SET_TAB: {
      if (state.activeTab === action.payload) return state
      return { ...init(action.nickname), activeTab: action.payload }
    }
    case EDIT_PROFILE.SET_FIELD: {
      const base = { ...state, error: '', [action.field]: action.value }
      if (action.field === 'nickname') {
        return { ...base, nicknameChecked: false, nicknameError: '' }
      }
      return base
    }
    case EDIT_PROFILE.SET_ERROR:
      return { ...state, error: action.payload }
    case EDIT_PROFILE.SHOW_CODE:
      return { ...state, showCodeSection: true }
    case EDIT_PROFILE.SET_VERIFIED:
      return { ...state, emailVerified: action.payload }
    case EDIT_PROFILE.SET_NICKNAME_CHECKED:
      return { ...state, nicknameChecked: action.payload }
    default:
      return state
  }
}

function EditProfileModal({ state: appState, dispatch: appDispatch }) {
  const [state, dispatch] = useReducer(reducer, appState.nickname, init)
  const set = field => e => dispatch({ type: EDIT_PROFILE.SET_FIELD, field, value: e.target.value })

  const close = () => appDispatch({ type: APP.CLOSE_MODAL })

  // Google 소셜 로그인 사용자는 비밀번호 변경 불가
  const isGoogleUser = appState.authProvider === 'GOOGLE'

  const passwordInvalid = state.password.length > 0 && !isValidPassword(state.password)
  const passwordMismatch = state.passwordConfirm.length > 0 && state.password !== state.passwordConfirm

  const canSaveNickname = state.nicknameChecked
  const canChangePassword =
    state.emailVerified && isValidPassword(state.password) && state.password === state.passwordConfirm

  // 닉네임 공통 검증 - 중복확인/저장 양쪽에서 사용
  function validChkNickname(nickname) {
    if (!nickname) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'nicknameError', value: '닉네임을 입력하세요.' })
      return false
    }
    if (nickname === appState.nickname) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'nicknameError', value: '이전과 다른 닉네임으로 설정하세요.' })
      return false
    }
    return true
  }

  async function checkNicknameFn() {
    const nickname = state.nickname.trim()
    if (!validChkNickname(nickname)) return

    await requestHandler(() => checkNickname(nickname), {
      onSuccess: (available, res) => {
        if (available) {
          dispatch({ type: EDIT_PROFILE.SET_NICKNAME_CHECKED, payload: true })
        } else {
          dispatch({ type: EDIT_PROFILE.SET_ERROR, payload: res.message })
        }
      },
      onFail: (message) => dispatch({ type: EDIT_PROFILE.SET_ERROR, payload: message }),
      fallbackMessage: '닉네임 중복 확인 중 에러가 발생했습니다.',
    })
  }

  async function updateNicknameFn() {
    const nickname = state.nickname.trim()

    if (!state.nicknameChecked || !validChkNickname(nickname)) return

    await requestHandler(() => updateNickname(nickname), {
      onSuccess: (data) => {
        appDispatch({ type: APP.SET_NICKNAME, payload: data.nickname })
        close()
      },
      onFail: (message) => console.warn('[EditProfileModal] 닉네임 변경 실패:', message),
      showGlobalError: true,
    })
  }

  async function handleSendCode() {
    await requestHandler(() => sendPasswordResetCode({ email: appState.email }), {
      onSuccess: () => dispatch({ type: EDIT_PROFILE.SHOW_CODE }),
      onFail: (message) => dispatch({ type: EDIT_PROFILE.SET_ERROR, payload: message }),
      fallbackMessage: '이메일 인증 요청 중 에러가 발생했습니다.',
    })
  }

  async function handleVerifyCode() {
    if (!state.emailCode) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'emailCodeError', value: '인증 코드를 입력하세요.' })
      return
    }

    await requestHandler(() => verifyPasswordResetCode({ email: appState.email, code: state.emailCode }), {
      onSuccess: () => {
        dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'emailCodeError', value: '' })
        dispatch({ type: EDIT_PROFILE.SET_VERIFIED, payload: true })
      },
      onFail: (message) => dispatch({ type: EDIT_PROFILE.SET_ERROR, payload: message }),
      fallbackMessage: '인증 코드 확인 중 에러가 발생했습니다.',
    })
  }

  async function updatePasswordFn() {
    if(!state.oldPassword) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'oldPassError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }
    if (!isValidPassword(state.password)) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'passwordError', value: '비밀번호는 영문, 숫자, 특수문자를 모두 포함해 8자 이상이어야 합니다.' })
      return
    }
    if (state.password !== state.passwordConfirm) {
      dispatch({ type: EDIT_PROFILE.SET_FIELD, field: 'passwordError', value: '비밀번호 확인이 일치하지 않습니다.' })
      return
    }

    await requestHandler(() => updatePassword({ currentPassword: state.oldPassword, newPassword: state.password }), {
      onSuccess: () => close(),
      onFail: (message) => dispatch({ type: EDIT_PROFILE.SET_ERROR, payload: message }),
      fallbackMessage: '비밀번호 변경 중 에러가 발생했습니다.',
    })
  }

  return (
    <ModalOverlay onClose={close} wide>
      <h3>정보 수정</h3>

      {/* 탭 전환 */}
      <div className="modal-tabs">
        <button
          className={`modal-tab ${state.activeTab === 'nickname' ? 'active' : ''}`}
          onClick={() => dispatch({ type: EDIT_PROFILE.SET_TAB, payload: 'nickname', nickname: appState.nickname })}
        >
          닉네임 변경
        </button>
        <button
          className={`modal-tab ${state.activeTab === 'password' ? 'active' : ''}`}
          onClick={() => dispatch({ type: EDIT_PROFILE.SET_TAB, payload: 'password', nickname: appState.nickname })}
        >
          비밀번호 변경
        </button>
      </div>

      {/* 닉네임 변경 탭 */}
      {state.activeTab === 'nickname' && (
        <>
          <div className="field-id-readonly">아이디 : {appState.email} (변경 불가)</div>
          <div className="form-group">
            <div className="input-row">
              <input
                className={`input${state.nicknameChecked ? ' input--success' : ''}`}
                placeholder="새 닉네임 입력"
                value={state.nickname}
                onChange={set('nickname')}
              />
              <button
                className="btn btn-ghost btn-sm"
                onClick={checkNicknameFn}
                disabled={state.nicknameChecked}
              >
                {state.nicknameChecked ? '확인 완료' : '중복확인'}
              </button>
            </div>
            {state.nicknameError && <div className="error-msg">{state.nicknameError}</div>}
            {state.nicknameChecked && <div className="copy-feedback" style={{ marginTop: 5, marginLeft: 5 }}>사용 가능한 닉네임입니다.</div>}
          </div>

          {state.error && <div className="alert-banner">{state.error}</div>}

          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button className="btn btn-primary" onClick={updateNicknameFn} disabled={!canSaveNickname}>
              닉네임 저장
            </button>
          </div>
        </>
      )}

      {/* 비밀번호 변경 탭 */}
      {state.activeTab === 'password' && (
        <>
          {isGoogleUser ? (
            <div className="google-pw-notice">
              구글 로그인 사용자는 비밀번호 변경이 불가능합니다.
            </div>
          ) : (
            <>
              <div className="form-group">
                <label>이메일 인증</label>
                <div className="input-row">
                  <input className="input" type="email" defaultValue={appState.email} readOnly />
                  <button
                    className="btn btn-ghost btn-sm"
                    onClick={handleSendCode}
                    disabled={state.emailVerified}
                  >
                    인증 요청
                  </button>
                </div>
              </div>

              {state.showCodeSection && (
                <div className="form-group">
                  <div className="input-row">
                    <input
                      className={`input${state.emailVerified ? ' input--success' : ''}`}
                      placeholder="인증 코드 입력"
                      value={state.emailCode}
                      onChange={e => dispatch({
                        type: EDIT_PROFILE.SET_FIELD,
                        field: 'emailCode',
                        value: onlyDigits(e.target.value).slice(0, 6),
                      })}
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={6}
                      disabled={state.emailVerified}
                    />
                    <button
                      className="btn btn-ghost btn-sm"
                      onClick={handleVerifyCode}
                      disabled={state.emailVerified}
                    >
                      {state.emailVerified ? '인증 완료' : '인증 확인'}
                    </button>
                  </div>
                  {state.emailCodeError && <div className="error-msg">{state.emailCodeError}</div>}
                </div>
              )}
              
              <div className="form-group">
                <label>현재 비밀번호</label>
                <input
                  className={`input`}
                  type="password"
                  placeholder="현재 비밀번호 입력"
                  value={state.oldPassword}
                  onChange={set('oldPassword')}
                />
                {state.oldPassError && (
                  <div className="error-msg">현재 비밀번호를 입력하세요.</div>
                )}
              </div>

              <div className="form-group">
                <label>새 비밀번호</label>
                <input
                  className={`input${passwordInvalid ? ' input--error' : ''}`}
                  type="password"
                  placeholder="새 비밀번호 입력"
                  value={state.password}
                  onChange={set('password')}
                />
                <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
                {passwordInvalid && (
                  <div className="error-msg">영문, 숫자, 특수문자를 모두 포함해 8자 이상 입력하세요.</div>
                )}
              </div>

              <div className="form-group">
                <label>새 비밀번호 확인</label>
                <input
                  className={`input${passwordMismatch ? ' input--error' : ''}`}
                  type="password"
                  placeholder="새 비밀번호 확인"
                  value={state.passwordConfirm}
                  onChange={set('passwordConfirm')}
                />
                {(passwordMismatch || state.passwordError) && (
                  <div className="error-msg">
                    {state.passwordError || '비밀번호 확인이 일치하지 않습니다.'}
                  </div>
                )}
              </div>
            </>
          )}

          {state.error && <div className="alert-banner">{state.error}</div>}

          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            {!isGoogleUser && (
              <button className="btn btn-primary" onClick={updatePasswordFn} disabled={!canChangePassword}>
                비밀번호 변경
              </button>
            )}
          </div>
        </>
      )}
    </ModalOverlay>
  )
}

export default EditProfileModal