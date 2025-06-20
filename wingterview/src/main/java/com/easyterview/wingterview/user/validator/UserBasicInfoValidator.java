package com.easyterview.wingterview.user.validator;

import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.validator.annotation.ValidUserBasicInfo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserBasicInfoValidator implements ConstraintValidator<ValidUserBasicInfo, UserBasicInfoDto> {

    @Override
    public boolean isValid(UserBasicInfoDto dto, ConstraintValidatorContext context) {
        // isKTB가 false면 나머지 필드는 체크 안 함
        if (!dto.getIsKTB()) {
            if (dto.getNickname() != null && (dto.getNickname().length() < 2 || dto.getNickname().length() > 50)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("닉네임은 최소 2자, 최대 50자까지 입력 가능합니다.")
                        .addPropertyNode("nickname").addConstraintViolation();
                return false;
            }

            return true;
        }

        boolean valid = true;

        // 닉네임 검증
        if (dto.getNickname() == null || !dto.getNickname().matches("^[a-z]{2,}\\.[a-z]{2,}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("닉네임은 소문자 영어이름.영어성 형식 (예: joy.lee)이어야 합니다.")
                    .addPropertyNode("nickname").addConstraintViolation();
            valid = false;
        }

        if (dto.getNickname() != null && (dto.getNickname().length() < 2 || dto.getNickname().length() > 50)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("닉네임은 최소 2자, 최대 50자까지 입력 가능합니다.")
                    .addPropertyNode("nickname").addConstraintViolation();
            valid = false;
        }

        // 커리큘럼 검증
        if (dto.getCurriculum() == null || !dto.getCurriculum().matches("^[가-힣]{3,4}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("과정명은 한글 3~4자로 입력해주세요.")
                    .addPropertyNode("curriculum").addConstraintViolation();
            valid = false;
        }

        return valid;

    }
}
