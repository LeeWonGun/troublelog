package com.min.edu.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamJoinRequest(
        @NotBlank(message = "팀 코드를 입력해 주세요.")
        @Size(max = 50, message = "팀 코드는 50자 이하로 입력해 주세요.")
        String teamCode
) {
}
