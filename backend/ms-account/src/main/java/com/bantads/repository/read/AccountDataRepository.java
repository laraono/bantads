package com.bantads.repository.read;

import com.bantads.entity.read.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDataRepository extends JpaRepository<AccountData, String> {
}
