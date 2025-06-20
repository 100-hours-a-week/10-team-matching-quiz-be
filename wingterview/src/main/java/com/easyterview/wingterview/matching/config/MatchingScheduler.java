package com.easyterview.wingterview.matching.config;

import com.easyterview.wingterview.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingScheduler {

    private final MatchingStatusManager matchingStatusManager;
    private final MatchingService matchingService;

    /**
    * 40분에 매칭 큐 open
    * */
//    @Scheduled(cron = "0 40 * * * *") // 초 분 시 일 월 요일
    public void openMatchingEveryHour() {
        log.info("🟢 매칭 OPEN 스케쥴러 실행됨");
        matchingStatusManager.openMatching();
    }

    /**
     * 매 시 정각에 매칭 시작
     */
//    @Scheduled(cron = "0 0 * * * *")
    public void doMatchingAlgorithm(){
        matchingService.doMatchingAlgorithm();
    }
}
