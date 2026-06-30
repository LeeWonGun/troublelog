function TagRow({ tags = [] }) {
  return (
    <div className="tag-row">
      {tags.map(t => <span key={t} className="tag">{t}</span>)}
    </div>
  )
}

export default TagRow