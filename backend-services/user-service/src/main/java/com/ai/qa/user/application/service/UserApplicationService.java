package com.ai.qa.user.application.service;



import com.ai.qa.user.api.dto.UserLoginRequestDto;
import com.ai.qa.user.api.dto.UserLoginResponseDto;
import com.ai.qa.user.api.dto.UserRegisterRequestDto;
import com.ai.qa.user.api.dto.UserRegisterResponseDto;
import com.ai.qa.user.api.exception.BusinessException;
import com.ai.qa.user.api.exception.ErrorCode;
import com.ai.qa.user.application.dto.GetUserInfoResponseDto;
import com.ai.qa.user.application.dto.UpdateNicknameRequestDto;
import com.ai.qa.user.application.dto.UpdateNicknameResponseDto;
import com.ai.qa.user.application.userService;
import com.ai.qa.user.domain.model.User;
import com.ai.qa.user.domain.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;


@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        // 注意：这里注入的是我们在领域层定义的接口，而不是具体的实现
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 更新用户昵称的应用服务方法
     * @param userId 目标用户的ID
     * @param newNickname 新的昵称
     * @return 更新后的信息
     */
    @Transactional // 保证操作的原子性
    public UpdateNicknameResponseDto updateNickname(Long userId, String newNickname) {
        // 1. 从仓库加载聚合根
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 调用聚合根的业务方法来执行操作
        //    所有的业务规则都在User.changeNickname()方法内部执行
        user.changeNickname(newNickname);

        // 3. 将变更后的聚合根交由仓库进行持久化
        userRepository.save(user);

        // 4. 转换成response dto来进行返回
        UpdateNicknameResponseDto updateNicknameResponseDto = new UpdateNicknameResponseDto();
        updateNicknameResponseDto.setUsername(user.getUsername());
        updateNicknameResponseDto.setNickname(user.getNickname());
        return updateNicknameResponseDto;

      }

    public UserLoginResponseDto login(UserLoginRequestDto request){
        // 1. 根据username来判断user是否以及存在
        //    1.1 如果已经存在了的话，处理继续
        //    1.2 如果不存在了的话，抛出异常
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 判断密码是否一致
        // 2. 使用 PasswordEncoder 的 matches 方法判断密码是否一致
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            // 3. 密码匹配成功，生成并返回Token
//            String jwt = jwtService.generateToken(user); // 假设你的User实体实现了UserDetails
//            return ResponseEntity.ok(new AuthenticationResponse(jwt));
            return new UserLoginResponseDto("00");
        } else {
            // 4. 密码不匹配，抛出异常
            throw new BusinessException(ErrorCode.PASSWORD_WRONG);
        }
    }

    /**
     * 用户注册的应用服务方法
     * @param request 目标用户的信息
     * @return 结果
     */
    public UserRegisterResponseDto register(UserRegisterRequestDto request) {
        // 1. 根据username来判断user是否以及存在
        //    1.1 如果已经存在了的话，抛出异常
        //    1.2 如果不存在了的话，处理继续
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTED);
        }

        // 2. 进行service层的调用
        User user = new User();
        user.setUsername(request.getUsername());
        // 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return new UserRegisterResponseDto("00");
    }

    /**
     *
     * @param userId
     * @return
     */
    public GetUserInfoResponseDto getUserById(Long userId){
        // 1. 从仓库加载聚合根
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 转换成response dto来进行返回
        GetUserInfoResponseDto getUserInfoResponseDto = new GetUserInfoResponseDto();
        getUserInfoResponseDto.setUsername(user.getUsername());
        getUserInfoResponseDto.setNickname(user.getNickname());
        return getUserInfoResponseDto;
    }

}