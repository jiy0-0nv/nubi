# 누비

 > 모든 게스트 하우스를 누벼라

## Members
 - 문유식
 - 이재원
 - 장연주
 - 정지윤
 - ** special guest : 김진욱 **


### 화이팅



## 실행 방법 (로컬)

requirements: JDK 21, Node.js 20+, MySQL 8.0+

### 1. Database

MySQL에 접속해서 빈 데이터베이스를 생성해주세요.
테이블은 백엔드가 뜰 때 JPA 엔티티 기준으로 자동 생성됩니다.

```sql
CREATE DATABASE accommodation_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. env 파일 세팅 & Backend 실행

`backend\.env` 파일을 생성하고 아래 값들을 채워주세요.

```
# Database
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# JWT
JWT_SECRET=

# Mail
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=

# File Upload Path
FILE_UPLOAD_DIR=
```

이후 IDE에서 백엔드 서버를 실행해주세요.
IntelliJ 사용시 PR #48에서 env 설정 관련을 확인하세요.

### 3. Dummy Data

스크립트를 실행하여 DB에 더미 데이터를 채워주세요.

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\seed-dummy-data.ps1
```


### 4. 프론트엔드 실행

```bash
cd frontend
npm run dev
```

