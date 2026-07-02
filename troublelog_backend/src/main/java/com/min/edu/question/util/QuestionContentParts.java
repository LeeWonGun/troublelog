package com.min.edu.question.util;

/**
 * 저장된 질문 Markdown 본문을 상황 설명과 코드 정보로 분리한 값 객체입니다.
 */
public record QuestionContentParts(
        String content,
        String codeLanguage,
        String code
) {
}
