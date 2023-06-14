package io.reflectoring.buckpal.account.domain;

import io.reflectoring.buckpal.account.domain.Activity.ActivityId;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  private final AccountId id;

  private final Money baselineBalance;

  private final ActivityWindow activityWindow;

  public static Account withoutId(Money baselineBalance, ActivityWindow activityWindow) {
    return new Account(null, baselineBalance, activityWindow);
  }

  public static Account withId(
      AccountId accountId, Money baselineBalance, ActivityWindow activityWindow) {
    return new Account(accountId, baselineBalance, activityWindow);
  }

  public Optional<AccountId> getId() {
    return Optional.ofNullable(this.id);
  }

  public Money calculateBalance() {
    return Money.add(this.baselineBalance, this.activityWindow.calculateBalance(this.id));
  }

  public boolean withdraw(Money money, AccountId targetAccountId) {

    if (!mayWithdraw(money)) {
      return false;
    }

    Activity withdrawal =
        new Activity(new ActivityId(activity.getId()), this.id, this.id, targetAccountId, LocalDateTime.now(), money);
    this.activityWindow.addActivity(withdrawal);
    return true;
  }

  private boolean mayWithdraw(Money money) {
    return Money.add(this.calculateBalance(), money.negate()).isPositiveOrZero();
  }

  public boolean deposit(Money money, AccountId sourceAccountId) {
    Activity deposit = new Activity(new ActivityId(activity.getId()), this.id, sourceAccountId, this.id, LocalDateTime.now(), money);
    this.activityWindow.addActivity(deposit);
    return true;
  }

  @Value
  public static class AccountId {
    private Long value;
  }
}
