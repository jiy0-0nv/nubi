import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// 개발 서버는 백엔드(Spring Boot, 8080)로 API와 업로드 이미지를 함께 프록시합니다.
//  - /api/**     : 모든 REST 엔드포인트
//  - /uploads/** : 숙소 사진 정적 서빙 (AdminRoomImageResponseDTO.url 이 이 경로로 내려옴)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
