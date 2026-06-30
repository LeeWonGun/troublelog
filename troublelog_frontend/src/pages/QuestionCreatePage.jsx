import { useReducer } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { WRITE } from '../constants/actionTypes.js'
import StackSelector from '../components/common/StackSelector.jsx'
import writeReducer, { initialState } from '../reducers/writeReducer.js'

function QuestionCreatePage() {
  const navigate = useNavigate()
  const { state: appState } = useAppContext()
  const [state, dispatch] = useReducer(writeReducer, initialState)

  const set = field => e => dispatch({ type: WRITE.SET_FIELD, field, value: e.target.value })

  function handleSubmit() {
    // TODO: API 연동 - axiosInstance.post('/questions', { ...state })
    navigate('/board')
  }

  return (
    <div className="main">
      <div className="page-head">
        <h1><span className="eyebrow">new entry</span> 게시글 작성</h1>
      </div>

      {/* panel--write: max-width 760px */}
      <div className="panel panel--write">
        <div className="form-group">
          <label>제목 <span className="form-label-hint">(최대 100자)</span></label>
          <input className="input" placeholder="어떤 문제가 발생했나요?" value={state.title} onChange={set('title')} />
        </div>

        <div className="form-group">
          <label>상황</label>
          <textarea className="textarea" placeholder="문제가 발생한 상황을 자유롭게 적어주세요." value={state.situation} onChange={set('situation')} />
        </div>

        <div className="form-group">
          <label>오류 메시지 <span className="form-label-hint">(선택)</span></label>
          {/* textarea-mono: font-family mono + font-size 12.5px */}
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
            <label className="radio-opt">
              <input type="radio" name="visibility" checked={state.visibility === 'TEAM'}
                onChange={() => dispatch({ type: WRITE.SET_VISIBILITY, payload: 'TEAM' })} />
              TEAM (팀 공개)
            </label>
            {state.visibility === 'TEAM' && (
              /* select--team: width 140px */
              <select
                className="select select--team"
                value={state.selectedTeamId}
                onChange={e => dispatch({ type: WRITE.SET_FIELD, field: 'selectedTeamId', value: e.target.value })}
              >
                <option value="">팀 선택</option>
                {appState.teams.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
            )}
          </div>
        </div>

        <div className="form-group">
          <label>기술 스택</label>
          <StackSelector
            toggles={state.stackToggles}
            onToggle={key => dispatch({ type: WRITE.TOGGLE_STACK, payload: key })}
          />
        </div>

        <div className="form-group">
          <label>이미지 첨부 <span className="form-label-hint">(최대 4개)</span></label>
          {/* attach-box--full: width 100%, height 64px, cursor pointer */}
          <div className="attach-box attach-box--full">+ 이미지 첨부</div>
        </div>

        <div className="form-actions">
          <button className="btn btn-ghost" onClick={() => navigate(-1)}>취소</button>
          <button className="btn btn-primary" onClick={handleSubmit}>작성</button>
        </div>
      </div>
    </div>
  )
}

export default QuestionCreatePage