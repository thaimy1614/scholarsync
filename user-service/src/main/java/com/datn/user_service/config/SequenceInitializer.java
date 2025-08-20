package com.datn.user_service.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class SequenceInitializer {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void createSequences() {
        entityManager.createNativeQuery(
                "CREATE SEQUENCE IF NOT EXISTS student_2025_seq START WITH 250000 INCREMENT BY 1;" +
                        "CREATE SEQUENCE IF NOT EXISTS student_2026_seq START WITH 260000 INCREMENT BY 1;" +
                        "CREATE SEQUENCE IF NOT EXISTS teacher_2025_seq START WITH 250000 INCREMENT BY 1;" +
                        "CREATE SEQUENCE IF NOT EXISTS teacher_2026_seq START WITH 260000 INCREMENT BY 1;" +
                        "CREATE SEQUENCE IF NOT EXISTS parent_2025_seq START WITH 250000 INCREMENT BY 1;" +
                        "CREATE SEQUENCE IF NOT EXISTS parent_2026_seq START WITH 260000 INCREMENT BY 1;"
        ).executeUpdate();
    }

}
