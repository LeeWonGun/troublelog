package com.min.edu.common.exception;

public class BusinessException extends RuntimeException {

    /*
     * 단순한 예외 메시지만으로는 프론트엔드에서 어떤 오류인지 구분하기 어렵다.
     * 따라서 TroubleLog에서 정의한 ErrorCode를 함께 보관하여
     * 공통 에러 응답에 errorCode 값을 내려줄 수 있도록 한다.
     */
    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}