package com.min.edu.team;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.min.edu.auth.security.JwtCookieService;
import com.min.edu.user.domain.AuthProvider;
import com.min.edu.user.domain.User;
import com.min.edu.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeamApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void createJoinListMembersLeaveAndDeleteTeam() throws Exception {
        createUser("leader@example.com", "leaderNick");
        createUser("member@example.com", "memberNick");
        createUser("outsider@example.com", "outsiderNick");

        Cookie leaderAccessTokenCookie = loginAndGetAccessTokenCookie("leader@example.com", "Abcd1234!");
        Cookie memberAccessTokenCookie = loginAndGetAccessTokenCookie("member@example.com", "Abcd1234!");
        Cookie outsiderAccessTokenCookie = loginAndGetAccessTokenCookie("outsider@example.com", "Abcd1234!");

        String createResponse = mockMvc.perform(post("/api/teams")
                        .cookie(leaderAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "백엔드 스터디팀",
                                  "description": "알고리즘 스터디"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("LEADER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        Long teamId = createJson.at("/data/teamId").asLong();
        String teamCode = createJson.at("/data/teamCode").asText();

        mockMvc.perform(post("/api/teams/join")
                        .cookie(memberAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teamCode": "%s"
                                }
                                """.formatted(teamCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        mockMvc.perform(get("/api/teams/%d".formatted(teamId))
                        .cookie(leaderAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamId").value(teamId))
                .andExpect(jsonPath("$.data.role").value("LEADER"));

        mockMvc.perform(get("/api/teams/%d".formatted(teamId))
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamId").value(teamId))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        mockMvc.perform(get("/api/teams/%d".formatted(teamId))
                        .cookie(outsiderAccessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        mockMvc.perform(get("/api/teams/code/%s".formatted(teamCode))
                        .cookie(outsiderAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamId").value(teamId))
                .andExpect(jsonPath("$.data.teamCode").value(teamCode))
                .andExpect(jsonPath("$.data.role").doesNotExist());

        mockMvc.perform(get("/api/teams/code/%s".formatted(teamCode))
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.teamId").value(teamId))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        mockMvc.perform(get("/api/teams/code/NOPE99")
                        .cookie(outsiderAccessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TEAM_CODE"));

        mockMvc.perform(post("/api/teams/join")
                        .cookie(memberAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teamCode": "%s"
                                }
                                """.formatted(teamCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_JOINED_TEAM"));

        mockMvc.perform(get("/api/teams/my")
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].teamId").value(teamId));

        mockMvc.perform(get("/api/teams/%d/members".formatted(teamId))
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(patch("/api/teams/%d/leave".formatted(teamId))
                        .cookie(leaderAccessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CANNOT_LEAVE_LEADER"));

        mockMvc.perform(patch("/api/teams/%d/leave".formatted(teamId))
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/teams/join")
                        .cookie(memberAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teamCode": "%s"
                                }
                                """.formatted(teamCode)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/teams/%d".formatted(teamId))
                        .cookie(memberAccessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        mockMvc.perform(delete("/api/teams/%d".formatted(teamId))
                        .cookie(leaderAccessTokenCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teams/code/%s".formatted(teamCode))
                        .cookie(outsiderAccessTokenCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TEAM_CODE"));

        mockMvc.perform(post("/api/teams/join")
                        .cookie(memberAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teamCode": "%s"
                                }
                                """.formatted(teamCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TEAM_CODE"));
    }

    private void createUser(String email, String nickname) {
        userRepository.save(new User(
                email,
                passwordEncoder.encode("Abcd1234!"),
                nickname,
                AuthProvider.LOCAL,
                null
        ));
    }

    private Cookie loginAndGetAccessTokenCookie(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.ACCESS_TOKEN_COOKIE_NAME);
    }
}
