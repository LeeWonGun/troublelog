package com.min.edu.common.exception;

import com.min.edu.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * 서비스 계층에서 직접 발생시키는 비즈니스 예외를 처리한다.
     *
     * 예를 들어 팀장이 팀 탈퇴를 시도하거나,
     * 채택된 답변을 삭제하려고 하는 경우처럼
     * TroubleLog 정책상 허용되지 않는 요청을 공통 응답 형식으로 변환한다.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.fail(e.getMessage(), e.getErrorCode().name());
    }

    /*
     * @Valid 검증 실패 시 발생하는 예외를 처리한다.
     *
     * 어떤 필드에서 검증 오류가 발생했는지 프론트엔드에서 표시할 수 있도록
     * 필드명과 오류 메시지를 Map 형태로 내려준다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (oldValue, newValue) -> oldValue
                ));

        return new ApiResponse<>(
                false,
                "입력값이 올바르지 않습니다.",
                ErrorCode.VALIDATION_ERROR.name(),
                errors
        );
    }

    /*
     * 예상하지 못한 서버 내부 오류를 처리한다.
     *
     * 실제 운영 환경에서는 예외 상세 메시지를 그대로 응답하지 않고,
     * 서버 로그에만 기록하는 방식으로 처리하는 것이 안전하다.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        return ApiResponse.fail(
                "서버 내부 오류가 발생했습니다.",
                ErrorCode.INTERNAL_SERVER_ERROR.name()
        );
    }
    
    /*
     * 업로드 파일이 서버 최대 용량(5MB)을 초과할 때 발생하는 예외를 처리한다.
     * Controller에 도달하기 전 서블릿 단계에서 던져지므로 별도로 잡아 FILE_SIZE_EXCEEDED로 통일한다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return ApiResponse.fail("파일 크기는 5MB를 초과할 수 없습니다.", ErrorCode.FILE_SIZE_EXCEEDED.name());
    }
}
