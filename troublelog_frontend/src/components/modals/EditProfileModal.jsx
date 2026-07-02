import { useRef, useState } from 'react'
import { APP } from '../../constants/actionTypes.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function EditProfileModal({ state, dispatch }) {
  const nicknameRef = useRef(null)
  const [activeTab, setActiveTab] = useState('nickname') // 'nickname' | 'password'
  const [showPwCode, setShowPwCode] = useState(false)    // 비밀번호 변경 탭 인증 코드 섹션

  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  // Google 소셜 로그인 사용자는 비밀번호 변경 불가
  const isGoogleUser = state.authProvider === 'GOOGLE'

  function saveNickname() {
    const val = nicknameRef.current?.value?.trim()
    if (val && val !== state.nickname) {
      dispatch({ type: APP.SET_NICKNAME, payload: val })
      close()
    }
  }

  return (
    <ModalOverlay onClose={close} wide>
      <h3>정보 수정</h3>

      {/* 탭 전환 */}
      <div className="modal-tabs">
        <button
          className={`modal-tab ${activeTab === 'nickname' ? 'active' : ''}`}
          onClick={() => setActiveTab('nickname')}
        >
          닉네임 변경
        </button>
        <button
          className={`modal-tab ${activeTab === 'password' ? 'active' : ''}`}
          onClick={() => setActiveTab('password')}
        >
          비밀번호 변경
        </button>
      </div>

      {/* 닉네임 변경 탭 */}
      {activeTab === 'nickname' && (
        <>
          <div className="field-id-readonly">아이디 : {state.email} (변경 불가)</div>
          <div className="form-group">
            <div className="input-row">
              <input
                className="input"
                placeholder="새 닉네임 입력"
                defaultValue={state.nickname}
                ref={nicknameRef}
              />
              <button className="btn btn-ghost btn-sm">중복확인</button>
            </div>
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button className="btn btn-primary" onClick={saveNickname}>닉네임 저장</button>
          </div>
        </>
      )}

      {/* 비밀번호 변경 탭 */}
      {activeTab === 'password' && (
        <>
          <div className="field-id-readonly">아이디 : {state.email} (변경 불가)</div>

          {isGoogleUser ? (
            <div className="google-pw-notice">
              구글 로그인 사용자는 비밀번호 변경이 불가능합니다.
            </div>
          ) : (
            <>
              <div className="form-group">
                <label>이메일 인증</label>
                <div className="input-row">
                  <input className="input" type="email" placeholder="이메일 입력" defaultValue={state.email} readOnly />
                  <button className="btn btn-ghost btn-sm" onClick={() => setShowPwCode(true)}>인증 요청</button>
                </div>
              </div>

              {showPwCode && (
                <div className="form-group">
                  <div className="input-row">
                    <input className="input" placeholder="인증 코드 입력" />
                    <button className="btn btn-ghost btn-sm">인증 확인</button>
                  </div>
                </div>
              )}

              <div className="form-group">
                <label>새 비밀번호</label>
                <input className="input" type="password" placeholder="새 비밀번호 입력" />
                <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
              </div>

              <div className="form-group">
                <label>새 비밀번호 확인</label>
                <input className="input" type="password" placeholder="새 비밀번호 확인" />
              </div>
            </>
          )}

          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            {!isGoogleUser && (
              <button className="btn btn-primary" onClick={close}>비밀번호 변경</button>
            )}
          </div>
        </>
      )}
    </ModalOverlay>
  )
}

export default EditProfileModal