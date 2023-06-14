package io.reflectoring.buckpal.account.domain;

import io.reflectoring.buckpal.account.domain.Account.AccountId;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.NonNull;

public class ActivityWindow {

  private List<Activity> activities;

  public LocalDateTime getStartTimestamp() {
    return activities.stream()
        .min(Comparator.comparing(Activity::getTimestamp))
        .orElseThrow(IllegalStateException::new)
        .getTimestamp();
  }

  public LocalDateTime getEndTimestamp() {
    return activities.stream()
        .max(Comparator.comparing(Activity::getTimestamp))
        .orElseThrow(IllegalStateException::new)
        .getTimestamp();
  }

  public Money calculateBalance(AccountId accountId) {
    Money depositBalance = activities.stream()
        .filter(a -> a.getTargetAccountId().equals(accountId))
        .map(Activity::getMoney)
        .reduce(Money.ZERO, Money::add);

    Money withdrawalBalance = activities.stream()
        .filter(a -> a.getSourceAccountId().equals(accountId))
        .map(Activity::getMoney)
        .reduce(Money.ZERO, Money::add);

    return Money.add(depositBalance, withdrawalBalance.negate());
  }

  public ActivityWindow(@NonNull List<Activity> activities) {
    this.activities = activities;
  }

  public List<Activity> getActivities() {
    return Collections.unmodifiableList(this.activities);
  }

  public void addActivity(Activity activity) {
    this.activities.add(activity);
  }
}
