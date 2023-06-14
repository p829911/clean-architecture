package io.reflectoring.buckpal.account.application.port.out;

import io.reflectoring.buckpal.account.domain.Account;
import io.reflectoring.buckpal.account.domain.Account.AccountId;
import java.time.LocalDateTime;

public interface LoadAccountPort {

  Account loadAccount(AccountId accountId, LocalDateTime baselineDate);
}
