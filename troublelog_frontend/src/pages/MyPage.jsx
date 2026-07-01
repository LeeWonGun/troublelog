import { useReducer } from 'react'
import { useAppContext } from '../context/AppContext.jsx'
import { APP, MYPAGE } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'
import Pagination from '../components/common/Pagination.jsx'
import myPageReducer, { initialState } from '../reducers/myPageReducer.js'
import { MOCK_MY_QUESTIONS, MOCK_MY_ANSWERS, MOCK_MY_COMMENTS } from '../constants/mockData.js'

const PAGE_SIZE = 5

function getRows(tab) {
  if (tab === 'questions') return MOCK_MY_QUESTIONS
  if (tab === 'answers')   return MOCK_MY_ANSWERS
  return MOCK_MY_COMMENTS
}

const EMPTY_MSG = {
  questions: '작성한 게시글이 없습니다.',
  answers:   '작성한 답변이 없습니다.',
  comments:  '작성한 댓글이 없습니다.',
}

function MyPage() {
  const { state: appState, dispatch: appDispatch } = useAppContext()
  const [state, dispatch] = useReducer(myPageReducer, initialState)

  const { activeTab, pages } = state
  const all = getRows(activeTab)
  const totalPages  = Math.max(1, Math.ceil(all.length / PAGE_SIZE))
  const currentPage = Math.min(pages[activeTab], totalPages)
  const shown = all.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  return (
    <div className="main">
      <div className="mypage-head">
        <div>
          <h1>마이페이지</h1>
        </div>
        <button
          className="btn btn-ghost btn-sm"
          onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.EDIT_PROFILE } })}
        >
          정보 수정
        </button>
      </div>

      <div className="page-head page-head--tight">
        <h1 className="page-head__title--sm">내 질문 조회</h1>
      </div>

      <div className="panel">
        <div className="auth-tabs auth-tabs--mb">
          {[
            ['questions', '내 질문',  MOCK_MY_QUESTIONS.length],
            ['answers',   '내 답변',  MOCK_MY_ANSWERS.length],
            ['comments',  '내 댓글',  MOCK_MY_COMMENTS.length],
          ].map(([tab, label, count]) => (
            <button
              key={tab}
              className={`auth-tab ${activeTab === tab ? 'active' : ''}`}
              onClick={() => dispatch({ type: MYPAGE.SET_TAB, payload: tab })}
            >
              {label} <span className="tab-count">{count}</span>
            </button>
          ))}
        </div>

        <div className="mini-list mini-list--fixed">
          {shown.length === 0 && (
            <div className="mini-empty">{EMPTY_MSG[activeTab]}</div>
          )}

          {activeTab === 'questions' && shown.map(q => (
            <div key={q.id} className="mini-row">
              <span className="t">{q.title}</span>
              <span className="mini-meta"><span>♡ {q.likes}</span><span>{q.date}</span></span>
            </div>
          ))}

          {activeTab === 'answers' && shown.map(a => (
            <div key={a.id} className="mini-row">
              <span className="t">
                {a.accepted && <span className="badge-mini">채택됨</span>}
                {a.title}
              </span>
              <span className="mini-meta"><span>♡ {a.likes}</span><span>{a.date}</span></span>
            </div>
          ))}

          {activeTab === 'comments' && shown.map(c => (
            <div key={c.id} className="mini-row mini-row--comment">
              <div className="t">
                <div className="c-on">{c.on}</div>
                <div className="c-body-mini">{c.body}</div>
              </div>
              <span className="mini-meta"><span>{c.date}</span></span>
            </div>
          ))}
        </div>

        <Pagination
          compact
          current={currentPage}
          total={totalPages}
          onChange={p => dispatch({ type: MYPAGE.SET_PAGE, payload: p })}
        />
      </div>

      <div className="page-head page-head--mt">
        <h1>내 팀 관리</h1>
      </div>
      <div className="panel">
        {appState.teams.map(t => (
          <div key={t.teamId} className="team-manage-row">
            <div>
              {t.name}
              <span className={`role-tag ${t.role === 'LEADER' ? 'LEADER' : 'MEMBER'}`}>
                {t.role === 'LEADER' ? '팀장' : '팀원'}
              </span>
            </div>
            {t.role === 'LEADER' ? (
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.TEAM_MANAGE, teamId: t.teamId } })}
              >
                관리
              </button>
            ) : (
              <button
                className="btn btn-danger btn-sm"
                onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: MODAL.LEAVE_CONFIRM, teamId: t.teamId } })}
              >
                탈퇴
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

export default MyPage