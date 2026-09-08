package com.bantads.repository.read;

import com.bantads.entity.read.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {
    List<AccountHistory> findAllByAccountNumberAndCreatedAtLessThan(String number, LocalDateTime start);
    List<AccountHistory> findAllByAccountNumberAndCreatedAtBetween(String number, LocalDateTime start, LocalDateTime end);
}
