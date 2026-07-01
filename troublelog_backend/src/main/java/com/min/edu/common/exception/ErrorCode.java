package com.min.edu.common.exception;

public enum ErrorCode {

    /*
     * 공통 오류 코드
     * 특정 기능에 종속되지 않는 기본적인 요청/인증/권한/서버 오류를 표현한다.
     */
    VALIDATION_ERROR,
    INVALID_REQUEST,
    UNAUTHORIZED,
    INVALID_TOKEN,
    FORBIDDEN,
    NOT_FOUND,
    DUPLICATE_RESOURCE,
    INTERNAL_SERVER_ERROR,

    /*
     * 사용자 / 인증 관련 오류 코드
     */
    USER_NOT_FOUND,
    DUPLICATE_EMAIL,
    DUPLICATE_NICKNAME,
    INVALID_PASSWORD,
    INVALID_AUTH_PROVIDER,
    GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED,
    INVALID_VERIFICATION_CODE,
    EXPIRED_VERIFICATION_CODE,
    MAIL_SEND_FAILED,

    /*
     * 팀 관련 오류 코드
     */
    TEAM_NOT_FOUND,
    INVALID_TEAM_CODE,
    ALREADY_JOINED_TEAM,
    TEAM_MEMBER_NOT_FOUND,
    CANNOT_LEAVE_LEADER,

    /*
     * 질문 / 답변 관련 오류 코드
     */
    QUESTION_NOT_FOUND,
    ANSWER_NOT_FOUND,
    ONLY_DEPTH_ZERO_CAN_BE_ACCEPTED,
    ACCEPTED_ANSWER_CANNOT_DELETE,

    /*
     * 좋아요 관련 오류 코드
     */
    ALREADY_LIKED,
    LIKE_NOT_FOUND,

    /*
     * 파일 관련 오류 코드
     */
    INVALID_FILE_TYPE,
    FILE_SIZE_EXCEEDED,
    FILE_NOT_FOUND
}
