package io.reflectoring.buckpal.account.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Activity {

  private ActivityId id;

  @NonNull private final Account.AccountId ownerAccountId;

  @NonNull private final Account.AccountId sourceAccountId;

  @NonNull private final Account.AccountId targetAccountId;

  @NonNull private final LocalDateTime timestamp;

  @NonNull private final Money money;

  @Value
  public static class ActivityId {
    Long value;
  }
}
