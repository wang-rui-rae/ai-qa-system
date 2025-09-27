package com.ai.qa.user.application.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户更新昵称请求")
public class UpdateNicknameRequestDto {

    @Schema(description = "昵称", example = "nickname1", requiredProperties = "true")
    private String nickname;

}
