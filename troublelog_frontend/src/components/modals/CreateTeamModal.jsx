import { useRef, useState } from 'react'
import { APP } from '../../constants/actionTypes.js'
import { createTeam } from '../../api/teamApi.js'
import { requestHandler } from '../../util/requestHandler.js'
import ModalOverlay from '../common/ModalOverlay.jsx'

function CreateTeamModal({ dispatch }) {
  const teamNameRef = useRef(null)
  const teamDescRef = useRef(null)
  const [teamNameError, setTeamNameError] = useState(false)

  const close = () => dispatch({ type: APP.CLOSE_MODAL })

  async function createTeamFn() {
    const name = teamNameRef.current?.value?.trim()
    const description = teamDescRef.current?.value?.trim()

    if (!name || name.length > 100) {
      setTeamNameError(true)
      return
    }

    setTeamNameError(false)

    requestHandler(() => createTeam({ name, description }), {
      onSuccess: (data) => dispatch({ type: APP.ADD_TEAM, payload: data }),
      onFail: (message) => console.warn('[CreateTeamModal] 팀 생성 실패:', message),
      showGlobalError: true,
    })
  }

  return (
    <ModalOverlay onClose={close}>
      <h3>팀 생성</h3>
      <div className="form-group">
        <label>팀 이름 <span className="form-label-hint">(필수, 최대 100자)</span></label>
        <input className="input" placeholder="팀 이름을 입력해 주세요" ref={teamNameRef} maxLength={100} />
        {teamNameError && <div className="error-msg">팀 이름을 입력하세요.</div>}
      </div>
      <div className="form-group">
        <label>팀 설명 <span className="form-label-hint">(선택)</span></label>
        <textarea className="textarea textarea--sm" placeholder="팀을 간단히 소개해 주세요" ref={teamDescRef} />
      </div>
      <div className="modal-actions">
        <button className="btn btn-ghost" onClick={close}>취소</button>
        <button className="btn btn-primary" onClick={createTeamFn}>생성</button>
      </div>
    </ModalOverlay>
  )
}

export default CreateTeamModal