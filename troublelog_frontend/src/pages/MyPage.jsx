import { useReducer } from 'react'
import { useAppContext } from '../context/AppContext.jsx'
import { APP, MYPAGE } from '../constants/actionTypes.js'
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
          <span className="since">가입일: {appState.userSince}</span>
        </div>
        <div className="nickname-row">
          <span className="nickname-label">닉네임</span>
          <span className="nickname-value">{appState.nickname}</span>
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: 'edit-nickname' } })}
          >
            닉네임 변경
          </button>
        </div>
      </div>

      {/* page-head--tight: margin-bottom 10px */}
      <div className="page-head page-head--tight">
        {/* page-head__title--sm: font-size 14px */}
        <h1 className="page-head__title--sm">내 활동</h1>
      </div>

      <div className="panel">
        {/* auth-tabs--mb: margin-bottom 14px */}
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

        {/*
          mini-list--fixed: height 315px (댓글 5행 기준) + overflow hidden
          패딩 행 불필요 — 고정 높이 컨테이너가 빈 공간을 자동으로 처리
        */}
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

      {/* page-head--mt: margin-top 22px */}
      <div className="page-head page-head--mt">
        <h1>내 팀 관리</h1>
      </div>
      <div className="panel">
        {appState.teams.map(t => (
          <div key={t.id} className="team-manage-row">
            <div>
              {t.name}
              <span className={`role-tag ${t.role === '팀장' ? 'leader' : 'member'}`}>{t.role}</span>
            </div>
            {t.role === '팀장' ? (
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: 'team-manage', teamId: t.id } })}
              >
                관리
              </button>
            ) : (
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => appDispatch({ type: APP.OPEN_MODAL, payload: { modal: 'leave-confirm', teamId: t.id } })}
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