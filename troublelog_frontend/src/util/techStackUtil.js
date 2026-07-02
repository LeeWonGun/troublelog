// GET /api/tech-stacks 응답([{ techStackId, name, category }])을
// StackSelector가 사용하는 카테고리 그룹 형태([{ cat, items }])로 변환
export const groupTechStacksByCategory = (techStacks = []) => {
  const grouped = new Map() // Map 사용: 서버 응답 순서대로 카테고리 노출

  techStacks.forEach(stack => {
    if (!grouped.has(stack.category)) grouped.set(stack.category, [])
    grouped.get(stack.category).push(stack)
  })

  return [...grouped.entries()].map(([cat, items]) => ({ cat, items }))
}