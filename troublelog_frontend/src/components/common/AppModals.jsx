import { useRef } from 'react'
import { useAppContext } from '../../context/AppContext.jsx'
import { APP } from '../../constants/actionTypes.js'
import StackSelector from './StackSelector.jsx'

function AppModals() {
  const { state, dispatch } = useAppContext()
  const { modal, modalTeamTarget, teams, createdTeamCode } = state

  const teamNameRef  = useRef(null)
  const teamDescRef  = useRef(null)
  const teamCodeRef  = useRef(null)
  const nicknameRef  = useRef(null)

  if (!modal) return null

  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  function handleBackdrop(e) {
    if (e.target === e.currentTarget) close()
  }

  const targetTeam = teams.find(t => t.id === modalTeamTarget)

  // ── 팀 생성 ──────────────────────────────────────────────────────
  if (modal === 'create-team') {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 생성</h3>
          <div className="form-group">
            <label>팀 이름 <span className="form-label-hint">(필수, 최대 100자)</span></label>
            <input className="input" placeholder="팀 이름을 입력해 주세요" ref={teamNameRef} />
          </div>
          <div className="form-group">
            <label>팀 설명 <span className="form-label-hint">(선택)</span></label>
            <textarea className="textarea textarea--sm" placeholder="팀을 간단히 소개해 주세요" ref={teamDescRef} />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button
              className="btn btn-primary"
              onClick={() =>
                dispatch({
                  type: APP.ADD_TEAM,
                  payload: { name: teamNameRef.current?.value?.trim() || '새 팀', code: 'Q7K2P9' },
                })
              }
            >
              생성
            </button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 생성 성공 ────────────────────────────────────────────────
  if (modal === 'create-team-success') {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <div className="modal-success-icon">✓</div>
          <h3>팀이 성공적으로 생성되었습니다</h3>
          <p className="modal-subtitle">아래 코드를 공유해서 팀원을 초대하세요.</p>
          <div className="code-display">{createdTeamCode}</div>
          <div className="modal-actions modal-actions--spread">
            <button className="btn btn-ghost" onClick={() => navigator.clipboard?.writeText(createdTeamCode)}>복사</button>
            <button className="btn btn-primary" onClick={close}>확인</button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 참여 ──────────────────────────────────────────────────────
  if (modal === 'join-team') {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 참여</h3>
          <p className="modal-subtitle">팀장에게 공유받은 코드를 입력하여 팀에 참가하세요.</p>
          <div className="form-group form-group--mt">
            <input className="input text-mono" placeholder="팀 코드 입력" ref={teamCodeRef} />
          </div>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={close}>취소</button>
            <button className="btn btn-primary" onClick={close}>입력</button>
          </div>
        </div>
      </div>
    )
  }

  // ── 팀 관리 (팀장) ───────────────────────────────────────────────
  if (modal === 'team-manage') {
    const MOCK_MEMBERS = [
      { name: '홍길동', role: '팀장', since: '2024.05.01', isMe: true },
      { name: '김철수', role: '팀원', since: '2024.05.03' },
      { name: '이영희', role: '팀원', since: '2024.05.10' },
      { name: '박민수', role: '팀원', since: '2024.05.12' },
    ]
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>{targetTeam?.name ?? '팀'} 관리</h3>
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
              onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: 'delete-confirm', teamId: modalTeamTarget } })}
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
  if (modal === 'delete-confirm') {
    return (
      <div className="modal-overlay" onClick={handleBackdrop}>
        <div className="modal">
          <h3>팀 삭제</h3>
          <p className="danger-text">
            정말 이 팀을 삭제하시겠습니까?<br />
            삭제 시 모든 팀원이 게시판에 접근할 수 없으며 복구할 수 없습니다.
          </p>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: 'team-manage' } })}>
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
  if (modal === 'leave-confirm') {
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

  // ── 닉네임 변경 ──────────────────────────────────────────────────
  if (modal === 'edit-nickname') {
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

  // ── 상세 검색 ─────────────────────────────────────────────────────
  if (modal === 'search-detail') {
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

          <div className="field-label field-label--spaced">첨부 이미지</div>
          <div className="radio-row">
            <label className="radio-opt">
              <input
                type="checkbox"
                checked={state.searchHasImage}
                onChange={() => dispatch({ type: APP.TOGGLE_SEARCH_HAS_IMAGE })}
              />
              첨부 이미지 있는 게시글만
            </label>
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

export default AppModals