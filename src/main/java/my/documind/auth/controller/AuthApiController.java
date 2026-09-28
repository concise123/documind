package my.documind.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.documind.auth.dto.ApiLoginRequest;
import my.documind.auth.dto.ApiLoginResponse;
import my.documind.auth.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authorization", description = "인증 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로그인", description = "사용자 인증에 성공하면 JWT 액세스 토큰을 발급한다.")
    @PostMapping("/login")
    public ResponseEntity<ApiLoginResponse> login(@Valid @RequestBody ApiLoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        String accessToken = jwtTokenProvider.createAccessToken((UserDetails) authentication.getPrincipal());
        ApiLoginResponse response = new ApiLoginResponse(accessToken);
        return ResponseEntity.ok(response);
    }
}
