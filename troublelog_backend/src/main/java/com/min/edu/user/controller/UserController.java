package com.min.edu.user.controller;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.user.dto.NicknameUpdateRequest;
import com.min.edu.user.dto.PasswordUpdateRequest;
import com.min.edu.user.dto.UserResponse;
import com.min.edu.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지에서 사용하는 내 정보 조회와 계정 정보 변경 API를 제공하는 Controller이다.
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 현재 로그인 사용자의 내 정보를 조회한다.
     */
    @GetMapping
    public ApiResponse<UserResponse> getMyInfo(Authentication authentication) {
        return ApiResponse.success("내 정보 조회가 완료되었습니다.", userService.getMyInfo(CurrentUser.id(authentication)));
    }

    /**
     * 현재 로그인 사용자의 닉네임을 변경한다.
     *
     * 삭제되지 않은 다른 사용자의 닉네임과 중복될 수 없다.
     */
    @PatchMapping("/nickname")
    public ApiResponse<UserResponse> updateNickname(
            Authentication authentication,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        return ApiResponse.success("닉네임이 변경되었습니다.", userService.updateNickname(CurrentUser.id(authentication), request));
    }

    /**
     * 현재 로그인 사용자의 서비스 비밀번호를 변경한다.
     *
     * Google 로그인 사용자는 서비스 비밀번호가 없으므로 변경할 수 없다.
     */
    @PatchMapping("/password")
    public ApiResponse<Void> updatePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        userService.updatePassword(CurrentUser.id(authentication), request);
        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }
}
