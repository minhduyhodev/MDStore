# MDStore

Hệ thống Dropshipping tài khoản số tự động — mua từ nhà cung cấp sỉ (VietShare...), giao hàng tức thì cho người dùng cuối qua API.

> **Bắt đầu tại đây:** [docs/CLAUDE.md](./docs/CLAUDE.md) — entrypoint cho agent và developer mới.

---

## Quick Start

### Yêu Cầu Hệ Thống
- Docker & Docker Compose
- JDK 21+
- Node.js 20+

### 1. Thiết Lập Biến Môi Trường

Copy file `.env.example` thành `.env` tại thư mục gốc:

```env
# Database
POSTGRES_DB=mdstore
POSTGRES_USER=admin
POSTGRES_PASSWORD=secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# VietShare API
VIETSHARE_API_URL=https://token.vietshare.site/v1
VIETSHARE_API_KEY=your_api_key_here
VIETSHARE_API_SECRET=your_secret_key_here
```

### 2. Khởi Động Infrastructure (DB + Redis)

```bash
docker-compose up -d
```

### 3. Khởi Động Ứng Dụng

**Backend:**
```bash
cd mdstore-backend
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd mdstore-frontend
npm install
npm run dev
```

---

## Tài Liệu Kỹ Thuật

Đọc theo thứ tự sau trước khi code:

1. [CLAUDE.md](./docs/CLAUDE.md) — entrypoint, thứ tự đọc theo task
2. [01-overview.md](./docs/01-overview.md) — bối cảnh dự án, mục tiêu
3. [02-decisions.md](./docs/02-decisions.md) — kiến trúc, quyết định đã chốt
4. [03-db-schema.md](./docs/03-db-schema.md) — schema DB (nguồn sự thật)
5. [04-suppliers/vietshare.md](./docs/04-suppliers/vietshare.md) — API VietShare
6. [05-business-flows.md](./docs/05-business-flows.md) — luồng nghiệp vụ