import { useEffect, useReducer } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { WRITE } from '../constants/actionTypes.js'
import StackSelector from '../components/common/StackSelector.jsx'
import CodeEditor from '../components/common/CodeEditor.jsx'
import writeReducer, { initialState } from '../reducers/writeReducer.js'
import { getQuestion, updateQuestion } from '../api/questionApi.js'
import { requestHandler } from '../util/requestHandler.js'
import { validateQuestionForm, buildQuestionPayload, buildFormStateFromDetail } from '../util/questionForm.js'
import { groupTechStacksByCategory } from '../util/techStackUtil.js'


function QuestionEditPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { state: appState } = useAppContext()

  // 수정 페이지는 기존 게시글을 불러올 때까지 loading 상태로 시작
  const [state, dispatch] = useReducer(writeReducer, { ...initialState, loading: true })

  // 소속 팀이 없으면 TEAM 공개 선택 자체를 막는다 (작성 페이지와 동일 정책)
  const hasTeams = appState.teams.length > 0

  const set = field => e => dispatch({ type: WRITE.SET_FIELD, field, value: e.target.value })
  // Monaco 등 event가 아닌 값을 직접 넘기는 컴포넌트용
  const setValue = field => value => dispatch({ type: WRITE.SET_FIELD, field, value })

  // 기존 게시글 조회 -> 폼 상태 pre-fill
  useEffect(() => {
    let ignore = false // 늦게 도착한 응답이 최신 입력값을 덮어쓰지 않도록 막는 가드

    requestHandler(() => getQuestion(id), {
      isCancelled: () => ignore,
      onSuccess: (data) => dispatch({ type: WRITE.PREFILL, payload: buildFormStateFromDetail(data) }),
      onFail: (message) => dispatch({ type: WRITE.LOAD_FAILED, payload: message }),
      fallbackMessage: '게시글을 불러오지 못했습니다.',
    })

    return () => { ignore = true }
  }, [id])

  function handleSubmit() {
    // 클라이언트 유효성 검증 - 실패 시 API 호출 없이 즉시 에러 표시
    const error = validateQuestionForm(state)
    if (error) {
      dispatch({ type: WRITE.SET_ERROR, payload: error })
      return
    }
    dispatch({ type: WRITE.SET_ERROR, payload: '' })

    // 작성자 본인 여부는 서버가 재검증한다 (본인이 아니면 실패 응답)
    requestHandler(() => updateQuestion(id, buildQuestionPayload(state)), {
      onSuccess: () => navigate(`/questions/${id}`),
      onFail: (message) => dispatch({ type: WRITE.SET_ERROR, payload: message }),
      fallbackMessage: '게시글 수정에 실패했습니다.',
    })
  }

  // 기존 게시글 로딩 중 / 조회 실패 화면
  if (state.loading) {
    return (
      <div className="main">
        <div className="panel" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>불러오는 중...</div>
      </div>
    )
  }

  if (state.loadError) {
    return (
      <div className="main">
        <button className="text-link text-link--back" onClick={() => navigate('/board')}>
          &#8592; 게시판 목록
        </button>
        <div className="panel">{state.loadError}</div>
      </div>
    )
  }

  return (
    <div className="main">
      <div className="page-head">
        <h1><span className="eyebrow">edit entry</span> 게시글 수정</h1>
      </div>

      {/* panel--write: max-width 760px */}
      <div className="panel panel--write">
        <div className="form-group">
          <label>제목 <span className="form-label-hint">(최대 100자)</span></label>
          <input className="input" placeholder="어떤 문제가 발생했나요?" value={state.title} onChange={set('title')} />
        </div>

        <div className="form-group">
          <label>내용</label>
          <textarea className="textarea" placeholder="문제가 발생한 상황을 자유롭게 적어주세요." value={state.situation} onChange={set('situation')} />
        </div>

        {/* 오류 코드 섹션: 언어 선택 + 코드 입력 */}
        <div className="form-group">
          <label>오류 코드 <span className="form-label-hint">(선택)</span></label>
          <CodeEditor
            language={state.errorLanguage}
            code={state.errorCode}
            onLanguageChange={setValue('errorLanguage')}
            onCodeChange={setValue('errorCode')}
          />
        </div>

        <div className="form-group">
          <label>오류 메시지 <span className="form-label-hint">(선택)</span></label>
          <textarea className="textarea textarea-mono" placeholder="발생한 에러 로그를 붙여넣어 주세요" value={state.errorMessage} onChange={set('errorMessage')} />
        </div>

        <div className="form-group">
          <label>시도한 방법 <span className="form-label-hint">(선택)</span></label>
          <textarea className="textarea" placeholder="이미 시도해본 방법이 있다면 적어주세요" value={state.triedMethods} onChange={set('triedMethods')} />
        </div>

        <div className="form-group">
          <label>공개 범위</label>
          <div className="radio-row">
            <label className="radio-opt">
              <input type="radio" name="visibility" checked={state.visibility === 'PUBLIC'}
                onChange={() => dispatch({ type: WRITE.SET_VISIBILITY, payload: 'PUBLIC' })} />
              PUBLIC (전체 공개)
            </label>
            {/* 소속 팀이 없으면 비활성화 */}
            <label
              className={`radio-opt ${!hasTeams ? 'radio-opt--disabled' : ''}`}
              title={!hasTeams ? '소속된 팀이 없습니다' : undefined}
            >
              <input type="radio" name="visibility" disabled={!hasTeams} checked={state.visibility === 'TEAM'}
                onChange={() => dispatch({ type: WRITE.SET_VISIBILITY, payload: 'TEAM' })} />
              TEAM (팀 공개)
            </label>
            {state.visibility === 'TEAM' && (
              <select
                className="select select--team"
                value={state.selectedTeamId}
                onChange={set('selectedTeamId')}
              >
                <option value="">팀 선택</option>
                {appState.teams.map(t => <option key={t.teamId} value={t.teamId}>{t.name}</option>)}
              </select>
            )}
          </div>
        </div>

        <div className="form-group">
          <label>기술 스택</label>
          
          <StackSelector
            categories={groupTechStacksByCategory(appState.techStacks)}
            toggles={state.stackToggles}
            onToggle={key => dispatch({ type: WRITE.TOGGLE_STACK, payload: key })}
          />
        </div>

        <div className="form-group">
          <label>이미지 첨부 <span className="form-label-hint">(최대 4개)</span></label>
          {/* TODO: fileApi 연동 전까지 목업 유지 (QuestionDetailResponse에 이미지 필드 없음) */}
          <div className="attach-row">
            {state.images.map((img, i) => (
              <div key={i} className="attach-box" style={{ position: 'relative' }}>
                {'🖼'} 이미지 {i + 1}
                <button
                  style={{ position: 'absolute', top: 4, right: 6, background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                  onClick={() => dispatch({ type: WRITE.REMOVE_IMAGE, payload: i })}
                >&#x2715;</button>
              </div>
            ))}
            {state.images.length < 4 && (
              <div
                className="attach-box"
                style={{ cursor: 'pointer' }}
                onClick={() => dispatch({ type: WRITE.ADD_IMAGE, payload: `image_${Date.now()}` })}
              >
                + 이미지 첨부
              </div>
            )}
          </div>
        </div>

        {state.error && <div className="alert-banner">{state.error}</div>}

        <div className="form-actions">
          <button className="btn btn-ghost" onClick={() => navigate(`/questions/${id}`)}>취소</button>
          <button className="btn btn-primary" onClick={handleSubmit}>수정</button>
        </div>
      </div>
    </div>
  )
}

export default QuestionEditPage