import { useNavigate, useLocation } from 'react-router-dom'
import { useAppContext } from '../../context/AppContext.jsx'
import { APP } from '../../constants/actionTypes.js'

function Sidebar() {
  const navigate = useNavigate()
  const location = useLocation()
  const { state, dispatch } = useAppContext()
  const { teams, teamListOpen, activeTeam, nickname, userSince } = state

  const isBoard = location.pathname === '/board'

  function handleTeamNav(teamId) {
    dispatch({ type: APP.SET_ACTIVE_TEAM, payload: teamId })
    navigate('/board')
  }

  function handleAllBoard() {
    dispatch({ type: APP.SET_ACTIVE_TEAM, payload: null })
    navigate('/board')
  }

  return (
    <aside className="sidebar">
      <button className="brand" onClick={() => navigate('/')}>
        <span className="prompt">&gt;_</span> 트러블로그
      </button>

      <div className="user-pill">
        <div className="avatar" />
        <div>
          <div className="name">{nickname} 님</div>
          <div className="since">since {userSince}</div>
        </div>
      </div>

      <button className="btn btn-primary btn-block" onClick={() => navigate('/questions/create')}>
        + 게시글 작성
      </button>

      <div className="nav-section">
        <button
          className={`nav-item ${isBoard && activeTeam === null ? 'active' : ''}`}
          onClick={handleAllBoard}
        >
          <span className="ic">#</span> 전체 게시판
        </button>
      </div>

      <div className="team-actions">
        <button
          className="btn btn-ghost btn-sm"
          onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: 'create-team' } })}
        >
          팀 생성
        </button>
        <button
          className="btn btn-ghost btn-sm"
          onClick={() => dispatch({ type: APP.OPEN_MODAL, payload: { modal: 'join-team' } })}
        >
          팀 참여
        </button>
      </div>

      <div>
        <button
          className={`team-toggle ${teamListOpen ? 'open' : ''}`}
          onClick={() => dispatch({ type: APP.TOGGLE_TEAM_LIST })}
        >
          팀 목록 <span className="chev">▸</span>
        </button>
        <div className={`team-list ${teamListOpen ? 'open' : ''}`}>
          {teams.map(t => (
            <button
              key={t.id}
              className={`nav-item ${isBoard && activeTeam === t.id ? 'active' : ''}`}
              onClick={() => handleTeamNav(t.id)}
            >
              <span className="dot" /> {t.name}
            </button>
          ))}
        </div>
      </div>

      <div className="nav-divider" />

      <button
        className={`nav-item ${location.pathname === '/mypage' ? 'active' : ''}`}
        onClick={() => navigate('/mypage')}
      >
        <span className="ic">@</span> 마이페이지
      </button>

      <div className="sidebar-spacer" />

      <div className="sidebar-footer">
        <button className="btn btn-ghost btn-block" onClick={() => navigate('/login')}>
          로그아웃
        </button>
      </div>
    </aside>
  )
}

export default Sidebar