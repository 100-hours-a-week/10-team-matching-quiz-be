package com.easyterview.wingterview.user.dto.request;

import com.easyterview.wingterview.user.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class UserBasicInfoDto {

    @Pattern(regexp = "^[가-힣]{2,}$", message = "이름은 한글만 입력 가능합니다.")
    @Size(min = 2, max = 50, message = "이름은 최소 2자 최대 50자까지 입력 가능합니다.")
    private String name;

    @Pattern(
            regexp = "^[a-z]{2,}\\.[a-z]{2,}$",
            message = "닉네임은 소문자로 2글자 이상 영어이름.영어성 형식으로 작성해주세요. 예: joy.lee"
    )
    @Size(min = 2, max = 50, message = "닉네임은 최소 2자 최대 50자까지 입력 가능합니다.")
    private String nickname;

    @Pattern(
            regexp = "^[가-힣]{3,4}\\s[가-힣]{2}$",
            message = "과정명 형식 오류"
    )
    @NotEmpty(message = "커리큘럼 누락")
    private String curriculum;

    @NotEmpty(message = "희망 직무 누락")
    @Size(max = 3, message = "희망 직무 갯수 초과")
    private List<String> jobInterest;

    @NotEmpty(message = "희망 직무 누락")
    @Size(max = 3, message = "희망 직무 갯수 초과")
    private List<String> techStack;

    @Pattern(
            regexp = "^(https?://).+\\..+",
            message = "URL 형식이 유효하지 않음"
    )
    private String profileImageUrl;

    private SeatPosition seatPosition;
}
