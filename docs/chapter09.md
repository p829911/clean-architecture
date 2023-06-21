# 애플리케이션 조립하기

애플리케이션이 시작될 때 클래스를 인스턴스화 하고 묶기 위해서 의존성 주입 매커니즘을 이용한다

평범한 자바로 이를 어떻게 하는지, 스프링, 스프링 부트 프레임워크에서는 이를 각각 어떻게 하는지 살펴본다

## 왜 조립까지 신경 써야 할까?

### 유스케이스와 어댑터를 그냥 필요할 때 인스턴스화하면 안되는 걸까?

**모든 의존성은 안쪽으로, 애플리케이션의 도메인 코드 방향으로 향해야 도메인 코드가 바깥 계층의 변경으로부터 안전하다**

- 유스케이스가 영속성 어댑터를 호출해야 하고 스스로 인스턴스화한다면 코드 의존성이 잘못된 방향으로 만들어진 것이다
- 이것이 바로 아웃고잉 포트 인터페이스를 생성한 이유다
- 유스케이스는 인터페이스만 알아야 하고, 런타임에 이 인터페이스의 구현을 제공받아야 한다

**코드를 더 테스트하기 쉽다**

- 한 클래스가 필요로 하는 모든 객체를 생성자로 전달할 수 있다면 실제 객체 대신 목으로 전달할 수 있고
- 이렇게 되면 격리된 단위 테스트를 생성하기가 쉬워진다

**그럼 의존성 규칙을 어기지 않으면서 객체 인스턴스를 생성할 책임은 누구에게 있을까?**

- 아키텍처에 대해 중립적이고 인스턴스 생성을 위해 모든 클래스에 대한 의존성을 가지는 설정 컴포넌트(configuration component)가 있어야 한다
- 중립적인 설정 컴포넌트는 인스턴스 생성을 위해 모든 클래스에 접근할 수 있다

<img width="1315" alt="image" src="https://github.com/wikibook/clean-architecture/assets/17154958/8e11dd2a-e118-4423-a708-c124a31fb90f">

설정 컴포넌트는 우리가 제공한 조각들로 애플리케이션을 조립하는 것을 책임진다

- 웹 어댑터 인스턴스 생성
- HTTP 요청이 실제로 웹 어댑터로 전달되도록 보장
- 유스케이스 인스턴스 생성
- 웹 어댑터에 유스케이스 인스턴스 제공
- 영속성 어댑터에 인스턴스 생성
- 유스케이스에 영속성 어댑터 인스턴스 제공
- 영속성 어댑터가 실제로 데이터베이스에 접근할 수 있도록 보장

더불어 설정 컴포넌트는 설정 파일이나 커맨드라인 파라미터 등과 같은 설정 파라미터의 소스에도 접근할 수 있어야 한다

## 평범한 코드로 조립하기

main 메서드 안에서 웹 컨트롤러부터 영속성 어댑터까지 필요한 모든 클래스의 인스턴스를 생성한 후 함께 연결한다

```java
package copyeditor.configuration;

class Application {
  public static void main(String[] args) {
    
    AccountRepository accountRepository = new AccountRepository();
    ActivityRepository activityRepository = new ActivityRepository();
    
    AccountPersistenceAdapter accountPersistenceAdapter = 
      new AccountPersistenceAdapter(accountRepository, activityRepository);
    
    SendMoneyUseCase sendMoneyUseCase =
      new SendMoneyService(
      accountPersistenceAdapter, // LoadAccountPort
      accountPersistenceAdapter); // UpdateAccountStatePort
    
    SendMoneyController sendMoneyController =
      new SendMoneyController(sendMoneyUseCase);
    
    // 웹 어댑터를 HTTP로 노출시키는 데 필요한 애플리케이션 부트스트래핑 로직
    startProcessingWebRequests(sendMoneyController);
  }
}
```

애플리케이션 부트스트래핑 from chatgpt

