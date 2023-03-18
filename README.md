# comeeatme-backend

> 맛집 SNS 컴잇미 백엔드

- [Figma](https://www.figma.com/file/tiXjGdGe9K7i2qjwRCr23N/ComeEatMe?node-id=0%3A1&t=iicpfwgzb4P2EYRt-1)
- [ERD](https://www.erdcloud.com/d/6s6iyQRF5d26HMJZL)


## 환경 변수

- `OAUTH2_KAKAO_CLIENT_ID`
- `OAUTH2_KAKAO_CLIENT_SECRET`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_VALIDITY`
- `JWT_REFRESH_TOKEN_VALIDITY`
- `DATABASE_URL_DEV`
- `DATABASE_NAME_DEV`
- `DATABASE_USERNAME_DEV`
- `DATABASE_PASSWORD_DEV`
- `AWS_S3_BUCKET`
- `AWS_ACCESS_KEY`
- `AWS_SECRET_KEY`


## 기술 스택

- spring-web, spring-data-jpa(+Querydsl), spring-security
- JWT, OAuth2
- MySQL
- flyway
- JUnit5, Mockito
- AWS S3