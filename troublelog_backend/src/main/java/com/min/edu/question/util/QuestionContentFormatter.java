package com.min.edu.question.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestionContentFormatter {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile(
            "(?s)```([a-zA-Z0-9#+-]+)\\R(.*?)\\R```"
    );

    private QuestionContentFormatter() {
    }

    public static String compose(String content, String codeLanguage, String code) {
        String safeContent = trimToEmpty(content);
        String safeCode = trimToEmpty(code);
        String safeLanguage = trimToEmpty(codeLanguage).toLowerCase();

        if (safeCode.isBlank()) {
            return safeContent;
        }

        if (safeLanguage.isBlank()) {
            safeLanguage = "text";
        }

        /*
         * questions.content는 별도 코드 컬럼을 두지 않고 Markdown 문자열로 저장한다.
         * 코드 언어 정보는 ```java 같은 코드블록 언어 태그로 관리한다.
         */
        return safeContent + "\n\n```" + safeLanguage + "\n" + safeCode + "\n```";
    }

    public static QuestionContentParts parse(String storedContent) {
        String safeStoredContent = trimToEmpty(storedContent);

        if (safeStoredContent.isBlank()) {
            return new QuestionContentParts("", null, null);
        }

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(safeStoredContent);

        if (!matcher.find()) {
            return new QuestionContentParts(safeStoredContent, null, null);
        }

        String codeLanguage = matcher.group(1);
        String code = matcher.group(2);

        /*
         * MVP에서는 질문 본문에 포함된 첫 번째 코드블록을 Monaco 코드 입력 영역으로 복원한다.
         * 여러 코드블록을 별도로 관리하려면 추후 question_code_blocks 같은 별도 테이블이 필요하다.
         */
        String content = matcher.replaceFirst("").trim();

        return new QuestionContentParts(
                content,
                codeLanguage,
                code
        );
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}