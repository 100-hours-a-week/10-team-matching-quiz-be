package com.easyterview.wingterview.interview.repository;

import com.easyterview.wingterview.interview.entity.MainQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MainQuestionRepository extends JpaRepository<MainQuestionEntity, UUID> {

    @Query(value = """
    SELECT DISTINCT mq.*
    FROM main_question mq
    JOIN main_question_tech_stack mts ON mq.id = mts.main_question_id
    WHERE mq.job_interest IN :jobInterests
    OR mts.tech_stack IN :techStacks
    ORDER BY RAND()
    LIMIT 4
    """, nativeQuery = true)
    List<MainQuestionEntity> findRandomMatchingQuestions(
            @Param("jobInterests") List<String> jobInterests,
            @Param("techStacks") List<String> techStacks
    );
}
