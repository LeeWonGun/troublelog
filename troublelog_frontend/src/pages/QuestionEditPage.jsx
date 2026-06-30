import { useReducer } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useAppContext } from '../context/AppContext.jsx'
import { WRITE } from '../constants/actionTypes.js'
import StackSelector from '../components/common/StackSelector.jsx'
import writeReducer, { initialState } from '../reducers/writeReducer.js'

// 오류 코드 언어 선택 옵션 (QuestionCreatePage와 동일)
const LANG_OPTIONS = ['Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin', 'Go', 'SQL', '기타']

/**
 * P-FE-BD-20 게시글 수정 페이지
 * QuestionCreatePage와 동일한 폼 구조에 기존 데이터를 pre-fill
 * - 진입: QuestionDetailPage의 "수정" 버튼 클릭 (작성자 본인만 접근 가능)
 * - 완료: /questions/:id 상세 페이지로 이동
 */

// 목업용 임시 데이터 — API 연동 시 useEffect + axiosInstance.get('/questions/:id')로 대체
const MOCK_EXISTING_POST = {
  title: '[Spring] 마이페이지 진입 시 500 Internal Server Error',
  situation: '로그인 직후 마이페이지에 진입하면 500 에러가 발생합니다. 로컬에서는 정상 동작하는데 배포 환경에서만 재현돼요.',
  errorLanguage: 'Java',
  errorCode: 'user.getProfile(); // NullPointerException 발생',
  errorMessage: 'Exception in thread "main" java.lang.NullPointerException:\n  Cannot invoke "User.getUser()" because "user" is null',
  triedMethods: '구글링 · 세션 설정값 변경 · 필터 순서 변경 시도',
  visibility: 'PUBLIC',
  selectedTeamId: '',
  stackToggles: { 'language-Java': true, framework_Spring: true },
  images: ['existing_image_1'],
}

function QuestionEditPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { state: appState } = useAppContext()

  // 기존 게시글 데이터로 초기 상태 pre-fill
  const [state, dispatch] = useReducer(writeReducer, {
    ...initialState,
    ...MOCK_EXISTING_POST,
  })

  const set = field => e => dispatch({ type: WRITE.SET_FIELD, field, value: e.target.value })

  function handleSubmit() {
    // TODO: API 연동 - axiosInstance.put('/questions/:id', { ...state })
    navigate(`/questions/${id}`)
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
          <select
            className="select"
            value={state.errorLanguage}
            onChange={set('errorLanguage')}
            style={{ marginBottom: 8 }}
          >
            <option value="">언어 선택</option>
            {LANG_OPTIONS.map(l => <option key={l} value={l}>{l}</option>)}
          </select>
          <textarea
            className="textarea textarea-mono"
            placeholder="오류 코드를 붙여넣어 주세요"
            value={state.errorCode}
            onChange={set('errorCode')}
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
            <label className="radio-opt">
              <input type="radio" name="visibility" checked={state.visibility === 'TEAM'}
                onChange={() => dispatch({ type: WRITE.SET_VISIBILITY, payload: 'TEAM' })} />
              TEAM (팀 공개)
            </label>
            {state.visibility === 'TEAM' && (
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
          {/* pre-fill된 스택이 chip-toggle.on 상태로 표시됨 */}
          <StackSelector
            toggles={state.stackToggles}
            onToggle={key => dispatch({ type: WRITE.TOGGLE_STACK, payload: key })}
          />
        </div>

        <div className="form-group">
          <label>이미지 첨부 <span className="form-label-hint">(최대 4개)</span></label>
          {/* 기존 이미지 + 추가 박스를 attach-row로 나열 */}
          <div className="attach-row">
            {state.images.map((img, i) => (
              <div key={i} className="attach-box" style={{ position: 'relative' }}>
                🖼 이미지 {i + 1}
                <button
                  style={{ position: 'absolute', top: 4, right: 6, background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                  onClick={() => dispatch({ type: WRITE.REMOVE_IMAGE, payload: i })}
                >✕</button>
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

        <div className="form-actions">
          <button className="btn btn-ghost" onClick={() => navigate(`/questions/${id}`)}>취소</button>
          <button className="btn btn-primary" onClick={handleSubmit}>수정</button>
        </div>
      </div>
    </div>
  )
}

export default QuestionEditPage

/*
 * [고려한 예외 처리 및 보안 사항]
 * - 진입 시 작성자 본인 여부를 서버에서 재검증 필요 (클라이언트 체크만으로는 불충분)
 * - 이미지 최대 4개 제한 — ADD_IMAGE 액션에서 guard 처리됨
 * - MOCK_EXISTING_POST는 임시 데이터, API 연동 시 useEffect + GET 요청으로 대체
 * - 수정 완료 후 상세 페이지로 이동 (id 기반 navigate)
 */
