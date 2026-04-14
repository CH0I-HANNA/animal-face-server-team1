# AnimalFace Server

얼굴 사진을 업로드하면 어떤 동물상인지 AI가 분석해주는 Spring Boot 백엔드 서버입니다.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| Java 21 + Spring Boot 3.4.3 | 메인 프레임워크 |
| Spring Security + JWT | 인증/인가 |
| JPA + MySQL / H2 | 데이터베이스 |
| AWS S3 (SDK v2) | 이미지 스토리지 |
| Spring Cloud OpenFeign | AI 서버 HTTP 통신 |
| Swagger (springdoc 2.8.5) | API 문서 |
| Spring Mail | 이메일 발송 |

---

## 프로젝트 구조

```
com.likelion.animalface
├── domain
│   ├── user          # 인증 + 회원 도메인
│   └── analysis      # 동물상 분석 + 스토리지 도메인
├── global
│   ├── security      # JWT 필터/프로바이더
│   ├── config        # Security, S3, Feign, Swagger 설정
│   ├── dto           # 공통 ApiResponse
│   └── common        # BaseTimeEntity
└── infra
    └── ai            # AI 서버 Feign 클라이언트
```

---

## 주요 기능

### 인증 (Auth)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (Access + Refresh Token 발급) |
| POST | `/api/v1/auth/reissue` | 토큰 재발급 (Refresh Token Rotation) |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/auth/find-id` | 이메일로 아이디 찾기 |
| POST | `/api/v1/auth/temp-password` | 임시 비밀번호 발급 및 이메일 발송 |

### 회원 (Member)

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/members/me` | 내 정보 조회 |
| PATCH | `/api/v1/members/me/password` | 비밀번호 변경 |

### 동물상 분석 (Analysis)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/analysis` | 이미지 URL로 동물상 AI 분석 |
| GET | `/api/v1/analysis/{id}` | 분석 결과 단건 조회 |
| GET | `/api/v1/analysis/my` | 내 분석 기록 목록 (페이지네이션) |

### 스토리지 (Storage)

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/v1/storage/presigned-url` | S3 Presigned PUT URL 발급 |

---

## 이미지 업로드 흐름

```
1. POST /api/v1/storage/presigned-url
   → S3 Presigned URL 발급 (유효 10분)

2. 클라이언트가 발급받은 URL로 S3에 직접 PUT 업로드

3. POST /api/v1/analysis
   → AI 서버에 imageUrl 전달 → 분석 결과 저장 및 반환
```

> 이미지가 서버를 거치지 않고 클라이언트에서 S3로 직접 업로드되어 서버 부하를 줄입니다.

---

## 인증 흐름

- 로그인 시 **Access Token**(단기)과 **Refresh Token**(14일) 발급
- Refresh Token은 DB에 저장하여 관리
- 모든 인증 요청은 `Authorization: Bearer {accessToken}` 헤더 필요
- 토큰 만료 시 `/auth/reissue`로 재발급 (Refresh Token Rotation)

---

## 동물상 종류

| 타입 | 설명 |
|------|------|
| CAT | 고양이상 |
| DOG | 강아지상 |
| FOX | 여우상 |
| BEAR | 곰상 |

---

## 공통 응답 형식

```json
{
  "success": true,
  "data": { ... },
  "message": "요청이 성공적으로 처리되었습니다."
}
```

```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지"
}
```

---

## API 문서

서버 실행 후 Swagger UI에서 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```