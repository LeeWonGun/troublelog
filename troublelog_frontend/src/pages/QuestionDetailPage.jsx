import { useEffect, useReducer } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { QDETAIL } from '../constants/actionTypes.js'
import { useAppContext } from '../context/AppContext.jsx'
import StatusChip from '../components/common/StatusChip.jsx'
import TagRow from '../components/common/TagRow.jsx'
import ModalOverlay from '../components/common/ModalOverlay.jsx'
import questionDetailReducer, { initialState } from '../reducers/questionDetailReducer.js'
import { MOCK_ANSWERS } from '../constants/mockData.js'
import { getQuestion, deleteQuestion } from '../api/questionApi.js'
import { likeQuestion, unlikeQuestion } from '../api/likeApi.js'
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

  const closeDeleteConfirm = () => dispatch({ type: QDETAIL.SET_DELETE_CONFIRM, payload: false })

  // 게시글 삭제 - 작성자 본인 여부는 서버가 재검증한다 (실패 시 전역 에러 모달 표시)
  function handleDelete() {
    requestHandler(() => deleteQuestion(id), {
      onSuccess: () => navigate('/board'),
      onFail: closeDeleteConfirm,
      fallbackMessage: '게시글 삭제에 실패했습니다.',
      showGlobalError: true,
    })
  }

  // 게시글 좋아요 토글 - 서버가 멱등 처리하므로 응답값(liked, likeCount)으로 상태를 확정한다
  function handleToggleLike() {
    // 비회원은 좋아요 불가 - 불필요한 요청 없이 바로 로그인 페이지로 이동
    if (!appState.isLoggedIn) {
      navigate('/login')
      return
    }

    const apiCall = state.liked ? () => unlikeQuestion(id) : () => likeQuestion(id)

    requestHandler(apiCall, {
      onSuccess: (data) => dispatch({ type: QDETAIL.SET_LIKE, payload: data }),
      fallbackMessage: '좋아요 처리에 실패했습니다.',
      showGlobalError: true,
    })
  }

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

  // 로그인 상태 + 작성자 본인인 경우에만 수정/삭제 버튼 노출 (UI 제어용 - 서버에서도 재검증)
  const isAuthor = appState.isLoggedIn
    && appState.userId != null
    && appState.userId === question.writerId

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
            onClick={handleToggleLike}
          >
            {state.liked ? '♥' : '♡'} 좋아요 {state.likeCount}
          </button>
          <div style={{ display: 'flex', gap: 8 }}>
            {isAuthor && (
              <>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => navigate(`/questions/${id}/edit`)}
                >
                  수정
                </button>
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => dispatch({ type: QDETAIL.SET_DELETE_CONFIRM, payload: true })}
                >
                  삭제
                </button>
              </>
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

      {/* 게시글 삭제 확인 모달 - soft delete이므로 사용자 확인 후에만 요청 */}
      {state.deleteConfirmOpen && (
        <ModalOverlay onClose={closeDeleteConfirm}>
          <h3>게시글 삭제</h3>
          <p className="danger-text">
            정말 이 게시글을 삭제하시겠습니까?<br />
            삭제된 게시글은 목록에서 사라지며 복구할 수 없습니다.
          </p>
          <div className="modal-actions">
            <button className="btn btn-ghost" onClick={closeDeleteConfirm}>취소</button>
            <button className="btn-danger-filled btn" onClick={handleDelete}>삭제</button>
          </div>
        </ModalOverlay>
      )}
    </div>
  )
}

export default QuestionDetailPage