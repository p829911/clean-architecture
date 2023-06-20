package io.reflectoring.buckpal.account.domain;

import static io.reflectoring.buckpal.common.AccountTestData.defaultAccount;
import static io.reflectoring.buckpal.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.reflectoring.buckpal.account.domain.Account.AccountId;
import org.junit.jupiter.api.Test;

class AccountTest {

  @Test
  void withdrawalSucceeds() {
    AccountId accountId = new AccountId(1L);
    Account account =
        defaultAccount()
            .withAccountId(accountId)
            .withBaselineBalance(Money.of(555L))
            .withActivityWindow(new ActivityWindow(
                defaultActivity()
                    .withTargetAccount(accountId)
                    .withMoney(Money.of(999L)).build(),
                defaultActivity()
                    .withTargetAccount(accountId)
                    .withMoney(Money.of(1L)).build()))
            .build();

    boolean success = account.withdraw(Money.of(555L), new AccountId(99L));

    assertThat(success).isTrue();
    assertThat(account.getActivityWindow().getActivities()).hasSize(3);
    assertThat(account.calculateBalance()).isEqualTo(Money.of(1000L));
  }
}
