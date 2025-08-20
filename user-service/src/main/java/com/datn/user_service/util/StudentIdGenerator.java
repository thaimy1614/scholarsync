package com.datn.user_service.util;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StudentIdGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long getNextUserId(Integer enrollmentYear, String roleName) {
        String sequenceName = roleName.toLowerCase() + "_" + enrollmentYear + "_seq";
        return ((Number) entityManager
                .createNativeQuery("SELECT nextval('" + sequenceName + "')")
                .getSingleResult())
                .longValue();
    }
}
