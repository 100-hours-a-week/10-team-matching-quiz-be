package com.easyterview.wingterview.user.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class UserBasicInfoDto {
    private String name;
    private String nickName;
    private String curriculum;
    private List<String> jobInterest;
    private List<String> techStack;
    private String profileImageUrl;
    private List<Integer> seatPosition;
}
