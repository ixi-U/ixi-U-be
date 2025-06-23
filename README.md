# 📡 ixi-U: 자연어 기반 통신 요금제 추천 챗봇 서비스

## 📍프로젝트 소개

> **프로젝트 기간**: 2025.06.02 ~ 2025.06.24
>
> ixi-U는 사용자의 통신 사용 습관을 자연어로 입력받아 최적의 요금제를 추천하는 **대화형 챗봇 서비스**입니다.<br>
> 복잡한 요금제 탐색 과정을 단축하고, **개인화된 추천**을 통해 통신비 절감과 만족도 향상을 목표로 합니다.

### 기획 배경

1. **낮은 요금제 만족도**
    - 소비자 만족도가 30%에 불과하며, 요금제 선택의 핵심 기준인 ‘요금’ 항목이 가장 불만족스러운 요소로 꼽힘

2. **복잡한 비교의 어려움**
    - 수십여 개 요금제 옵션을 일반 사용자가 일일이 비교·분석하기 어려워 비용 낭비 및 서비스 미스매칭 발생

3. **기존 챗봇의 한계**
    - 기존 챗봇은 키워드 매칭과 정형화된 필터링에만 의존해 자연어 이해가 부족하고 유연한 응답 제공이 어려움

### 프로젝트 목표 및 기대 효과

- 복잡한 요금제 선택 과정에서의 혼란 해소 &rarr; **의사결정 간소화**

- 자연어 인터페이스를 통한 편의성 강화 &rarr; **사용자 경험(UX) 증대**

- 맞춤형 요금제 추천으로 통신비 절감 &rarr; **비용 절감·고객 만족도 제고**

---

