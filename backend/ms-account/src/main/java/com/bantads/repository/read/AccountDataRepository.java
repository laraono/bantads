package com.bantads.repository.read;

import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountDataRepository extends JpaRepository<AccountData, String> {

    List<AccountData> findAllByManagerCPF(String managerCPF);

    @Query("SELECT manager_cpf as managerCPF, COUNT(account_number) as totalAccounts, SUM(balance) as totalBalance FROM account_data GROUP BY managerCPF")
    List<AccountsByManagerDTO> getAccountCountGroupByManager();

    @Query("SELECT manager_cpf as managerCPF, COUNT(account_number) as totalAccounts, SUM(balance) as totalBalance FROM account_data WHERE manager_cpf = :cpf GROUP BY manager_cpf")
    AccountsByManagerDTO getAccountCountByManager(String cpf);

    boolean existsAccountDataByAccountNumber(String accountNumber);

    AccountData findByAccountNumber(String accountNumber);
}
