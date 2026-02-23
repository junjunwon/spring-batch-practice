# Spring Batch 학습 정리

## 1. 잡 파라미터 유효성 검증 (JobParametersValidator)

- `JobParametersValidator` 인터페이스로 잡 실행 전 파라미터 검증 수행
- 검증 실패 시 예외 발생으로 잡 실행 차단
- **CompositeJobParametersValidator**: 여러 검증기를 조합하여 사용 가능

## 2. 잡 파라미터 증가 (JobParametersIncrementer)

- 동일 잡을 여러 번 실행할 때 `JobInstanceAlreadyCompleteException` 방지
- `JobParametersIncrementer` 구현체로 매 실행마다 고유한 파라미터 생성
- **RunIdIncrementer**: `run.id` Long 타입 자동 증가 (기본 제공)
- **DailyJobTimeStamper**: 날짜 단위로 실행하고 싶을 때 커스텀 구현 (예: `run.date` 파라미터)

## 3. JobExecutionListener (잡 리스너)

- `beforeJob`, `afterJob` 콜백으로 잡 시작/종료 시점에 로직 실행
- **주의**: `afterJob`은 잡의 완료 상태(SUCCESS/FAILED)와 관계없이 항상 호출됨
- **@BeforeJob, @AfterJob**: 인터페이스 구현 없이 메서드에 애너테이션만 붙여 사용 가능
- 단, 스프링 배치에 등록하려면 `JobExecutionListenerFactoryBean` 등으로 래핑 필요

## 4. ExecutionContext와 승격

- `ExecutionContext`는 기본적으로 잡의 세션 역할
- Step 간 데이터 공유: StepExecution의 ExecutionContext → JobExecution의 ExecutionContext로 **승격**
- **ExecutionContextPromotionListener**: Step이 성공했을 때만 지정한 키를 Job 레벨로 승격
- 첫 번째 스텝이 성공했을 때만 후속 스텝에 데이터를 넘기고 싶을 때 유용

## 5. BATCH_JOB_EXECUTION_CONTEXT

- `SERIALIZED_CONTEXT` 컬럼: 직렬화된 자바 객체
- 잡이 **실행 중이거나 실패한 경우**에만 채워짐 (정상 완료 시 비워질 수 있음)

## 6. Tasklet과 RepeatStatus

- Tasklet 처리 완료 시 `RepeatStatus` 반환
- **CONTINUABLE**: 해당 Tasklet을 다시 실행하라는 의미
- **FINISHED**: Tasklet 완료, 다음 Step으로 진행

## 7. CallableTaskletAdapter

- Tasklet 로직을 `Callable`로 감싸 **다른 스레드**에서 실행
- 스텝 실행 스레드와 별개 스레드에서 동작 (병렬 실행이 아님)
- `Callable`이 유효한 `RepeatStatus`를 반환할 때까지 스텝은 완료로 간주되지 않음

## 8. 청크 크기 구성

### 정적 커밋 개수
- `chunk(size)`로 고정 개수 지정

### CompletionPolicy
- 청크 완료 여부를 동적으로 결정
- **SimpleCompletionPolicy**: 고정 개수 기반 (chunk와 유사)
- **TimeoutTerminationPolicy**: 시간 기반으로 청크 종료
