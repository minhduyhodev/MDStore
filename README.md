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

Copy file `.env.example` thành `.env` rồi điền giá trị thật vào:

```bash
cp .env.example .env
# Mở .env bằng editor và điền giá trị thật cho từng biến
```

> ⚠️ **Không dùng giá trị mẫu trong tài liệu này cho môi trường thật.**  
> File `.env` đã được `.gitignore` — **không commit file này lên git**.  
> Chỉ file `.env.example` (không chứa giá trị thật) mới được commit.

Các biến cần điền (xem đầy đủ trong [`.env.example`](./.env.example)):
- `POSTGRES_USER` / `POSTGRES_PASSWORD` — credential PostgreSQL
- `VIETSHARE_API_KEY` / `VIETSHARE_API_SECRET` — lấy từ dashboard VietShare


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
cd client
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