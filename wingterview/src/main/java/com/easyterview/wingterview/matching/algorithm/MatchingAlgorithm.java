package com.easyterview.wingterview.matching.algorithm;

import com.easyterview.wingterview.user.entity.UserEntity;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Setter
@Component
public class MatchingAlgorithm {

    private static final int JOB_WEIGHT = 20;
    private static final int TECH_WEIGHT = 15;
    private static final int CURRICULUM_WEIGHT = 10;

    private Map<String, MatchingUser> participantMap;
    private List<MatchingUser> participants;

//    public static void main(String[] args) {
//        int participantCount = 151;
//        List<MatchingUser> participants = generateDummyParticipants(participantCount);
//
//        participantMap = participants.stream()
//                .collect(Collectors.toMap(MatchingUser::getId, user -> user));
//
//        List<Pair<String, String>> greedyMatches = performGreedyMatching(new ArrayList<>(participants));
//
//        System.out.println("===== Greedy Matching 결과 =====");
//        printMatchingResults(greedyMatches);
//
//        printRemainingUsers(participants, greedyMatches);
//    }

    public void printMatchingResults(List<Pair<String, String>> matches) {
        for (Pair<String, String> pair : matches) {
            MatchingUser userA = participantMap.get(pair.getLeft());
            MatchingUser userB = participantMap.get(pair.getRight());
            int matchScore = calculateMatchScore(userA, userB);

            System.out.println("-------------------------------");
            System.out.printf("🔗 Pair: %s ↔ %s (점수: %d)%n", pair.getLeft(), pair.getRight(), matchScore);
            System.out.println("User A:");
            printUserInfo(userA);
            System.out.println("User B:");
            printUserInfo(userB);
            System.out.println("-------------------------------");
        }
    }

    public void printRemainingUsers(List<MatchingUser> participants, List<Pair<String, String>> matches) {
        Set<String> matchedUsers = matches.stream()
                .flatMap(pair -> Arrays.stream(new String[]{pair.getLeft(), pair.getRight()}))
                .collect(Collectors.toSet());

        System.out.println("\n===== 남은 인원 =====");
        participants.stream()
                .filter(user -> !matchedUsers.contains(user.getUserId()))
                .forEach(user -> {
                    System.out.println("-------------------------------");
                    System.out.printf("🧍‍♂️ 남은 사용자: %s%n", user.getUserId());
                    printUserInfo(user);
                    System.out.println("-------------------------------");
                });
    }

    public void printUserInfo(MatchingUser user) {
        if (user != null) {
            System.out.printf("ID: %s%n", user.getUserId());
            System.out.printf("Job Interests: %s%n", String.join(", ", user.getJobInterests()));
            System.out.printf("Tech Stacks: %s%n", String.join(", ", user.getTechStacks()));
            System.out.printf("Curriculum: %s%n", user.getCurriculum());
        } else {
            System.out.println("정보 없음");
        }
    }

//    private static List<MatchingUser> generateDummyParticipants(int n) {
//        List<String> jobs = Arrays.asList("백엔드 개발자", "프론트엔드 개발자", "풀스택 개발자",
//                "클라우드 엔지니어", "솔루션 아키텍트", "DevOps 엔지니어",
//                "ML 엔지니어", "AI 백엔드 개발자", "데이터 사이언티스트");
//
//        List<String> stacks = Arrays.asList("Java", "Spring", "Typescript", "JavaScript",
//                "Python", "Kubernetes", "AWS", "Pytorch", "FastAPI", "LangChain");
//        List<String> curriculums = Arrays.asList("생성형 AI", "풀스택", "클라우드");
//
//        List<MatchingUser> users = new ArrayList<>();
//        Random rand = new Random();
//
//        for (int i = 0; i < n; i++) {
//            int jobCount = rand.nextInt(3) + 1;
//            int stackCount = rand.nextInt(3) + 1;
//
//            Collections.shuffle(jobs);
//            List<String> jobInterests = new ArrayList<>(jobs.subList(0, jobCount));
//
//            Collections.shuffle(stacks);
//            List<String> techStacks = new ArrayList<>(stacks.subList(0, stackCount));
//
//            String curriculum = curriculums.get(rand.nextInt(curriculums.size()));
//            users.add(new MatchingUser("user" + i, jobInterests, techStacks, curriculum));
//        }
//
//        return users;
//    }

    public int calculateMatchScore(MatchingUser a, MatchingUser b) {
        long jobMatches = a.getJobInterests().stream().filter(b.getJobInterests()::contains).count();
        long techMatches = a.getTechStacks().stream().filter(b.getTechStacks()::contains).count();
        int curriculumMatch = a.getCurriculum().equals(b.getCurriculum()) ? 1 : 0;

        return (int) (jobMatches * JOB_WEIGHT + techMatches * TECH_WEIGHT + curriculumMatch * CURRICULUM_WEIGHT);
    }

    public List<Pair<String, String>> performGreedyMatching() {
        List<Pair<String, String>> pairs = new ArrayList<>();
        Set<String> matched = new HashSet<>();

        PriorityQueue<Pair<String, String>> pq = new PriorityQueue<>(
                (p1, p2) -> Integer.compare(
                        calculateMatchScore(participantMap.get(p2.getLeft()), participantMap.get(p2.getRight())),
                        calculateMatchScore(participantMap.get(p1.getLeft()), participantMap.get(p1.getRight()))
                ));

        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                pq.add(Pair.of(participants.get(i).getUserId(), participants.get(j).getUserId()));
            }
        }

        while (!pq.isEmpty() && matched.size() < participants.size()) {
            Pair<String, String> bestPair = pq.poll();
            if (!matched.contains(bestPair.getLeft()) && !matched.contains(bestPair.getRight())) {
                pairs.add(bestPair);
                matched.add(bestPair.getLeft());
                matched.add(bestPair.getRight());
            }
        }

        return pairs;
    }
}
