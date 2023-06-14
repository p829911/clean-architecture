package io.reflectoring.buckpal.account.application.port.in;


import io.reflectoring.buckpal.account.domain.Account.AccountId;
import io.reflectoring.buckpal.account.domain.Money;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SendMoneyCommand {
  
  @NotNull
  private final AccountId sourceAccountId;
  @NotNull
  private final AccountId targetAccountId;
  @NotNull
  private final Money money;

  public SendMoneyCommand(AccountId sourceAccountId, AccountId targetAccountId, Money money) {
    this.sourceAccountId = sourceAccountId;
    this.targetAccountId = targetAccountId;
    this.money = money;
  }
}
