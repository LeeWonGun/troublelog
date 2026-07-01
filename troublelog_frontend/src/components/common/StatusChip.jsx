function StatusChip({ solved }) {
  if (solved) {
    return <span className="chip chip--ok"><span className="dot" />해결</span>
  }
  return <span className="chip chip--err"><span className="dot" />미해결</span>
}

export default StatusChip