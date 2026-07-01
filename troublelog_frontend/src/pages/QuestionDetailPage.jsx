import { useEffect, useReducer } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { QDETAIL } from '../constants/actionTypes.js'
import { useAppContext } from '../context/AppContext.jsx'
import StatusChip from '../components/common/StatusChip.jsx'
import TagRow from '../components/common/TagRow.jsx'
import questionDetailReducer, { initialState } from '../reducers/questionDetailReducer.js'
import { MOCK_ANSWERS } from '../constants/mockData.js'
import { getQuestion } from '../api/questionApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { formatDateTime } from '../util/dateUtil.js'
import MarkdownHighlighter, { normalizeCodeLanguage } from '../components/common/MarkdownHighlighter.jsx'

function CommentBlock({ answerId, commentInput, onInputChange }) {
  if (answerId !== 101) return null
  return (
    <div className="comment-block">
      {/* 댓글A */}
      <div className="comment-row">
        <div className="c-head">닉네임A · 2026.06.23 14:50</div>
        <div className="c-body">저도 같은 문제 겪었는데 필터 체인 순서 문제였어요.</div>
        <div className="c-actions"><button>답글</button><button>수정</button><button>삭제</button></div>
      </div>

      {/* 댓글A의 대댓글B — 대댓글에는 "답글" 버튼 없음 */}
      <div className="comment-row reply">
        <div className="c-head"><span className="reply-arrow">&#8618;</span> 닉네임B · 2026.06.23 15:02</div>
        <div className="c-body">감사합니다! 해결했습니다.</div>
        <div className="c-actions"><button>수정</button><button>삭제</button></div>
      </div>

      {/* 댓글C */}
      <div className="comment-row">
        <div className="c-head">닉네임C · 2026.06.23 15:30</div>
        <div className="c-body">저도 같은 에러 처음엔 당황했는데 Security 설정 확인해보세요!</div>
        <div className="c-actions"><button>답글</button><button>수정</button><button>삭제</button></div>
      </div>

      <div className="comment-input-row">
        <input
          placeholder="댓글을 입력하세요"
          value={commentInput ?? ''}
          onChange={e => onInputChange(e.target.value)}
        />
        <button className="btn btn-primary btn-sm">등록</button>
      </div>
    </div>
  )
}

// 목업 목적으로 임시 하드코딩된 작성자 정보 (API 연동 시 서버 응답으로 대체)
const MOCK_AUTHOR_ID = 'user_001'

function QuestionDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { state: appState } = useAppContext()
  const [state, dispatch] = useReducer(questionDetailReducer, initialState)

  // 질문 상세 조회 - 라우트 파라미터(id)가 바뀔 때마다 재조회
  useEffect(() => {
    let ignore = false // 응답이 늦게 도착했을 때 이전 요청 결과로 state를 덮어쓰지 않기 위한 가드

    requestHandler(() => getQuestion(id), {
      isCancelled: () => ignore,
      onSuccess: (data) => dispatch({ type: QDETAIL.SET_QUESTION, payload: data }),
      onFail: (message) => dispatch({ type: QDETAIL.SET_ERROR, payload: message }),
      fallbackMessage: '질문을 불러오지 못했습니다.',
    })

    return () => { ignore = true }
  }, [id])

  if (state.error || !state.question) {
    return (
      <div className="main">
        <button className="text-link text-link--back" onClick={() => navigate('/board')}>
          &#8592; 게시판 목록
        </button>
        <div className="panel">{state.error ?? '질문을 찾을 수 없습니다.'}</div>
      </div>
    )
  }

  const { question } = state

  const normalizedCodeLanguage = normalizeCodeLanguage(question.codeLanguage)
  const codeMarkdown = question.code
    ? `\`\`\`${normalizedCodeLanguage}\n${question.code}\n\`\`\``
    : ''

  // 로그인 상태이고 작성자 본인인 경우에만 수정 버튼 표시
  // TODO: API 연동 시 question.authorId === appState.userId 로 비교
  const isAuthor = appState.isLoggedIn && appState.email === MOCK_AUTHOR_ID

  return (
    <div className="main">
      <button className="text-link text-link--back" onClick={() => navigate('/board')}>
        &#8592; 게시판 목록
      </button>

      <div className="panel">
        <div className="detail-head">
          <h1>{question.title}</h1>
          <StatusChip solved={question.status === 'SOLVED'} />
        </div>
        <TagRow tags={question.techStacks?.map(stack => stack.name) ?? []} />
        <div className="detail-meta">
          {/* TODO: writerNickname은 회원 구조 연동 전까지 null -> writerId로 임시 표기 */}
          <span>{question.writerNickname ?? `작성자 #${question.writerId}`}</span>
          <span>{formatDateTime(question.createdAt)}</span>
        </div>

        <div className="field-block">
          <div className="field-label">내용</div>
          <p>{question.content}</p>
        </div>

        {(question.codeLanguage || question.code) && (
          <div className="field-block">
            <div className="field-label field-label--with-pill">
              <span>오류 코드</span>
              <span className="code-lang-pill">{question.codeLanguage || 'text'}</span>
            </div>
            <MarkdownHighlighter
              markdown={codeMarkdown}
              emptyText="등록된 오류 코드가 없습니다."
            />
          </div>
        )}

        {question.errorMessage && (
          <div className="field-block">
            <div className="field-label">오류 메시지</div>
            <div className="code-block">{question.errorMessage}</div>
          </div>
        )}

        {question.tried && (
          <div className="field-block">
            <div className="field-label">시도한 방법</div>
            <p>{question.tried}</p>
          </div>
        )}

        {/* TODO: 첨부 이미지: QuestionDetailResponse에 이미지 목록 필드가 없어
                              fileApi 연동 전까지 임의 목업 표시  */}
        <div className="field-block">
          <div className="field-label">첨부 이미지</div>
          <div className="attach-box">&#128444; 첨부한 사진</div>
        </div>

        <div className="detail-footer">
          <button
            className={`icon-btn ${state.liked ? 'liked' : ''}`}
            onClick={() => dispatch({ type: QDETAIL.TOGGLE_POST_LIKE })}
          >
            {state.liked ? '♥' : '♡'} 좋아요 {state.likeCount}
          </button>
          <div style={{ display: 'flex', gap: 8 }}>

            {isAuthor && (
              <button
                className="btn btn-ghost btn-sm"
                onClick={() => navigate(`/questions/${id}/edit`)}
              >
                수정
              </button>
            )}
            <button className="btn btn-ghost btn-sm">답변 작성</button>
          </div>
        </div>
      </div>

      <div className="answers-head">
        <h2>답변 <span className="count">{question.answerCount}</span></h2>
      </div>

      {MOCK_ANSWERS.map(a => {
        const isAccepted = state.acceptedAnswerId === a.id
        const isLiked = !!state.answerLikes[a.id]
        return (
          <div key={a.id} className={`answer-card ${isAccepted ? 'accepted' : ''}`}>
            <div className="answer-head">
              <span className="who">{a.author}</span>
              {isAccepted && <span className="chip chip--accepted"><span className="dot" />채택됨</span>}
            </div>
            <div className="answer-body">{a.body}</div>
            <div className="answer-actions">
              <div className="left">
                <button
                  className={`icon-btn ${isLiked ? 'liked' : ''}`}
                  onClick={() => dispatch({ type: QDETAIL.TOGGLE_ANSWER_LIKE, payload: a.id })}
                >
                  {isLiked ? '♥' : '♡'}  좋아요 {a.likes + (isLiked ? 1 : 0)}
                </button>
                <span className="icon-btn" style={{ cursor: 'default' }}>댓글 {a.comments}</span>
                <button className="text-link">수정</button>
                <button className="text-link">삭제</button>
              </div>
              {!state.acceptedAnswerId && (
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => dispatch({ type: QDETAIL.ACCEPT_ANSWER, payload: a.id })}
                >
                  답변 채택
                </button>
              )}
            </div>
            <CommentBlock
              answerId={a.id}
              commentInput={state.commentInputs[a.id]}
              onInputChange={val => dispatch({ type: QDETAIL.SET_COMMENT_INPUT, answerId: a.id, value: val })}
            />
          </div>
        )
      })}
    </div>
  )
}

export default QuestionDetailPage
