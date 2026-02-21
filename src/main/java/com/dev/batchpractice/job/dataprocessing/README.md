# dataprocessing Job

## 개요
입력 데이터를 읽고 처리한 뒤 출력 저장까지 수행하는 **Reader → Processor → Writer** 풀 파이프라인을 실습하기 위한 Job입니다.
외부 API 호출, 엔티티 매핑, 트랜잭션과 청크 단위 커밋 동작을 함께 확인합니다.

---

## 학습 목적
- Chunk 기반 Step 동작 이해 (읽기/처리/쓰기 사이클)
- ItemReader / ItemProcessor / ItemWriter의 책임 분리
- 외부 API 연동 처리 흐름 이해 (Processor 내부 호출)
- 트랜잭션 경계와 커밋 크기(청크 사이즈) 동작 확인
- ExecutionContext를 통한 상태 관리 포인트 파악

---

## Job 구성

### Job
- Job Name: `dataProcessingJob`
- 단일 또는 다중 Step 구성 (입력 초기화 → 데이터 처리)

### Step (예: dataProcessingStep)
- Chunk-Oriented Processing
- Reader: DB에서 입력 엔티티 조회 (또는 커서/페이징 기반)
- Processor: 외부 API 호출/비즈니스 로직 적용 → 출력 엔티티 생성
- Writer: 결과 엔티티 저장

---

## 실행 흐름
1. Reader가 입력 엔티티(BatchInput)를 읽음
2. Processor가 `ExternalApiService` 등을 통해 가공 → `BatchOutput`으로 변환
3. Writer가 `BatchOutputRepository`로 저장
4. 청크 단위 커밋으로 성능/일관성 관리

---

## 주요 클래스
- `BatchJobConfig`: 데이터 처리 Job/Step 설정
- Reader: `BatchInputItemReader`
- Processor: `ApiCallItemProcessor`
- Writer: (프로젝트의 writer 패키지 구성에 따름)
- 엔티티/리포지토리: `BatchInput`, `BatchOutput`, `BatchInputRepository`, `BatchOutputRepository`
- 서비스: `ExternalApiService`
- 보조: `DataInitializationTasklet`(초기 데이터 준비 시 사용), `BatchPerformanceListener`(성능 측정/로그)

---

## 파라미터
- 필요 시 `name`, `timestamp` 등 JobParameters를 사용하여 실행 구분 및 동적 처리
- Late Binding이 필요한 경우 `@StepScope` + `@Value("#{jobParameters['key']}")` 활용

---

## 실행 시 주의사항
- 동일 파라미터로 재실행 시 기존 JobInstance와 충돌할 수 있으므로 구분자(`timestamp`) 권장
- 외부 API 실패/타임아웃에 대한 예외 처리 및 재시도 정책 검토
- 대용량 처리 시 청크 사이즈/트랜잭션 경계 조정

---

## 확장 아이디어
- 다중 Step으로 전처리/본처리/후처리 분리
- 실패/재시작 시 ExecutionContext를 활용한 재처리 지점 관리
- 스루풋 측정 및 병렬 처리(TaskExecutor) 적용으로 성능 개선

---

## JobParameters 검증 및 파라미터 관리

- `JobParametersValidator`를 활용하여 Job 실행 전 파라미터 검증 가능
- 여러 Validator를 조합하는 Composite 구조 확장 가능
- 실행 모드별 Validator 분리 및 @Qualifier로 선택 주입 가능
- 파라미터 Late Binding: `@StepScope`, `@Value("#{jobParameters['name']}")` 등 사용 시 Job 실행 시점에 파라미터 주입

---

## incrementer와 외부 파라미터 동시 사용 이슈

- Job에 incrementer가 존재하면 외부 파라미터(run.id 등) 무시됨 → incrementer 결과만 사용
- 실무 적용 기준:
  - 외부 파라미터 기반 Job: incrementer 사용하지 않음
  - 자동 재실행 Job: incrementer 사용
  - 실행 시점에 run.id 또는 timestamp 직접 추가 방식 권장
