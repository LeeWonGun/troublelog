import { useEffect, useRef, useState } from 'react'
import { useAppContext } from '../../context/AppContext.jsx'
import { APP } from '../../constants/actionTypes.js'
import { MODAL } from '../../constants/modalTypes.js'
import StackSelector from './StackSelector.jsx'
import { requestHandler } from '../../util/requestHandler.js'
import { createTeam, getMyTeams, getTeamByCode, joinTeam } from '../../api/teamApi.js'

function AppModals() {
  const { state, dispatch } = useAppContext()
  const { modal, modalTeamTarget, teams, createdTeamCode } = state

  const teamNameRef = useRef(null)
  const teamDescRef = useRef(null)
  const teamCodeRef = useRef(null)
  const nicknameRef = useRef(null)

  const [teamNameError, setTeamNameError] = useState(false)
  const [teamCodeError, setTeamCodeError] = useState(false)
  const [teamCodeErrorMsg, setTeamCodeErrorMsg] = useState('')

  // teamByCode로 검증에 성공한 팀 정보. null이면 아직 코드 입력 단계, 값이 있으면 참여 확인 단계.
  const [verifiedTeam, setVerifiedTeam] = useState(null)

  const [copied, setCopied] = useState(false)
  const copyTimeoutRef = useRef(null)

  // 언마운트 시 타이머 정리 (setState on unmounted component 방지)
  useEffect(() => {
    return () => {
      if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
    }
  }, [])

  // JOIN_TEAM 모달이 오픈될 때마다 이전 검증 상태를 초기화
  useEffect(() => {
    if (modal === MODAL.JOIN_TEAM) {
      setVerifiedTeam(null)
      setTeamCodeError(false)
      setTeamCodeErrorMsg('')
    }
  }, [modal])

  async function copyToClipboard(text) {
    try {
      await navigator.clipboard?.writeText(text)
      setCopied(true)
      if (copyTimeoutRef.current) clearTimeout(copyTimeoutRef.current)
      copyTimeoutRef.current = setTimeout(() => setCopied(false), 1500)
    } catch (e) {
      console.warn('[AppModals] 클립보드 복사 실패:', e)
    }
  }

  if (!modal) return null

  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  function handleBackdrop(e) {
    if (e.target === e.currentTarget) close()
  }

  const targetTeam = teams.find(t => t.id === modalTeamTarget)

  async function createTeamFn() {
    const name = teamNameRef.current?.value?.trim()
    const description = teamDescRef.current?.value?.trim()

    if (!name || name.length > 100) {
      setTeamNameError(true);
      return
    }

    setTeamNameError(false)

    requestHandler(() => createTeam({ name, description }), {
      onSuccess: (data) => dispatch({ type: APP.ADD_TEAM, payload: data }),
      onFail: (message) => console.warn('[AppModals] 팀 생성 실패:', message),
      showGlobalError: true,
    })
  }

  // 팀 코드가 실제로 존재/유효한지 먼저 확인
  async function verifyTeamCodeFn() {
    const teamCode = teamCodeRef.current?.value?.trim()
    if (!teamCode || teamCode.length > 50) {
      setTeamCodeError(true)
      setTeamCodeErrorMsg('팀 코드를 입력하세요.')
      setVerifiedTeam(null)
      return
    }
    setTeamCodeError(false)
    setTeamCodeErrorMsg('')

    requestHandler(() => getTeamByCode(teamCode), {
      onSuccess: (data) => setVerifiedTeam(data),
      onFail: (message) => {
        setVerifiedTeam(null)
        setTeamCodeError(true)
        setTeamCodeErrorMsg(message)
      },
    })
  }

  // 검증을 통과한 팀에 한해서만 실제 참여 요청을 보낸다.
  async function joinTeamFn() {
    if (!verifiedTeam || verifiedTeam.role) return

    requestHandler(() => joinTeam(verifiedTeam.teamCode), {
      onSuccess: (data) => {
        dispatch({ type: APP.JOIN_TEAM, payload: data })
        close()
      },
      onFail: (message) => {
        setVerifiedTeam(null)
        setTeamCodeError(true)
        setTeamCodeErrorMsg(message)
      },
      showGlobalError: true,
    })
  }

  async function joinTeamFn() {
    const teamCode = teamCodeRef.current?.value?.trim();
    if (!teamCode || teamCode.length > 50) {
      setTeamCodeError(true);
      return;
    }
    setTeamCodeError(false);

    requestHandler(() => joinTeam(teamCode), {
      onSuccess: (data) => {
        dispatch({ type: APP.JOIN_TEAM, payload: data })
        close()
      },
      onFail: (message) => console.warn('[AppModals] 팀 참여 실패:', message),
      showGlobalError: true,
    })
  }

  // ── 팀 생성 ──────────────────────────────────────────────────────
  if (modal === MODAL.CREATE_TEAM) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 생성</h3>
          <div className="form-group">
            <label>팀 이름 <span className="form-label-hint">(필수, 최대 100자)</span></label>
            <input className="input" placeholder="팀 이름을 입력해 주세요" ref={teamNameRef} maxLength={100} />
            {teamNameError && <div className="error-msg">팀 이름을 입력하세요.</div>}
          </div>
          <div className="form-group">
            <label>팀 설명 <span className="form-label-hint">(선택)</span></label>
            <textarea className="textarea textarea--sm" placeholder="팀을 간단히 소개해 주세요" ref={teamDescRef} />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button
              className="btn btn-primary"
              onClick={createTeamFn}
            >
              생성
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 생성 성공 ────────────────────────────────────────────────
  if (modal === MODAL.CREATE_TEAM_SUCCESS) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <div className="modal-success-header">
            <div className="modal-success-icon">&#10003;</div>
            <div className="modal-success-text">
              <h3>팀이 성공적으로 생성되었습니다</h3>
              <p className="modal-subtitle">아래 코드를 공유해서 팀원을 초대하세요.</p>
            </div>
          </div>
          <div className="code-display">{createdTeamCode}</div>
          <div className="modal-actions modal-actions--spread">
            <button className="btn btn-ghost" onClick={() => copyToClipboard(createdTeamCode)}>복사</button>
            <button className="btn btn-primary" onClick={close}>확인</button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 참여 ──────────────────────────────────────────────────────
  if (modal === MODAL.JOIN_TEAM) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 참여</h3>
          <p className="modal-subtitle">팀장에게 공유받은 코드를 입력하여 팀에 참가하세요.</p>

          {!verifiedTeam ? (
            // 1단계: 팀 코드 입력 후 유효성 확인
            <>
              <div className="form-group form-group--mt">
                <input
                  className="input text-mono"
                  placeholder="팀 코드 입력"
                  ref={teamCodeRef}
                  maxLength={50}
                  onChange={() => { setTeamCodeError(false); setTeamCodeErrorMsg('') }}
                />
                {teamCodeError && <div className="error-msg">{teamCodeErrorMsg || '팀 코드를 확인해 주세요.'}</div>}
              </div>
              <div className="modal-actions">
                <button className="btn btn-ghost" onClick={close}>취소</button>
                <button className="btn btn-primary" onClick={verifyTeamCodeFn}>확인</button>
              </div>
            </>
          ) : (
            // 2단계: 검증된 팀 정보를 보여주고 최종 참여 여부를 확인받는다.
            <>
              <div className="form-group form-group--mt">
                <div className="field-label">팀 이름</div>
                <div className="team-code-value">{verifiedTeam.name}</div>
                {verifiedTeam.description && <p className="modal-subtitle">{verifiedTeam.description}</p>}
              </div>
              {alreadyJoined && <div className="error-msg">이미 참여 중인 팀입니다.</div>}
              {!alreadyJoined && teamCodeError && <div className="error-msg">{teamCodeErrorMsg}</div>}
              <div className="modal-actions">
                <button className="btn btn-ghost" onClick={() => setVerifiedTeam(null)}>다시 입력</button>
                <button className="btn btn-primary" onClick={joinTeamFn} disabled={alreadyJoined}>참여</button>
              </div>
            </>
          )}
        </div>
      </div>
    )
  }

  // ── 팀 관리 (팀장) ───────────────────────────────────────────────
  if (modal === MODAL.TEAM_MANAGE) {
    const MOCK_MEMBERS = [
      { name: '홍길동', role: '팀장', since: '2024.05.01', isMe: true },
      { name: '김철수', role: '팀원', since: '2024.05.03' },
      { name: '이영희', role: '팀원', since: '2024.05.10' },
      { name: '박민수', role: '팀원', since: '2024.05.12' },
    ]
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          {/* 목업 기준: 팀명만 표시, "관리" 텍스트 없음 */}
          <h3>{targetTeam?.name ?? '팀'}</h3>
          <div className="field-label modal-team-code-label">
            팀 코드: <span className="team-code-value">A1B2C3</span>{' '}
            <button className="text-link" onClick={() => navigator.clipboard?.writeText('A1B2C3')}>복사</button>
          </div>
          <div className="field-label">팀원 ({MOCK_MEMBERS.length})</div>
          <div className="modal-member-list">
            {MOCK_MEMBERS.map(m => (
              <div key={m.name} className="member-row">
                <span className="mname">
                  {m.name}
                  <span className={`role-tag ${m.role === '팀장' ? 'leader' : 'member'}`}>{m.role}</span>
                </span>
                <span className="mmeta">{m.since} 가입{m.isMe ? ' (나)' : ''}</span>
              </div>
            ))}
          </div>
          <div className="modal-actions modal-actions--spread">
            <button
              className="btn btn-danger"
              onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.DELETE_CONFIRM, teamId: modalTeamTarget } })}
            >
              팀 삭제
            </button>
            <button className="btn btn-primary" onClick={close}>확인</button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 삭제 확인 ─────────────────────────────────────────────────
  if (modal === MODAL.DELETE_CONFIRM) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 삭제</h3>
          <p className="danger-text">
            정말 이 팀을 삭제하시겠습니까?<br />
            삭제 시 모든 팀원이 게시판에 접근할 수 없으며 복구할 수 없습니다.
          </p>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.TEAM_MANAGE } })}>
              취소
            </button>
            <button className="btn-danger-filled btn" onClick={() => dispatch({ type: APP.REMOVE_TEAM, payload: modalTeamTarget })}>
              삭제
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 탈퇴 확인 ─────────────────────────────────────────────────
  if (modal === MODAL.LEAVE_CONFIRM) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 탈퇴</h3>
          <p className="danger-text">
            이 팀에서 탈퇴하시겠습니까?<br />
            탈퇴 시 팀 게시판에 접근할 수 없습니다.
          </p>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button className="btn-danger-filled btn" onClick={() => dispatch({ type: APP.LEAVE_TEAM, payload: modalTeamTarget })}>
              탈퇴
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── 닉네임 변경 (하위 호환 유지, EDIT_PROFILE로 대체됨) ─────────
  if (modal === MODAL.EDIT_NICKNAME) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>닉네임 변경</h3>
          <div className="form-group form-group--mt">
            <div className="input-row">
              <input className="input" placeholder="새 닉네임 입력" defaultValue={state.nickname} ref={nicknameRef} />
              <button className="btn btn-ghost btn-sm">중복확인</button>
            </div>
          </div>
          <div className="auth-note auth-note--no-mt">현재 닉네임과 동일한 값은 입력할 수 없습니다.</div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button
              className="btn btn-primary"
              onClick={() => {
                const val = nicknameRef.current?.value?.trim()
                if (val && val !== state.nickname) {
                  dispatch({ type: APP.SET_NICKNAME, payload: val })
                  close()
                }
              }}
            >
              닉네임 저장
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── 정보 수정 (닉네임 변경 + 비밀번호 변경 탭 통합) ─────────────
  // EditProfileModal은 탭 상태를 내부 로컬 상태로 관리하므로 별도 컴포넌트로 분리
  if (modal === MODAL.EDIT_PROFILE) {
    return (
      <EditProfileModal
        state={state}
        dispatch={dispatch}
        nicknameRef={nicknameRef}
        close={close}
      />
    )
  }

  // ── 상세 검색 ─────────────────────────────────────────────────────
  if (modal === MODAL.SEARCH_DETAIL) {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal modal--wide">
          <h3>상세 검색</h3>
          <div className="form-group form-group--mt">
            <input
              className="input"
              placeholder="검색어를 입력하세요"
              value={state.searchKeyword}
              onChange={e => dispatch({ type: APP.SET_SEARCH_KEYWORD, payload: e.target.value })}
            />
          </div>

          <div className="field-label field-label--mb">기술 스택</div>
          <StackSelector
            toggles={state.searchStackToggles}
            onToggle={key => dispatch({ type: APP.TOGGLE_SEARCH_STACK, payload: key })}
          />

          {/* 상태 필터: 전체 / 해결됨 / 미해결 */}
          <div className="field-label field-label--spaced">상태</div>
          <div className="radio-row">
            {[['all', '전체'], ['solved', '해결됨'], ['unsolved', '미해결']].map(([val, label]) => (
              <label key={val} className="radio-opt">
                <input
                  type="radio"
                  name="search-status"
                  checked={state.searchStatus === val}
                  onChange={() => dispatch({ type: APP.SET_SEARCH_STATUS, payload: val })}
                />
                {label}
              </label>
            ))}
          </div>

          <div className="modal-actions modal-actions--spread">
            <button className="btn btn-ghost" onClick={() => dispatch({ type: APP.RESET_SEARCH_FILTERS })}>초기화</button>
            <button className="btn btn-primary" onClick={close}>검색 적용</button>
          </div>
        </div>
      </div>
    )
  }

  return null
}

