import { APP } from '../../constants/actionTypes.js'
import StackSelector from '../common/StackSelector.jsx'
import ModalOverlay from '../common/ModalOverlay.jsx'

function SearchDetailModal({ state, dispatch }) {
  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  return (
    <ModalOverlay onClose={close} wide>
      <h3>상세 검색</h3>
      <div className="form-group form-group--mt">
        <input
          className="input"
          placeholder="검색어를 입력하세요"
          value={state.searchKeyword}
          onChange={e => dispatch({ type: APP.SET_SEARCH_KEYWORD, payload: e.target.value })}
        />
      </div>

      <div className="field-label field-label--mb">기술 스택</div>
      <StackSelector
        toggles={state.searchStackToggles}
        onToggle={key => dispatch({ type: APP.TOGGLE_SEARCH_STACK, payload: key })}
      />

      {/* 상태 필터: 전체 / 해결됨 / 미해결 */}
      <div className="field-label field-label--spaced">상태</div>
      <div className="radio-row">
        {[['all', '전체'], ['solved', '해결됨'], ['unsolved', '미해결']].map(([val, label]) => (
          <label key={val} className="radio-opt">
            <input
              type="radio"
              name="search-status"
              checked={state.searchStatus === val}
              onChange={() => dispatch({ type: APP.SET_SEARCH_STATUS, payload: val })}
            />
            {label}
          </label>
        ))}
      </div>

      <div className="modal-actions modal-actions--spread">
        <button className="btn btn-ghost" onClick={() => dispatch({ type: APP.RESET_SEARCH_FILTERS })}>초기화</button>
        <button className="btn btn-primary" onClick={close}>검색 적용</button>
      </div>
    </ModalOverlay>
  )
}

export default SearchDetailModal