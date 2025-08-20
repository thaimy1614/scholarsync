package com.datn.user_service.repository;

import com.datn.user_service.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Boolean existsByEmail(String email);

    @Query("SELECT a.email FROM Account a WHERE a.email IN :emails")
    List<String> findExistingEmails(@Param("emails") Set<String> emails);

}
