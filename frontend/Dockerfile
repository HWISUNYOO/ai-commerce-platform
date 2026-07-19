# syntax=docker/dockerfile:1
# 1) 빌드 스테이지: Vite로 정적 자산 생성
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 2) 실행 스테이지: nginx로 정적 파일 서빙 + /api 리버스 프록시
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
