# 04. 유스케이스 구현하기

## 도메인 소개

육각형 아키텍처는 도메인 중심의 아키텍처에 적합하기 때문에 도메인 엔티티를 만드는 것을 시작한 후 해당 도메인 엔티티를 중심으로 유스케이스를 구현한다.

### Account Entity

- 실제 계좌의 현재 스냅숏을 제공한다.
- 계좌에 대한 모든 입금과 출금은 `Activity` 엔티티에 포착된다.
- 한 계좌에 대한 모든 활동들을 항상 메모리에 한꺼번에 올리는 것은 현명한 방법이 아니기 떄문에
  `ActivityWindow` 값 객체를 이용해서 지난 며칠 혹은 몇 주간의 범위에 해당하는 `Activity`만 보유한다.
- `baselineBalance`: `activityWindow`의 첫번째 활동 바로 전의 잔고를 표현
- 입금과 출금은 새로운 활동을 활동창에 추가하는 것에 불과
- 출금하기 전에는 잔고를 초과하는 금액을 출금할 수 없도록 하는 비즈니스 규칙 검사

<img width="400" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/4d1c2b98-c315-4db5-99cc-8e16c1d872e0">

### Activity Entity

- 계좌에 대한 모든 입금과 출금을 기록한다.

<img width="587" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/9c03aec3-504e-4eda-a3fc-bac980c62bef">

### ActivityWindow Value

<img width="365" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/fe07b3dd-e785-4435-8973-7eedee9426bf">

## 유스케이스 둘러보기

일반적으로 유스케이스는 다음과 같은 단계를 따른다.

1. **입력을 받는다**
   - 유스케이스는 인커밍 어댑터로부터 입력을 받는다
   - 유스케이스는 도메인 로직에만 신경써야 하고 입력 유효성 검증으로 오염되면 안된다
2. **비즈니스 규칙을 검증한다**
   - 유스케이스는 비즈니스 규칙을 검증할 책임이 있다
   - 도메인 엔티티와 이 책임을 공유한다
3. **모델 상태를 조작한다**
   - 입력을 기반으로 어떤 방법으로든 모델의 상태를 변경한다
   - 도메인 객체의 상태를 바꾸고 영속성 어댑터를 통해 구현된 포트로 이 상태를 전달해서 저장될 수 있게 한다
   - 또 다른 아웃고잉 어댑터를 호출할 수도 있다
4. **출력을 반환한다**
   - 아웃고잉 어댑터에서 온 출력값을, 유스케이스를 호출한 어댑터로 반환할 출력 객체로 변환한다

### SendMoneyService

- 인커밍 포트 인터페이스 `SendMoneyUseCase` 구현
- 계좌를 불러오기 위해 아웃고잉 포트 인터페이스 `LoadAccountPort` 호출
- 데이터베이스의 계좌 상태를 업데이트 하기 위해 `UpdateAccountStatePort` 호출

<img width="893" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/ea44f755-2b5e-4978-8382-cea5bea6d73b">

<img width="955" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/c8093c44-9a55-4707-8010-d53e3d628f4d">

## 입력 유효성 검증

입력 유효성 검증은 유스케이스 클래스의 책임이 아니긴 하지만 애플리케이션 계층의 책임에 해당한다
애플리케이션 계층에서 입력 유효성을 검증해야 하는 이유는, 그렇게 하지 않을 경우 애플리케이션 코어의 바깥쪽으로부터 유효하지 않은 입력값을 받게 되고, 모델의 상태를 해칠 수 있기 때문이다
입력 모델(input model)의 생성자에서 입력 유효성을 검증하자 (`SendMoneyCommand`)

```java
@Getter
public class SendMoneyCommand {
  
  private final AccountId sourceAccountId;
  private final AccountId targetAccountId;
  private final Money money;

  public SendMoneyCommand(AccountId sourceAccountId, AccountId targetAccountId, Money money) {
    this.sourceAccountId = sourceAccountId;
    this.targetAccountId = targetAccountId;
    this.money = money;
    requireNonNull(sourceAccountId);
    requireNonNull(targetAccountId);
    requireNonNull(money);
    requireGreaterThan(money, 0);
  }
}
```

- 조건 중 하나라도 위배되면 객체를 생성할 때 예외를 던져서 객체 생성을 막으면 된다
- 필드들은 모두 `final` 이다. 
- 일단 생성에 성공하고 나면 상태는 유효하고 이후에 잘못된 상태로 변경할 수 없다는 사실을 보장할 수 있다
  `SendMoneyCommand` 는 유스케이스 API의 일부이기 때문에 인커밍 포트 패키지에 위치한다. 
