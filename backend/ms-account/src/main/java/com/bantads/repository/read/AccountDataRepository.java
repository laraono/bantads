package com.bantads.repository.read;

import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountDataRepository extends JpaRepository<AccountData, String> {

    List<AccountData> findAllByManagerCPF(String managerCPF);

    @Query("SELECT managerCPF, COUNT(accountNumber) FROM AccountData GROUP BY managerCPF")
    List<AccountsByManagerDTO> getAccountCountByManager();

    boolean existsAccountDataByAccountNumber(String accountNumber);

    AccountData findByAccountNumber(String accountNumber);
}
