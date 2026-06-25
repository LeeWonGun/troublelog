package com.min.edu.common.response;

/*
 * TroubleLog의 모든 API 응답 형식을 통일하기 위한 공통 응답 객체이다.
 *
 * 프론트엔드는 success, message, errorCode, data 구조를 기준으로
 * 성공/실패 여부와 화면에 표시할 메시지를 일관되게 처리할 수 있다.
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        String errorCode,
        T data
) {

    /*
     * 조회, 생성, 수정 등 응답 데이터가 있는 성공 응답에 사용한다.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data);
    }

    /*
     * 삭제, 로그아웃처럼 별도의 응답 데이터가 필요 없는 성공 응답에 사용한다.
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    /*
     * 예외 처리나 비즈니스 규칙 위반 시 실패 응답에 사용한다.
     */
    public static ApiResponse<Void> fail(String message, String errorCode) {
        return new ApiResponse<>(false, message, errorCode, null);
    }
}