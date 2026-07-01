package com.min.edu.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
        @NotBlank(message = "팀 이름을 입력해 주세요.")
        @Size(max = 100, message = "팀 이름은 100자 이하로 입력해 주세요.")
        String name,

        String description
) {
}
