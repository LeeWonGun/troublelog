/**
 * API 호출 공통 처리 유틸
 * 
 * @param {() => Promise<any>} apiCall - axiosInstance 기반 API 함수 호출 (인자는 호출부에서 클로저로 바인딩)
 * @param {Object} handlers
 * @param {(data: any, res: any) => void} [handlers.onSuccess] - res.success === true 일 때 res.data와 함께 호출
 * @param {(message: string) => void} [handlers.onFail] - res.success === false 이거나 예외 발생 시 호출
 * @param {string} [handlers.fallbackMessage] - 서버가 message를 내려주지 않을 때 사용할 기본 문구
 * @param {() => boolean} [handlers.isCancelled] - true를 반환하면 onSuccess/onFail을 호출하지 않음
 *   (useEffect에서 id가 바뀌거나 언마운트된 뒤 늦게 도착한 응답이 상태를 덮어쓰는 것을 방지하기 위한 가드)
 */
export async function requestHandler(
  apiCall,
  { onSuccess, onFail, fallbackMessage = '요청 처리 중 오류가 발생했습니다.', isCancelled } = {},
) {
  try {
    const res = await apiCall()
    if (isCancelled?.()) return

    if (res.success) {
      onSuccess?.(res.data, res)
    } else {
      onFail?.(res.message ?? fallbackMessage)
    }
  } catch (err) {
    if (isCancelled?.()) return

    const message = err.response?.data?.message ?? fallbackMessage
    onFail?.(message)
  }
}