## 🚀 핵심 기능
![](https://github.com/user-attachments/assets/bd5b53db-90ce-46d9-8932-67bb7d454b07)

---

## ✨ 주요 성과

### [1️⃣ 챗봇 성능 고도화](https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%92%AC-AI-%EC%B1%97%EB%B4%87-%EA%B0%9C%EB%B0%9C)

#### 추천 정확도 향상을 위한 복합적인 프롬프트 엔지니어링 기법 적용

1. Role Prompting (역할 기반 프롬프팅)

2. Chain-of-Thought, COT (단계적 추론)

3. Few-Shot-Learning with Example (예제 학습)

4. Question And Negative (or Negative Examples) 부정 학습

5. Constraint-Based Prompting (제약 기반 프롬프팅)

<div align="center">
  <img src="https://github.com/user-attachments/assets/3f313c35-eb5a-4454-b7ee-de764e6a7c06" width="800" />
</div><br>

#### Spring AI를 활용한 RAG 시스템 구축을 통한 사용자 맞춤형 추천 구현

<br><div align="center">
  <img src="https://github.com/user-attachments/assets/93bfce87-9df7-4839-9429-478a191938fd" width="500" />
</div>

<br><br>

### [2️⃣ 금칙어 성능 고도화](https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%9A%A8-%EA%B8%88%EC%B9%99%EC%96%B4-%ED%95%84%ED%84%B0%EB%A7%81-%EA%B0%9C%EB%B0%9C)

#### 문제 1

> 기존에는 ```String.contains``` 를 고려하였으나 변형된 금칙어를 탐지할 수 없다는 우려 발생 

#### 접근 

> Aho–Corasick, 단순 Contains, RAG 기반(벡터화 및 토크나이징), LLM 방식 각각의 오류율과 응답 속도를 측정하여 가장 적합한 금칙어 검출 방법을 선정하기로 결정

<div align="center">
  <img src="https://github.com/user-attachments/assets/63c78170-f703-45da-bbee-70324990d4d3" width="600" />
</div><br>

#### 성과 및 결론 

> 변형된 금칙어는 RAG 및 LLM 기반 방식으로 검출할 수 있으나, 이 과정에서 정상 데이터를 금칙어로 오판할 가능성이 높아지고 응답 속도도 느려짐

#### 문제 2

> 기존에는 ```String.contains``` 를 고려하였으나 시간복잡도가 O(N*M) 이므로 응답 속도에 대한 우려 발생 

#### 접근

> 아호코라식과 contains를 비교하여 상황 별 응답 속도 차이를 관측

<div align="center">
  <img src="https://github.com/user-attachments/assets/d689877c-bf23-4847-a436-17746b6afcd5" width="600" />
</div><br>

#### 성과 및 결론 

> 앞으로 데이터 및 프로젝트의 확장성을 고려하여 아호코라식 방식을 채택 

#### 최종 결론

> **금칙어 필터링에 아호코라식 알고리즘 적용**

<br><br>

### [3️⃣ Redis 캐싱](https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%92%A8-Redis-%EC%BA%90%EC%8B%B1)

#### 문제 
> 자주 조회되는 요금제 페이지에 대한 부하상황 증가 가능성. 이에 따른 응답 속도 하락 문제 발생 가능

#### 접근 
> JMeter를 활용한 부하 테스트를 실시하여 시스템의 응답 속도 개선 여부 검증

##### 테스트 환경

- Concurrent Users: 1,000

- Test Duration: 60초

- Dataset Size: 약 5,000개 요금제

##### 부하 설정 근거

- LG U+ 사이트 일일 방문자: 약 25,200명

- 평균 세션(5분) 동시접속자: (25,200 × 300) ÷ 86,400 ≈ 87.5명

- 피크 계수(1.52) 적용 시: 135~175명

- 안정성 검증을 위해 1분 1,000명 환경으로 설정

##### 테스트 시나리오

- Baseline (Pre-Cache): 캐시·인덱스 미적용

- Optimized (With Cache): 캐시 + 인덱스 적용

#### 성과 및 결론 

> 캐시 및 인덱스 적용으로 목록 조회 평균 응답이 **31 ms &rarr; 6 ms(80%↓)**, P95 **38 ms &rarr; 12 ms(68%↓)**, 개수 조회 평균
**13 ms &rarr; 4 ms(69%↓)**, P95 **18 ms &rarr; 6 ms(66%↓)** 로 대폭 개선

---

## ⚙️ 기술 스택

<img src="https://github.com/user-attachments/assets/2b0a6ee6-4ff0-4fa1-9432-641244086261" width="1000">

---

## 🛠️ 시스템 아키텍처

<img src="https://github.com/user-attachments/assets/1cc3a373-7581-49db-9c1c-0b7ed34cdcf8" width="700">

---

## [📚 Github Wiki](https://github.com/ixi-U/ixi-U-be/wiki)

> 저희 팀의 고민이 담긴 깃허브 위키입니다✨

<ul>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%9B%A0%EF%B8%8F-%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D-%EC%84%A0%ED%83%9D-%EC%9D%B4%EC%9C%A0"> 🛠️ 기술 스택 선택 이유</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%93%91-Team-Convention"> 📑 Team Convention</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%97%93%EF%B8%8F-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%9D%BC%EC%A0%95"> 🗓️ 프로젝트 일정 </a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%8F%97%EF%B8%8F-Graph-DB-%EC%84%A4%EA%B3%84"> 🏗️ Graph DB 설계 </a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%92%AC-AI-%EC%B1%97%EB%B4%87-%EA%B0%9C%EB%B0%9C"> 💬 AI 챗봇 개발</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%9A%A8-%EA%B8%88%EC%B9%99%EC%96%B4-%ED%95%84%ED%84%B0%EB%A7%81-%EA%B0%9C%EB%B0%9C"> 🚨 금칙어 필터링 개발</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%94%A7-Cypher-%EC%BF%BC%EB%A6%AC-%ED%8A%9C%EB%8B%9D"> 🔧 Cypher 쿼리 튜닝</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%92%A8-Redis-%EC%BA%90%EC%8B%B1"> 💨 Redis 캐싱</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%93%84-CypherDSL"> 📄 CypherDSL</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%94%92-Social-Login-&-%EC%9D%B8%EC%A6%9D-%EC%9D%B8%EA%B0%80"> 🔐 Social Login & 인증/인가</a></li>
<li><a href="https://github.com/ixi-U/ixi-U-be/wiki/%F0%9F%91%B7-%EC%9D%B8%ED%94%84%EB%9D%BC-&-CI-CD"> 👷 인프라 & CI/CD </a></li>
</ul>

---

## 🧑🏻‍💻 역할 분담

| <img src="https://avatars.githubusercontent.com/Minjae-An" width="80"><br><a href="https://github.com/Minjae-An">👑 안민재</a> | <img src="https://avatars.githubusercontent.com/yereumi" width="80"><br><a href="https://github.com/yereumi">신예지</a> | <img src="https://avatars.githubusercontent.com/Leesowon" width="80"><br><a href="https://github.com/Leesowon">이소원</a> | <img src="https://avatars.githubusercontent.com/dionisos198" width="80"><br><a href="https://github.com/dionisos198">이진우</a> | <img src="https://avatars.githubusercontent.com/hyeonZIP" width="80"><br><a href="https://github.com/hyeonZIP">임재현</a> | <img src="https://github.com/user-attachments/assets/4304fcd3-369c-46d1-b369-d4ca0569e4e3" width="80"><br><a href="https://github.com/tmdals1207">홍승민</a> |
|-----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------| 
| <ul><li>PM</li><li>개발 총괄</li></ul>                                                                                          | <ul><li>요금제 시스템</li><li>금칙어 시스템</li><li>성능 고도화</li></ul>                                                             | <ul><li>인프라</li><li>인증 / 인가 시스템</li></ul>                                                                              | <ul><li>DB 모델링</li><li>요금제 시스템</li><li>금칙어 시스템</li><li>금칙어 고도화</li></ul>                                                     | <ul><li>챗봇 시스템</li><li>어드민 시스템</li><li>챗봇 고도화</li></ul>                                                                | <ul><li>데이터 수집</li><li>DB 모델링</li><li>인프라</li><li>회원 시스템</li></ul>                                                                                        |
