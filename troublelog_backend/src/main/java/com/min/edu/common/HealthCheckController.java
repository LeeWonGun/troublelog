package com.min.edu.common;

import com.min.edu.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("서버가 정상적으로 실행 중입니다.", "OK");
    }
}