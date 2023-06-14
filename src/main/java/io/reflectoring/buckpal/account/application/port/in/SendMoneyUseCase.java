package io.reflectoring.buckpal.account.application.port.in;

import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public interface SendMoneyUseCase {
  boolean sendMoney(@Valid SendMoneyCommand command);
}