- 그러므로 유효성 검증이 애플리케이션의 코어에 남아있지만 신성한 유스케이스 코드를 오염시키지는 않는다

### Bean Validation API

공식 홈페이지: https://beanvalidation.org/

참고: https://meetup.nhncloud.com/posts/223

이 API를 이용하면 필요한 유효성 규칙들을 필드의 애너테이션으로 표현할 수 있다

```java
@Value
@EqualsAndHashCode(callSuper = false)
public
class SendMoneyCommand extends SelfValidating<SendMoneyCommand> {

    @NotNull
    private final AccountId sourceAccountId;

    @NotNull
    private final AccountId targetAccountId;

    @NotNull
    private final Money money;

    public SendMoneyCommand(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money money) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.money = money;
        this.validateSelf();
    }
}
```

`SelfValidating`

```java
public abstract class SelfValidating<T> {

  private Validator validator;

  public SelfValidating() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  /**
   * Evaluates all Bean Validations on the attributes of this
   * instance.
   */
  protected void validateSelf() {
    Set<ConstraintViolation<T>> violations = validator.validate((T) this);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
```

- 라스트마일에서 `Controller`에도 request parameter 앞에 `@Valid` 애너테이션을 붙여 사용하고 있다
- `SelfValidating` 의 기능을 애너테이션으로 대체할 수 있다
- `Service` 에서 사용하려면 클래스 위에 `@Validated` 를 붙이고 검증할 파라미터 앞에 `@Valid` 를 붙이면 사용할 수 있다

```java
@Validated
public interface SendMoneyUseCase {
  boolean sendMoney(@Valid SendMoneyCommand command);
}
```

- 입력 모델에 있는 유효성 검증 코드를 통해 유스케이스 구현체 주위에 사실상 오류 방지 계층(anti corruption layer)를 만들었다
- 여기서 말하는 계층은 하위 계층을 호출하는 계층형 아키텍처에서의 계층이 아니라 잘못된 입력을 호출자에게 돌려주는 유스케이스 보호막을 의미한다

## 생성자의 힘

~~파라미터가 더 많아진다면 빌더 패턴을 쓸 수도 있다~~

- ~~생성자를 private으로 만들고 빌더의 build() 메서드 내부에 생성자 호출을 숨길 수 있다~~
- ~~그러나 파라미터가 또 추가된다면 생성자와 빌더에 새로운 필드를 추가해야한다~~
- ~~빌더를 호출하는 코드에 새로운 필드를 추가하는 것을 잊고 만다~~
- ~~컴파일러는 이처럼 유효하지 않은 상태의 불변 객체를 만들려는 시도에 대해서는 경고해주지 못한다.~~
- ~~하지만 빌더 뒤에 숨기는 대신 생성자를 직접 사용했다면 새로운 필드를 추가하거나 필드를 삭제할 때마다 컴파일 에러를 따라 나머지 코드에 변경사항을 반영할 수 있었을 것이다~~

~~결론: IDE의 힘을 믿고 그냥 생성자 쓰자~~

## 유스케이스마다 다른 입력 모델

> 각 유스케이스 전용 입력 모델은 유스케이스를 훨씬 명확하게 만들고 다른 유스케이스와의 결합도 제거해서 불필요한 부수효과가 발생하지 않게 한다

각기 다른 유스케이스에 동일한 입력 모델을 사용하는건 좋지 않다
유스케이스마다 필요한 필드가 다를 수 있고 그러면 필드에 `null`이 들어갈 때도 있는데
불변의 커맨드 객체의 필드에 대해서 `null`을 유효한 상태로 받아들이는 것은 그 자체로 코드 냄새다
더 문제가 되는 부분은 각기 다른 유스케이스에 서로 다른 유효성 검증 로직이 필요하다

## 비즈니스 규칙 검증하기

| 입력 유효성 검증                                | 비즈니스 규칙 검증                                           |
| ----------------------------------------------- | ------------------------------------------------------------ |
| 값 그 자체로 검증 가능                          | 도메인 모델의 현재 상태에 접근해야 함                        |
| 선언적으로 구현 가능 (`@NotNull`)               | 도메인 지식과 맥락이 더 필요                                 |
| 구문상의 (syntactical) 유효성 검증              | 유스케이스의 맥락 속에서 의미적인(semantical) 유효성 검증    |
| 송금되는 금액은 0보다 커야 한다 (모델에 접근 X) | 출금 계좌는 초과 출금되어서는 안된다 (모델의 현재 상테에 접근) |

