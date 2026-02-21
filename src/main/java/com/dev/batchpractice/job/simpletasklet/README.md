# simpletasklet Job

## 개요
Spring Batch에서 **가장 단순한 형태의 Job + Step + Tasklet** 구성을 실습하기 위한 Job입니다.  
별도의 Reader / Processor / Writer 없이,  
람다 기반 Tasklet을 사용하여 Step 실행 흐름을 확인하는 것이 목적입니다.

---

## 학습 목적
- Spring Batch Job / Step 기본 구조 이해
- Tasklet 기반 Step 동작 방식 확인
- JobRepository를 사용하는 Job / Step 생성 방식 학습
- RepeatStatus.FINISHED 의미 이해

---

## Job 구성

### Job
- Job Name: `simpleJob`
- 단일 Step으로 구성

### Step
- Step Name: `simpleStep1`
- Tasklet 방식으로 구현
- Step 실행 시 콘솔 로그 출력

```java
System.out.println(">> This is Step 1");
