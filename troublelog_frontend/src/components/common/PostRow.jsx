import { useNavigate } from 'react-router-dom'
import StatusChip from './StatusChip.jsx'
import TagRow from './TagRow.jsx'

function PostRow({ post }) {
  const navigate = useNavigate()
  return (
    <div className="post-row" onClick={() => navigate(`/questions/${post.id}`)}>
      <div className="row-top">
        <span className="title">{post.title}</span>
        <StatusChip solved={post.solved} />
      </div>
      <div className="row-bottom">
        <TagRow tags={post.tags} />
        <div className="meta-right">
          <span>♡ {post.likes}</span>
        </div>
      </div>
    </div>
  )
}

export default PostRow