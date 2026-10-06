package com.bantads.repository.read;

import com.bantads.dto.event.GetAccountDTO;
import com.bantads.dto.read.AccountsByManagerDTO;
import com.bantads.entity.read.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountDataRepository extends JpaRepository<AccountData, String> {

    List<AccountData> findAllByManagerCPF(String managerCPF);

    @Query("SELECT managerCPF, COUNT(accountNumber) as totalAccounts, SUM(balance) as totalBalance FROM AccountData GROUP BY managerCPF")
    List<AccountsByManagerDTO> getAccountCountGroupByManager();

    @Query("SELECT managerCPF, COUNT(accountNumber) as totalAccounts, SUM(balance) as totalBalance FROM AccountData WHERE managerCPF = :cpf GROUP BY managerCPF")
    AccountsByManagerDTO getAccountCountByManager(String cpf);

    boolean existsAccountDataByAccountNumber(String accountNumber);

    AccountData findByAccountNumber(String accountNumber);

    List<AccountData> findAllByClientCPFIn(List<String> clientCPFs);

    AccountData findByClientCPF(String clientCPF);

    void deleteByClientCPF(String CPF);
}
