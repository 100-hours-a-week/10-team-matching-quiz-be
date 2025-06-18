package com.easyterview.wingterview.user.service;

import com.easyterview.wingterview.common.enums.Seats;
import com.easyterview.wingterview.common.util.S3Util;
import com.easyterview.wingterview.common.util.SeatPositionUtil;
import com.easyterview.wingterview.common.util.UUIDUtil;
import com.easyterview.wingterview.global.exception.*;
import com.easyterview.wingterview.interview.entity.InterviewFeedbackEntity;
import com.easyterview.wingterview.interview.entity.InterviewHistoryEntity;
import com.easyterview.wingterview.interview.repository.InterviewHistoryRepository;
import com.easyterview.wingterview.interview.repository.InterviewHistoryRepositoryCustom;
import com.easyterview.wingterview.matching.entity.MatchingParticipantEntity;
import com.easyterview.wingterview.matching.repository.MatchingParticipantRepository;
import com.easyterview.wingterview.user.dto.request.SeatPosition;
import com.easyterview.wingterview.user.dto.request.UserBasicInfoDto;
import com.easyterview.wingterview.user.dto.response.*;
import com.easyterview.wingterview.user.entity.*;
import com.easyterview.wingterview.user.enums.JobInterest;
import com.easyterview.wingterview.user.enums.TechStack;
import com.easyterview.wingterview.user.repository.InterviewStatRepository;
import com.easyterview.wingterview.user.repository.RecordRepository;
import com.easyterview.wingterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InterviewStatRepository interviewStatRepository;
    private final MatchingParticipantRepository matchingParticipantRepository;
    private final InterviewHistoryRepositoryCustom interviewHistoryRepositoryCustom;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final RecordRepository recordRepository;
    private final S3Util s3Util;

    @Transactional
    @Override
    public void saveUserInfo(UserBasicInfoDto userBasicInfo) {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        user.setName(userBasicInfo.getName());
        user.setNickname(userBasicInfo.getNickname());
        user.setProfileImageUrl(userBasicInfo.getProfileImageName() != null ? s3Util.getUrl(userBasicInfo.getProfileImageName()) : null);
        user.setIsKTB(userBasicInfo.getIsKTB());

        // KTB 회원인 경우
        if(userBasicInfo.getIsKTB()) {
            user.setCurriculum(userBasicInfo.getCurriculum());

            SeatPosition seatPosition = userBasicInfo.getSeatPosition();
            int seatInt = SeatPositionUtil.seatPosToInt(seatPosition);
            Optional<UserEntity> seatUserOpt = userRepository.findBySeat(seatInt);
            // 자리에 누가 있고 그게 내가 아니라면 -> 중복
            if (seatUserOpt.isPresent() && !seatUserOpt.get().getId().equals(user.getId()))
                throw new AlreadyBlockedSeatException();
            user.setSeat(seatInt);
        }

        List<UserTechStackEntity> techStacks = userBasicInfo.getTechStack().stream()
                .map(TechStack::from) // 문자열 → enum
                .map(stack -> UserTechStackEntity.builder()
                        .user(user)
                        .techStack(stack)
                        .build())
                .toList();

        List<UserJobInterestEntity> jobInterests = userBasicInfo.getJobInterest().stream()
                .map(JobInterest::from)
                .map(interest -> UserJobInterestEntity.builder()
                        .user(user)
                        .jobInterest(interest)
                        .build())
                .toList();

        InterviewStatEntity interviewStat = InterviewStatEntity.builder()
                .user(user)
                .build();

        // 기존 것들 clear하고 새로 설정
        user.getUserJobInterest().clear();
        user.getUserJobInterest().addAll(jobInterests);

        user.getUserTechStack().clear();
        user.getUserTechStack().addAll(techStacks);
        interviewStat.setUser(user);

        interviewStatRepository.save(interviewStat);

        user.setInterviewStat(interviewStat);

        userRepository.save(user);
    }

    @Override
    public SeatPositionDto getBlockedSeats() {
        // DTO 구조 변경(A,B,C 분단)
        boolean[][] A = new boolean[Seats.ROW_LENGTH.getLength()][Seats.COL_LENGTH.getLength()/3];
        boolean[][] B = new boolean[Seats.ROW_LENGTH.getLength()][Seats.COL_LENGTH.getLength()/3];
        boolean[][] C = new boolean[Seats.ROW_LENGTH.getLength()][Seats.COL_LENGTH.getLength()/3];
        List<Integer> seats = userRepository.findAllSeatInfo();

        // 자리들에 대해 boolean[][] 배열에 넣어주기
        seats.forEach((index) -> {
            int seatX = index / Seats.COL_LENGTH.getLength();
            int seatY = index % Seats.COL_LENGTH.getLength();
            if(seatY / 3 == 0){
                A[seatX][seatY%3] = true;
            }
            else if(seatY / 3 == 1){
                B[seatX][seatY%3] = true;
            }
            else{
                C[seatX][seatY%3] = true;
            }
        });
        BlockedSeats blockedSeats = BlockedSeats.builder()
                .A(A)
                .B(B)
                .C(C)
                .build();

        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);
        Integer mySeatIdx = user.getSeat();
        int[] mySeatPosition = user.getSeat() == null ? null : new int[] {mySeatIdx / Seats.COL_LENGTH.getLength() + 1 , mySeatIdx % Seats.COL_LENGTH.getLength() + 1 };

        return SeatPositionDto.builder()
                .seats(blockedSeats)
                .mySeatPosition(mySeatPosition)
                .build();
    }

    @Override
    public CheckSeatDto checkSeatBlocked(String seatPositionId) {
        Optional<UserEntity> seatUser = userRepository.findBySeat(SeatPositionUtil.seatPosIdToInt(seatPositionId));
        boolean isSelected = seatUser.isPresent();

        return CheckSeatDto.builder()
                .isSelected(isSelected)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public UserInfoDto getMyInfo() {
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        Optional<MatchingParticipantEntity> matchingParticipantEntity = matchingParticipantRepository.findByUserId(user.getId());

        return UserInfoDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .name(user.getName())
                .curriculum(user.getIsKTB() ? user.getCurriculum() : "Temp")
                .seatCode(user.getIsKTB() ? SeatPositionUtil.seatIdxToSeatCode(user.getSeat()) : "Temp-Temp")
                .jobInterest(user.getUserJobInterest().stream()
                        .map(interestEntity -> interestEntity.getJobInterest().getLabel())
                        .collect(Collectors.toList()))
                .techStack(user.getUserTechStack().stream()
                        .map(techStackEntity -> techStackEntity.getTechStack().getLabel())
                        .toList())
                .interviewCnt(user.getInterviewStat().getInterviewCnt())
                .profileImageUrl(user.getProfileImageUrl())     
                .isInQueue(matchingParticipantEntity.isPresent())
                .myId(user.getId().toString())
                .isKTB(user.getIsKTB())
                .build();
    }

    @Override
    @Transactional
    public void blockSeatPosition(String seatPositionId) {
        int seatInt = SeatPositionUtil.seatPosIdToInt(seatPositionId);

        // 1. 이미 점유된 좌석인지 확인
        Optional<UserEntity> seatUser = userRepository.findBySeat(seatInt);
        if (seatUser.isPresent()) {
            throw new AlreadyBlockedSeatException();
        }

        // 2. 현재 사용자 정보 가져오기
        UserEntity user = userRepository.findById(UUIDUtil.getUserIdFromToken())
                .orElseThrow(InvalidTokenException::new);

        // 3. 사용자가 기존 좌석을 점유하고 있다면 해제
        if (user.getSeat() != null) {
            user.setSeat(null);
        }

        // 4. 새 좌석으로 설정
        user.setSeat(seatInt);
    }

    @Override
    public InterviewHistoryDto getInterviewList(String userId, String cursor, Integer limit) {
        return interviewHistoryRepositoryCustom.findByCursorWithLimit(userId,cursor == null ? null : UUID.fromString(cursor),limit);
    }

    @Override
    public InterviewDetailDto getInterviewDetail(String userId, String interviewHistoryId) {
        InterviewHistoryEntity interviewHistory = interviewHistoryRepository.findById(UUID.fromString(interviewHistoryId)).orElseThrow(InterviewNotFoundException::new);

        if(interviewHistory.getIsFeedbackRequested()) {
            RecordingEntity recordingEntity = recordRepository.findByInterviewHistoryId(UUID.fromString(interviewHistoryId)).orElseThrow(RecordNotFoundException::new);
            List<FeedbackItem> feedbackItemList = interviewHistory.getSegments().stream().map(s -> {
                        if (s.getFeedback() == null) {
                            return FeedbackItem.builder()
                                    .segmentId(s.getId().toString())
                                    .startAt(s.getFromTime())
                                    .endAt(s.getToTime())
                                    .question(s.getSelectedQuestion())
                                    .order(s.getSegmentOrder())
                                    .build();
                        }
                        InterviewFeedbackEntity interviewFeedback = s.getFeedback();
                        return
                                FeedbackItem.builder()
                                        .segmentId(s.getId().toString())
                                        .commentary(interviewFeedback.getCommentary())
                                        .endAt(s.getToTime())
                                        .modelAnswer(interviewFeedback.getCorrectAnswer())
                                        .startAt(s.getFromTime())
                                        .question(s.getSelectedQuestion())
                                        .order(s.getSegmentOrder())
                                        .build();
                    }).sorted(Comparator.comparingInt(FeedbackItem::getOrder))
                    .toList();


            return InterviewDetailDto.builder()
                    .feedback(feedbackItemList)
                    .recordingUrl(recordingEntity.getUrl())
                    .createdAt(interviewHistory.getCreatedAt())
                    .duration((interviewHistory.getEndAt().getTime() - interviewHistory.getCreatedAt().getTime()) / 1000)
                    .build();
        }
        else{
            List<FeedbackItem> feedbackItemList = interviewHistory.getSegments().stream().map(s -> {
                return FeedbackItem.builder()
                        .segmentId(s.getId().toString())
                        .question(s.getSelectedQuestion())
                        .order(s.getSegmentOrder())
                        .build();
            }).sorted(Comparator.comparingInt(FeedbackItem::getOrder))
                    .toList();

            return InterviewDetailDto.builder()
                    .createdAt(interviewHistory.getCreatedAt())
                    .duration((interviewHistory.getEndAt().getTime() - interviewHistory.getCreatedAt().getTime()) / 1000)
                    .feedback(feedbackItemList)
                    .build();
        }
    }


}
