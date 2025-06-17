package com.easyterview.wingterview.user.dto.request;

import com.easyterview.wingterview.user.entity.UserEntity;
import com.easyterview.wingterview.user.validator.annotation.ValidUserBasicInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@ValidUserBasicInfo
public class UserBasicInfoDto {

    // TODO : isKTB에 따른 valid 분기처리

    private Boolean isKTB;

    @Pattern(regexp = "^[가-힣]{2,}$", message = "이름은 한글만 입력 가능합니다.")
    @Size(min = 2, max = 50, message = "이름은 최소 2자 최대 50자까지 입력 가능합니다.")
    private String name;

    private String nickname;

    private String curriculum;

    @NotEmpty(message = "희망 직무 누락")
    @Size(max = 3, message = "희망 직무 갯수 초과")
    private List<String> jobInterest;

    @NotEmpty(message = "희망 직무 누락")
    @Size(max = 3, message = "희망 직무 갯수 초과")
    private List<String> techStack;

    private String profileImageName;

    private SeatPosition seatPosition;
}
