// 백엔드 QuestionListResponse(questionId, status, techStacks, likeCount ...) 를
// PostRow 등 화면 컴포넌트가 기대하는 필드(id, solved, tags, likes ...)로 변환하는 매퍼
export const mapQuestionListItem = (item) => ({
  id: item.questionId,
  title: item.title,
  solved: item.status === 'SOLVED',
  tags: item.techStacks?.map(stack => stack.name) ?? [],
  likes: item.likeCount,
  viewCount: item.viewCount,
  answerCount: item.answerCount,
  createdAt: item.createdAt,
  writerNickname: item.writerNickname,
})