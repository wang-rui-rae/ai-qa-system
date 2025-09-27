package com.ai.qa.user.api.controller;

import com.ai.qa.user.api.dto.*;
import com.ai.qa.user.application.dto.GetUserInfoResponseDto;
import com.ai.qa.user.application.dto.UpdateNicknameRequestDto;
import com.ai.qa.user.application.dto.UpdateNicknameResponseDto;
import com.ai.qa.user.application.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/***
 * 为什么user-service必须也要自己做安全限制？
 * 1。零信任网络 (Zero Trust Network)：在微服务架构中，你必须假设内部网络是不安全的。不能因为一个请求来自API Gateway就完全信任它。万一有其他内部服务被攻破，它可能会伪造请求直接调用user-service，绕过Gateway。如果user-service没有自己的安全防线，它就会被完全暴露。
 * 2。职责分离 (Separation of Concerns)：Gateway的核心职责是路由、限流、熔断和边缘认证。而user-service的核心职责是处理用户相关的业务逻辑，业务逻辑与谁能执行它是密不可分的。授权逻辑是业务逻辑的一部分，必须放在离业务最近的地方。
 * 3。细粒度授权 (Fine-Grained Authorization)：Gateway通常只做粗粒度的授权，比如“USER角色的用户可以访问/api/users/**这个路径”。但它无法知道更精细的业务规则，例如：
 * GET /api/users/{userId}: 用户123是否有权查看用户456的资料？
 * PUT /api/users/{userId}: 只有用户自己或者管理员才能修改用户信息。
 * 这些判断必须由user-service结合自身的业务逻辑和数据来完成。
 */
@RestController
@RequestMapping("/api/user")
@Tag(name="用户管理",description = "提供用户相关接口")
public class UserController {

    private final UserApplicationService userApplicationService;

    @Autowired
    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 更新用户昵称的API端点
     *
     * @param userId  从URL路径中获取的用户ID
     * @param request 包含新昵称的请求体
     * @return 返回更新后的用户信息和HTTP状态码200 (OK)
     */
    @PostMapping("/{userId}/nickname")
    @Operation(summary = "=用户昵称更新", description = "该接口用于更新用户昵称")
    public ApiResponse<UpdateNicknameResponseDto> updateNickname(
            @PathVariable Long userId,
            @RequestBody UpdateNicknameRequestDto request) {

        // 控制器只负责调用应用层，不处理业务逻辑
        UpdateNicknameResponseDto updateNicknameResponseDto = userApplicationService.updateNickname(userId, request.getNickname());
        return ApiResponse.success(updateNicknameResponseDto);
    }

    /**
     * 根据id获取user信息的API端点
     *
     * @param userId 从URL路径中获取的用户ID
     * @return 返回用户信息和HTTP状态码200 (OK)
     */
    @Operation(summary = "=获取用户ID", description = "该接口用于获取用户ID")
    @GetMapping("/{userId}")
    public ApiResponse<GetUserInfoResponseDto> getUserById(@PathVariable("userId") Long userId) {
        GetUserInfoResponseDto getUserInfoResponseDto = userApplicationService.getUserById(userId);
        return ApiResponse.success(getUserInfoResponseDto);
    }



    // TODO jwt
    /**
     * 根据username和password获取user信息的API端点
     *
     * @param request 包含用户信息的请求体
     * @return 返回resultCode(00)和HTTP状态码200 (OK)
     */
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody UserLoginRequestDto request) {
        UserLoginResponseDto userLoginResponseDto = userApplicationService.login(request);
        return new UserLoginResponseDto("00");
    }

    /**
     * 注册用户信息的API端点
     *
     * @param request 包含用户信息的请求体
     * @return 返回resultCode(00)和HTTP状态码200 (OK)
     */
    @PostMapping("/register")
    public ApiResponse<UserRegisterResponseDto> register(@Valid @RequestBody UserRegisterRequestDto request) {
        UserRegisterResponseDto userRegisterResponseDto = userApplicationService.register(request);
        return ApiResponse.success(userRegisterResponseDto);
    }

}
