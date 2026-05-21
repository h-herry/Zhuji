package com.zhuji.userorg.controller;

import com.zhuji.common.core.result.ApiResponse;
import com.zhuji.userorg.dto.CreateUserRequest;
import com.zhuji.userorg.dto.LoginRequest;
import com.zhuji.userorg.dto.LoginResponse;
import com.zhuji.userorg.service.UserService;
import com.zhuji.userorg.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证接口", description = "登录、注册、注销")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody CreateUserRequest request) {
        UserVO user = userService.register(request);
        return ApiResponse.success(user);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        userService.logout(userId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        return ApiResponse.success(null);
    }
}