구분하는게 나중에 유지보수할 때 그것이 어디에 있는지 찾는데도 도움된다

비즈니스 규칙은 도메인 엔티티 안에서 구현한다 (Account.withdraw)
이렇게 하면 이 규칙을 지켜야 하는 비즈니스 로직 바로 옆에 규칙이 위치하기 때문에 위치를 정하는 것도 쉽고 추론하기도 쉽다

## 풍부한 도메인 모델 vs 빈약한 도메인 모델

> 정답은 없다. 각자의 필요에 맞는 스타일을 자유롭게 택해서 사용하면 된다

### 풍부한 도메인 모델

풍부한 도메인 모델에서는 애플리케이션의 코어에 있는 엔티티에서 가능한 한 많은 도메인 로직이 구현된다
엔티티들은 상태를 변경하는 메서드를 제공하고, 비즈니스 규칙에 맞는 유효한 변경만을 허용한다 (Account 엔티티 구현 방식)
많은 비즈니스 규칙이 유스케이스 구현체 대신 엔티티에 위치하게 된다

### 빈약한 도메인 모델

엔티티 자체가 굉장히 얇다
일반적으로 엔티티는 상태를 표현하는 필드와 이 값을 읽고 바꾸기 위한 getter, setter 메서드만 포함하고 어떤 도메인 로직도 가지고 있지 않다
도메인 로직이 유스케이스 클래스에 구현돼 있다는 것이다

비즈니스 규칙을 검증하고, 엔티티의 상태를 바꾸고, 데이터베이스 저장을 담당하는 아웃고잉 포트에 엔티티를 전달할 책임 역시 유스케이스 클래스에 있다

풍부함이 엔티티 대신 유스케이스에 존재하는 것이다

## 유스케이스마다 다른 출력 모델

> 유스케이스들 간에 같은 출력 모델을 공유하게 되면 유스케이스들도 강하게 결합된다.
> 한 유스케이스에서 출력 모델에 새로운 필드가 필요해지면 이 값과 관련이 없는 다른 유스케이스에서도 이 필드를 처리해야 한다.
> 공유 모델은 장기적으로 봤을 때 갖가지 이유로 점점 커지게 돼 있다.
> 단일 책임 원칙을 적용하고 모델을 분리해서 유지하는 것은 유스케이스의 결합을 제거하는데 도움이 된다.

입력과 비슷하게 출력도 가능하면 각 유스케이스에 맞게 구체적일수록 좋다
호출은 호출자에게 꼭 필요한 데이터만 들고 있어야 한다

송금하기의 반환 값으로 `boolean` 값을 반환 할지 업데이트된 `Account` 를 통째로 반환할지에 대한 정답은 없다
그러나 유스케이스를 가능한 한 구체적으로 유지하기 위해서는 계속 질문해야 한다

만약 의심스럽다면 가능한 한 적게 반환하자

## 읽기 전용 유스케이스는 어떨까?

> 쿼리를 위한 인커밍 전용 포트를 만들고 이를 쿼리 서비스에 구현한다

간단히 계좌의 잔액을 가져오고자 하는 읽기 전용 작업을 유스케이스로 만들어야 할까?
애플리케이션 코어의 관점에서 간단한 데이터 쿼리다
그렇기 때문에 프로젝트 맥락에서 유스케이스로 간주되지 않는다면 실제 유스케이스와 구분하기 위해 쿼리로 구현할 수 있다

### CQRS

https://learn.microsoft.com/ko-kr/azure/architecture/patterns/cqrs

> CQRS는 데이터 저장소에 대한 읽기 및 업데이트 작업을 구분하는 패턴인 명령과 쿼리의 역할 분리를 의미합니다. 애플리케이션에서 CQRS를 구현하면 성능, 확장성 및 보안을 최대화할 수 있습니다. CQRS로 마이그레이션하면 유연성이 생기므로 시스템이 점점 진화하고 업데이트 명령이 도메인 수준에서 병합 충돌을 일으키지 않도록 할 수 있습니다.

## 결론

입출력 모델을 독립적으로 모델링한다면 원치 않는 부수효과를 피할 수 있다
물론 유스케이스 간에 모델을 공유하는 것보다는 더 많은 작업이 필요하다

그러나 유스케이스 별로 모델을 만들면 유스케이스를 명확하게 이해할 수 있고, 장기적으로 유지보수하기도 더 쉽다.

또한 여러 명의 개발자가 다른 사람이 작업 중인 유스케이스를 건드리지 않은 채로 여러 개의 유스케이스를 동시에 작업할 수 있다