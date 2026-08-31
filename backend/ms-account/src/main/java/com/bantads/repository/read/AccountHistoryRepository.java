package com.bantads.repository.read;

import com.bantads.entity.read.AccountHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountHistoryRepository extends JpaRepository<AccountHistory, Long> {
    List<AccountHistory> findAllByAccountNumber(String number);
}
