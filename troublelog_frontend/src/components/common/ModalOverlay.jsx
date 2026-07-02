// 모든 모달이 공통으로 쓰는 배경(backdrop) + 컨테이너
function ModalOverlay({ onClose, wide, children }) {
  function handleBackdrop(e) {
    if (e.target === e.currentTarget) onClose()
  }

  return (
    <div className="modal-overlay" onClick={handleBackdrop}>
      <div className={`modal${wide ? ' modal--wide' : ''}`}>
        {children}
      </div>
    </div>
  )
}

export default ModalOverlay