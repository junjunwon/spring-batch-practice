# jobparameterflow Job

## 개요
JobParameters 검증부터 Tasklet Late Binding, Scheduler를 통한 Job 실행,  
그리고 ExecutionContext 동작 차이까지 **Job 실행 전–중 흐름을 한 번에 검증**하기 위한 Job입니다.

Spring Batch에서
> “Job은 어떻게 실행되고, 파라미터는 언제/어디서 주입되며,  
ExecutionContext는 어떤 범위에서 동작하는가?”

를 실습 코드로 확인하는 것이 목적입니다.

---

## 학습 목적

### 1. JobParametersValidator 적용
- `JobParametersValidator`를 직접 구현하여 Job 실행 전 파라미터 검증
- 필수 파라미터(`name`) 존재 여부 검증
- 유효하지 않은 경우 `InvalidJobParametersException` 발생
- Job 실행 단계 이전에 검증 실패 시 **Job 자체 실행 차단**

→ Job 실행 전 파라미터 안정성 확보

---

### 2. Tasklet + Late Binding 적용
- `@StepScope`
- `@Value("#{jobParameters['name']}")`

을 사용하여 **Job 실행 시점에 파라미터를 주입(Late Binding)**

#### 확인 포인트
- JobParameters는 Bean 생성 시점이 아닌 Step 실행 시점에 바인딩됨
- Tasklet 내부에서 Job / Step 메타 정보 접근 가능
- JobParameters → Tasklet → ExecutionContext로 값 전달 흐름 확인

---

### 3. Scheduler를 통한 Job 실행
- `JobOperator`를 사용하여 Scheduler 기반 Job 실행
- Spring Batch 6 기준 구조

#### Scheduler 특징
- `name` 파라미터를 동적으로 주입
- `timestamp` 파라미터 추가
    - 동일 JobParameters로 인한 JobInstance 중복 실행 방지

→ 외부 트리거 기반 Batch 실행 구조 이해

---

### 4. ExecutionContext 동작 차이 검증

ExecutionContext의 **조회/수정 가능 범위**를 코드로 직접 검증

#### JobExecutionContext
- `ChunkContext`를 통해 조회 가능
- **읽기 전용**
- 값 변경 시 실제 반영되지 않음

#### JobExecution.getExecutionContext()
- 실제 수정 가능한 ExecutionContext
- 값 변경 시 정상 반영

→ ExecutionContext의 책임 범위 및 사용 위치 명확히 이해

---

## Job 구성

### Job
- Job Name: `jobParameterFlowJob`

### Step
- Tasklet 기반 단일 Step
- JobParameters 검증 → Tasklet 실행 → ExecutionContext 조작

---

## 주요 클래스
- `JobParameterFlowJobConfig`
- `JobParameterFlowTasklet`
- `ParameterValidator`
- `BatchScheduler`

---

## 실행 시 주의사항
- `name` 파라미터는 필수
- `timestamp` 파라미터는 중복 실행 방지를 위해 항상 포함

---

## 핵심 정리
- JobParametersValidator는 **Job 실행 이전**에 동작
- Late Binding은 **Step 실행 시점**에 적용
- JobExecutionContext ≠ JobExecution.getExecutionContext()
- Scheduler 기반 실행 시 JobOperator 사용

---

## 확장 아이디어
- Step 분리 후 ExecutionContext 승격 흐름 확인
- Job 실패/재시작 시 ExecutionContext 유지 여부 확인
- StepExecutionContext → JobExecutionContext 승격 시나리오 추가

