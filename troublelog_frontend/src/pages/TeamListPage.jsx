import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { APP } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'

function TeamListPage() {
  const navigate = useNavigate()
  const { state, dispatch } = useAppContext()

  return (
    <div className="main">
      <div className="page-head">
        <h1>내 팀 목록</h1>
      </div>
      <div className="panel">
        {state.teams.length === 0 && (
          /* team-list-empty: text-align center, color muted, padding 24px 0 */
          <div className="team-list-empty">참여한 팀이 없습니다.</div>
        )}
        {state.teams.map(t => (
          <div key={t.id} className="team-manage-row">
            <div>
              {t.name}
              <span className={`role-tag ${t.role === '팀장' ? 'leader' : 'member'}`}>{t.role}</span>
            </div>
            <button
              className="btn btn-ghost btn-sm"
              onClick={() => { dispatch({ type: APP.SET_ACTIVE_TEAM, payload: t.id }); navigate('/board') }}
            >
              게시판 →
            </button>
          </div>
        ))}

        {/* team-action-row: flex + gap 8px + margin-top 16px */}
        <div className="team-action-row">
          {/* btn-flex: flex 1 */}
          <button className="btn btn-ghost btn-flex" onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.CREATE_TEAM } })}>
            팀 생성
          </button>
          <button className="btn btn-ghost btn-flex" onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.JOIN_TEAM } })}>
            팀 참여
          </button>
        </div>
      </div>
    </div>
  )
}

export default TeamListPage