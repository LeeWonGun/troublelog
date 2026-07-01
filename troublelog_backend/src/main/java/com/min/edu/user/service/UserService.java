package com.min.edu.user.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.user.domain.User;
import com.min.edu.user.dto.NicknameUpdateRequest;
import com.min.edu.user.dto.PasswordUpdateRequest;
import com.min.edu.user.dto.UserResponse;
import com.min.edu.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 내 정보 조회와 계정 정보 변경 비즈니스 로직을 담당하는 Service이다.
 *
 * 삭제된 사용자는 로그인, 중복 검사, 마이페이지 조회 대상에서 제외한다.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 삭제되지 않은 활성 사용자를 조회한다.
     */
    public User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

    /**
     * 현재 로그인 사용자의 내 정보를 응답 DTO로 변환해 반환한다.
     */
    public UserResponse getMyInfo(Long userId) {
        return UserResponse.from(getActiveUser(userId));
    }

    /**
     * 현재 로그인 사용자의 닉네임을 변경한다.
     *
     * 다른 활성 사용자가 사용 중인 닉네임으로는 변경할 수 없다.
     */
    @Transactional
    public UserResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = getActiveUser(userId);

        if (!user.getNickname().equals(request.nickname())
                && userRepository.existsByNicknameAndDelflag(request.nickname(), "N")) {
            throw new BusinessException("이미 사용 중인 닉네임입니다.", ErrorCode.DUPLICATE_NICKNAME);
        }

        user.changeNickname(request.nickname());
        return UserResponse.from(user);
    }

    /**
     * 현재 로그인 사용자의 서비스 비밀번호를 변경한다.
     *
     * Google 계정은 외부 인증을 사용하므로 서비스 비밀번호 변경을 허용하지 않는다.
     */
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateRequest request) {
        User user = getActiveUser(userId);

        if (!user.isLocalUser()) {
            throw new BusinessException(
                    "Google 사용자는 서비스 비밀번호를 변경할 수 없습니다.",
                    ErrorCode.GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED
            );
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
