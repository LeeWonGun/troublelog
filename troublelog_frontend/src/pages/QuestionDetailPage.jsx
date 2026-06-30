import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { QDETAIL } from '../constants/actionTypes.js'
import StatusChip from '../components/common/StatusChip.jsx'
import TagRow from '../components/common/TagRow.jsx'
import questionDetailReducer, { initialState } from '../reducers/questionDetailReducer.js'
import { MOCK_ANSWERS } from '../constants/mockData.js'

function CommentBlock({ answerId, commentInput, onInputChange }) {
  if (answerId !== 101) return null
  return (
    <div className="comment-block">
      <div className="comment-row">
        <div className="c-head">닉네임A · 2026.06.23 14:50</div>
        <div className="c-body">저도 같은 문제 겪었는데 필터 체인 순서 문제였어요.</div>
        <div className="c-actions"><button>답글</button><button>수정</button><button>삭제</button></div>
      </div>
      <div className="comment-row reply">
        <div className="c-head"><span className="reply-arrow">↳</span> 닉네임B · 2026.06.23 15:02</div>
        <div className="c-body">감사합니다! 해결했습니다.</div>
        <div className="c-actions"><button>수정</button><button>삭제</button></div>
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
  const [state, dispatch] = useReducer(questionDetailReducer, initialState)

  return (
    <div className="main">
      {/* text-link--back: margin-bottom: 16px */}
      <button className="text-link text-link--back" onClick={() => navigate('/board')}>
        ← 게시판 목록
      </button>

      <div className="panel">
        <div className="detail-head">
          <h1>[Spring] 마이페이지 진입 시 500 Internal Server Error</h1>
          <StatusChip solved={false} />
        </div>
        <TagRow tags={['JAVA', 'Spring', 'PostgreSQL']} />
        <div className="detail-meta">
          <span>오지민</span><span>2026.06.23 14:20</span>
        </div>

        <div className="field-block">
          <div className="field-label">상황</div>
          <p>로그인 직후 마이페이지에 진입하면 500 에러가 발생합니다. 로컬에서는 정상 동작하는데 배포 환경에서만 재현돼요.</p>
        </div>
        <div className="field-block">
          <div className="field-label">오류 메시지</div>
          <div className="code-block">
            Exception in thread &quot;main&quot; java.lang.NullPointerException:<br />
            &nbsp;&nbsp;Cannot invoke &quot;User.getUser()&quot; because &quot;user&quot; is null<br />
            &nbsp;&nbsp;at com.troublelog.mypage.MyPageService.getProfile(MyPageService.java:42)
          </div>
        </div>
        <div className="field-block">
          <div className="field-label">시도한 방법</div>
          <p>구글링 · 세션 설정값 변경 · 필터 순서 변경 시도</p>
        </div>
        <div className="field-block">
          <div className="field-label">첨부 이미지</div>
          <div className="attach-box">🖼 첨부한 사진</div>
        </div>

        <div className="detail-footer">
          <button
            className={`icon-btn ${state.liked ? 'liked' : ''}`}
            onClick={() => dispatch({ type: QDETAIL.TOGGLE_POST_LIKE })}
          >
            {state.liked ? '♥' : '♡'} 좋아요 {state.likeCount}
          </button>
          <button className="btn btn-ghost btn-sm">답변 작성</button>
        </div>
      </div>

      <div className="answers-head">
        <h2>답변 <span className="count">{MOCK_ANSWERS.length}</span></h2>
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
                  {isLiked ? '♥' : '♡'} 좋아요 {a.likes + (isLiked ? 1 : 0)}
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