// ── 정보 수정 모달 (닉네임 변경 + 비밀번호 변경 탭) ─────────────────
// useState가 필요하므로 별도 함수 컴포넌트로 분리 (Hook 규칙: 조건부 호출 불가)
function EditProfileModal({ state, dispatch, nicknameRef, close }) {
  const [activeTab, setActiveTab] = useState('nickname') // 'nickname' | 'password'
  const [showPwCode, setShowPwCode] = useState(false)    // 비밀번호 변경 탭 인증 코드 섹션

  // Google 소셜 로그인 사용자는 비밀번호 변경 불가
  const isGoogleUser = state.authProvider === 'GOOGLE'

  return (
    <div className="modal-overlay" onClick={e => { if (e.target === e.currentTarget) close() }}>
      <div className="modal modal--wide">
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
            {/* 아이디 readonly 표시 */}
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
              <button
                className="btn btn-primary"
                onClick={() => {
                  const val = nicknameRef.current?.value?.trim()
                  if (val && val !== state.nickname) {
                    dispatch({ type: APP.SET_NICKNAME, payload: val })
                    close()
                  }
                }}
              >
                닉네임 저장
              </button>
            </div>
          </>
        )}

        {/* 비밀번호 변경 탭 */}
        {activeTab === 'password' && (
          <>
            {/* 아이디 readonly 표시 */}
            <div className="field-id-readonly">아이디 : {state.email} (변경 불가)</div>

            {/* Google 사용자: 비밀번호 변경 불가 안내만 표시 */}
            {isGoogleUser ? (
              <div className="google-pw-notice">
                구글 로그인 사용자는 비밀번호 변경이 불가능합니다.
              </div>
            ) : (
              <>
                {/* 이메일 인증 요청 */}
                <div className="form-group">
                  <label>이메일 인증</label>
                  <div className="input-row">
                    <input className="input" type="email" placeholder="이메일 입력" defaultValue={state.email} readOnly />
                    <button className="btn btn-ghost btn-sm" onClick={() => setShowPwCode(true)}>인증 요청</button>
                  </div>
                </div>

                {/* 인증 코드 입력 섹션 — 인증 요청 후 노출 */}
                {showPwCode && (
                  <div className="form-group">
                    <div className="input-row">
                      <input className="input" placeholder="인증 코드 입력" />
                      <button className="btn btn-ghost btn-sm">인증 확인</button>
                    </div>
                  </div>
                )}

                {/* 새 비밀번호 */}
                <div className="form-group">
                  <label>새 비밀번호</label>
                  <input className="input" type="password" placeholder="새 비밀번호 입력" />
                  <div className="hint">* 8자 이상, 영문+숫자+특수문자 조합</div>
                </div>

                {/* 새 비밀번호 확인 */}
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
      </div>
    </div>
  )
}

export default AppModals
