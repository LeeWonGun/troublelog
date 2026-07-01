package com.min.edu.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣]{2,50}$",
                message = "닉네임은 특수문자 없이 2~50자로 입력해 주세요."
        )
        String nickname
) {
}
