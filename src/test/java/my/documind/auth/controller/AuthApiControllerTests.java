package my.documind.auth.controller;

import my.documind.auth.jwt.JwtAuthenticationFilter;
import my.documind.auth.jwt.JwtTokenProvider;
import my.documind.config.ApiAuthenticationEntryPoint;
import my.documind.config.CustomSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthApiController.class)
@Import({
        CustomSecurityConfig.class,
        ApiAuthenticationEntryPoint.class,
        JwtAuthenticationFilter.class
})
public class AuthApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("로그인에 성공하면 JWT를 발급한다")
    void shouldReturnAccessToken_whenLoginSucceeds() throws Exception {
        // given
        UserDetails userDetails = User.withUsername("test@test.com")
                .password("password")
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtTokenProvider.createAccessToken(userDetails))
                .thenReturn("access-token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                    "email": "test@test.com",
                                    "password": "password"
                                    }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("로그인에 실패하면 JWT를 발급하지 않는다")
    void shouldReturnUnauthorized_whenLoginFails() throws Exception {
        // given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                    "email": "test@test.com",
                                    "password": "wrong-password"
                                    }"""))
                .andExpect(status().isUnauthorized());
        verify(jwtTokenProvider, never()).createAccessToken(any());
    }
}