> 애플리케이션 부트스트래핑은 새로운 소프트웨어 애플리케이션을 개발하거나 실행하기 위해 초기 설정과 초기화를 수행하는 과정을 의미합니다. 이 단계는 애플리케이션이 실행되기 전에 필요한 환경을 설정하고 초기 데이터를 로드하는 등 초기화 작업을 수행하는 것을 포함합니다.
>
> 애플리케이션 부트스트래핑의 과정은 일반적으로 다음과 같은 작업을 포함할 수 있습니다:
>
> 1. 환경 설정: 애플리케이션 실행에 필요한 환경 변수, 구성 파일 또는 외부 서비스에 대한 연결을 설정합니다. 이 단계에서는 데이터베이스 연결, 로깅 설정, 보안 설정 등 애플리케이션의 전반적인 구성이 이루어집니다.
> 2. 종속성 해결: 애플리케이션이 필요로 하는 외부 라이브러리, 프레임워크, 패키지 등의 종속성을 해결합니다. 이 단계에서는 종속성을 다운로드하거나 설치하여 애플리케이션에서 사용할 수 있도록 합니다.
> 3. 데이터베이스 초기화: 데이터베이스를 생성하거나 기존 데이터베이스를 초기화합니다. 이 단계에서는 데이터베이스 스키마를 생성하고 초기 데이터를 로드하는 등의 작업을 수행합니다.
> 4. 초기 데이터 로드: 애플리케이션에 필요한 초기 데이터를 로드합니다. 예를 들어, 사용자 정보, 설정 값, 정적 데이터 등을 데이터베이스에 삽입하거나 파일에서 읽어와서 메모리에 적재합니다.
> 5. 애플리케이션 실행: 모든 초기화 작업이 완료되면 애플리케이션을 실행합니다. 이 단계에서는 서버를 시작하거나 사용자 인터페이스를 표시하여 애플리케이션을 사용할 수 있도록 합니다.
>
> 애플리케이션 부트스트래핑은 애플리케이션의 효율적인 시작과 안정적인 동작을 보장하기 위해 중요한 과정입니다. 일반적으로 프레임워크나 개발 도구에서 부트스트래핑을 위한 기능이 제공되며, 개발자는 이러한 기능을 활용하여 애플리케이션의 초기 설정과 초기화를 수행할 수 있습니다.

**단점**

1. 앞의 코드는 웹 컨트롤러, 유스케이스, 영속성 어댑터가 단 하나씩만 있는 애플리케이션을 예로 든 것이다
   완전한 엔터프라이즈 애플리케이션을 실행하기 위해서는 이러한 코드가 훨씬 더 많아진다
2. 각 클래스가 속한 패키지 외부에서 인스턴스를 생성하기 때문에 이 클래스들은 전부 public 이어야 한다
   이렇게 되면 유스케이스가 영속성 어댑터에 직접 접근하는 것을 막지 못한다
   package-private 접근 제한자를 이용해서 이러한 원치 않은 의존성을 피할 수 있었다면 더 좋았을 것

## 스프링의 클래스패스 스캐닝으로 조립하기

스프링 프레임워크를 이용해서 애플리케이션을 조립한 결과물: 애플리케이션 컨텍스트(application context)  
애플리케이션 컨텍스트는 애플리케이션을 구성하는 모든 객체(bean)를 포함한다

스프링은 클래스패스 스캐닝으로 클래스패스에서 접근 가능한 모든 클래스를 확인해서 `@Component` 애너테이션이 붙은 클래스를 찾고 각 클래스의 객체를 생성한다  
이때 클래스는 필요한 모든 필드를 인자로 받는 생성자를 가지고 있어야 한다

1. `@Component` 애너테이션이 붙은 클래스를 찾는다 (`AccountPersistenceAdapter`)
2. 클래스의 생성자 인자 중 `@Component` 가 붙은 클래스들을 찾는다
   (JpaRepository 상속 받은 repository interface들은 spring data jpa 가 proxy를 만들어 빈으로 등록)
3. 그 클래스들의 인스턴스를 만들어 애플리케이션 컨텍스트에 추가한다
4. 필요한 객체들이 모두 생성되면 `AccountPersistenceAdapter` 의 생성자를 호출하고 객체를 애플리케이션 컨텍스트에 추가한다

```java
@RequiredArgsConstructor
@Component
class AccountPersistenceAdapter implements LoadAccountPort, UpdateAccountStatePort {
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final AccountMapper accountMapper;
  // ...
}
```

