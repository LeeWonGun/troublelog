import { useEffect, useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { APP, MYPAGE } from '../constants/actionTypes.js'
import { MODAL } from '../constants/modalTypes.js'
import Pagination from '../components/common/Pagination.jsx'
import myPageReducer, { initialState } from '../reducers/myPageReducer.js'
import { MOCK_MY_ANSWERS, MOCK_MY_COMMENTS } from '../constants/mockData.js'
import { getMyQuestions } from '../api/questionApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { mapQuestionListItem } from '../util/questionMapper.js'
import { formatDateTime } from '../util/dateUtil.js'

const PAGE_SIZE = 5

const EMPTY_MSG = {
  questions: '작성한 게시글이 없습니다.',
  answers: '작성한 답변이 없습니다.',
  comments: '작성한 댓글이 없습니다.',
}

function MyPage() {
  const { state: appState, dispatch: appDispatch } = useAppContext()
  const [state, dispatch] = useReducer(myPageReducer, initialState)
  const navigate = useNavigate()

  const { activeTab, pages, myQuestions } = state
  const questionsPage = pages.questions

  useEffect(() => {
    let ignore = false // 페이지가 빠르게 바뀔 때 늦게 도착한 응답이 최신 상태를 덮어쓰지 않도록 막는 가드

    dispatch({ type: MYPAGE.SET_LOADING })

    requestHandler(
      () => getMyQuestions({ page: questionsPage - 1, size: PAGE_SIZE}), // 화면은 1-based, API는 0-based
      {
        isCancelled: () => ignore,
        onSuccess: (data) => dispatch({
          type: MYPAGE.SET_DATA,
          payload: {
            items: (data.content ?? []).map(mapQuestionListItem),
            totalPages: Math.max(1, data.totalPages ?? 1),
            totalCount: data.totalElements ?? 0,
          },
        }),
        onFail: (message) => dispatch({ type: MYPAGE.SET_ERROR, payload: message }),
        fallbackMessage: '내 질문 목록을 불러오지 못했습니다.',
      },
    )

    return () => { ignore = true }
  }, [questionsPage])

  //TODO: 답변/댓글 탭은 백엔드 API가 아직 없어 목업 기반 클라이언트 페이징을 유지
  const mockRows = activeTab === 'answers' ? MOCK_MY_ANSWERS : MOCK_MY_COMMENTS
  const mockTotalPages = Math.max(1, Math.ceil(mockRows.length / PAGE_SIZE))

  // 질문 탭은 서버 페이징, 나머지 탭은 목업 클라이언트 페이징으로 분기한다.
  const totalPages = activeTab === 'questions' ? myQuestions.totalPages : mockTotalPages
  const currentPage = activeTab === 'questions'
    ? questionsPage
    : Math.min(pages[activeTab], mockTotalPages)
  const shown = activeTab === 'questions'
    ? myQuestions.items
    : mockRows.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE)

  const isQuestionsBusy = activeTab === 'questions' && myQuestions.loading
  const questionsError = activeTab === 'questions' ? myQuestions.error : null

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
            ['questions', '내 질문', myQuestions.totalCount],
            ['answers', '내 답변', MOCK_MY_ANSWERS.length],
            ['comments', '내 댓글', MOCK_MY_COMMENTS.length],
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
          {isQuestionsBusy && <div className="mini-empty">불러오는 중...</div>}
          {questionsError && !isQuestionsBusy && <div className="mini-empty">{questionsError}</div>}

          {!isQuestionsBusy && !questionsError && shown.length === 0 && (
            <div className="mini-empty">{EMPTY_MSG[activeTab]}</div>
          )}

          {activeTab === 'questions' && !isQuestionsBusy && !questionsError && shown.map(q => (
            <div
              key={q.id}
              className="mini-row mini-row--link"
              onClick={() => navigate(`/questions/${q.id}`)} // 클릭 시 게시글 상세로 이동
            >
              <span className="t">{q.title}</span>
              <span className="mini-meta">
                <span>♡ {q.likes}</span>
                <span>{formatDateTime(q.createdAt).slice(0, 10)}</span>
              </span>
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
        {appState.teams.length > 0 ?
          appState.teams.map(t => (
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
          ))
          : <p className="empty-message">소속된 팀이 없습니다.</p>}

      </div>
    </div>
  )
}

export default MyPage