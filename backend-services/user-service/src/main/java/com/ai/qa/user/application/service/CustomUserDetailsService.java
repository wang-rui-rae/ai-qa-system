package com.ai.qa.user.application.service;

import com.ai.qa.user.domain.model.User;
import com.ai.qa.user.domain.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        Optional<User> userOpt;

        // 1. 优先尝试将 identifier 作为 **用户名** 查找（用于登录）
        userOpt = userRepository.findByUsername(identifier);

        if (userOpt.isEmpty()) {
            // 2. 如果不是用户名，尝试将 identifier 作为 **用户 ID** 查找（用于 IdentityHeaderFilter）
            try {
                Long id = Long.valueOf(identifier);
                userOpt = userRepository.findById(id); // 假设您有 findById 方法
            } catch (NumberFormatException e) {
                // 如果 identifier 既不是存在的用户名，又不能转换为数字（用户 ID），则抛出异常
                throw new UsernameNotFoundException("User not found or ID format error: " + identifier);
            }
        }
        // 💡 重点：这里需要支持两种查找：
        // a. 登录时，username 是 email/username (由 SecurityConfig 调用)
        // b. 身份透传时，username 是用户 ID (由 IdentityHeaderFilter 调用)

        // 3. 最终检查用户是否存在
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

        return org.springframework.security.core.userdetails.User.builder()
                // 💡 关键修改：将 Long ID 转换为 String 赋给 username 字段
                // 因为gateway发送到下游的请求是X-User-Id，所以这里需要修改成X-User-Id
                // 也就是不把username字段赋值，而是用id赋值
//                .username(user.getUsername())
                .username(String.valueOf(user.getId()))
            .password(user.getPassword())
            .build();
    }
}