스프링이 인식할 수 있는 애너테이션을 직접 만들 수도 있다

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface PersistenceAdapter {
  
  @AliasFor(annotation = Component.class)
  String value() default "";
}
```

### 단점

1. 클래스에 프레임워크에 특화된 애너테이션을 붙여야 한다는 점에서 침투적이다
   - 강경한 클린 아키텍처파: 코드를 특정한 프레임워크와 결합시키기 때문에 사용하지 말아야 한다고 주장
2. 마법 같은 일이 일어나서 스프링 전문가가 아니라면 원인을 찾는 데 수일이 걸릴 수 있는 숨겨진 부수효과를 야기할 수도 있다
   - 클래스 패스 스캐닝은 스프링에게 부모 패키지를 알려주고 이 패키지 안에서 `@Component` 가 붙은 클래스를 찾으라고 지시한다 
   - 클래스 패스 스캐닝은 너무 둔하다
     - 클래스 하나하나에 대해 다 알기 힘들다
     - 애플리케이션 컨텍스트에 실제로는 올라가지 않았으면 하는 클래스가 있을 수 있다

## 스프링의 자바 컨피그로 조립하기

애플리케이션 컨텍스트에 추가할 빈을 생성하는 설정 클래스를 만든다

```java
@Configuration
@EnableJpaRepositories
class PersistenceAdapterConfiguration {
  
  @Bean
  AccountPersistenceAdapter accountPersistenceAdapter(
  	AccountRepository accountRepository,
    ActivityRepository activityRepository,
    AccountMapper accountMapper) {
    
    return new AccountPersistenceAdapter(
    	accountRepository,
    	activityRepository,
    	acountMapper)
  }
  
  @Bean
  AccountMapper accountMapper() {
    return new AccountMapper();
  }
}
```

여전히 클래스패스 스캐닝을 사용하지만 모든 빈을 가져오는 대신 설정 클래스만 선택하기 때문에 해로운 마법이 일어날 확률이 줄어든다

`AccountRepository`, `ActivityRepository` interface는 `@EnableJpaRepositories` 애너테이션으로 인해 스프링이 직접 생성해서 제공한다

메인 애플리케이션에도 `@EnableJpaRepositories` 와 같은 설정 클래스를 붙일 수 있다

- 애플리케이션이 시작할 때마다 JPA를 활성화해서 
- 영속성이 실질적으로 필요없는 테스트에서 애플리케이션을 실행할 때도 JPA 리포지토리들을 활성화 한다
- 그렇기 때문에 이러한 기능 애너테이션을 별도의 설정 모듈로 옮기는게 애플리케이션을 더 유연하게 만든다

비슷한 방법으로 웹 어댑터, 혹은 애플리케이션 계층의 특정 모듈을 위한 설정 클래스를 만들 수 있다

- 특정 모듈만 포함하고, 그 외의 다른 모듈의 빈은 모킹해서 애플리케이션 컨텍스트를 만들 수 있어 테스트에 큰 유연성이 생긴다

`@Component` 애너테이션을 코드 여기 저기에 붙이도록 강제하지 않아 스프링 프레임워크에 대한 의존성 없이 깔끔하게 유지할 수  있다

### 문제점

설정 클래스가 생성하는 빈이 설정 클래스와 같은 패키지에 존재하지 않는다면 빈들을 public으로 만들어야 한다

## 결론

- 클래스패스 스캐닝은 아주 편리한 기능이다
  - 하지만 코드의 규모가 커지면 금방 투명성이 낮아진다
  - 어떤 빈이 애플리케이션 컨텍스트에 올라오는지 정확히 알 수 없게 된다
  - 테스트에서 애플리케이션 컨텍스트의 일부만 독립적으로 띄우기 어려워진다
- 애플리케이션 조립을 책임지는 전용 설정 컴포넌트를 만들면
  - 서로 다른 모듈로부터 독립되어 코드 상에서 손쉽게 옮겨다닐 수 있는 응집도가 매우 높은 모델을 만들 수 있다
  - 하지만 설정 컴포넌트를 유지보수하는데 시간을 추가로 들여야 한다
