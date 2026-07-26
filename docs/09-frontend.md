# 09 — Frontend (Next.js)

> File này giải thích cấu trúc project frontend, cách chạy, và lý do đằng sau
> các quyết định kỹ thuật. Đọc file này trước khi bắt đầu code frontend.

---

## Cách chạy

```bash
# 1. Vào thư mục client (chữ thường — do npm naming restriction)
cd client

# 2. Copy file môi trường rồi điền giá trị thật
cp .env.example .env.local
# Mở .env.local, điền NEXT_PUBLIC_API_BASE_URL (ví dụ: http://localhost:8080)

# 3. Cài dependencies (chỉ cần làm 1 lần)
npm install

# 4. Chạy dev server
npm run dev
# → Mở http://localhost:3000 trên browser
```

> **Tại sao dùng `.env.local` thay vì `.env`?**
> Đây là quy ước riêng của Next.js — nó tự đọc `.env.local` mà không cần cấu hình thêm.
> `.env.local` cũng tự động bị ignore bởi Next.js gitignore, an toàn hơn khi dùng nhầm tên.

---

## Stack sử dụng

| Package | Mục đích |
|---|---|
| **Next.js 16 (App Router)** | Framework React — routing tự động theo cấu trúc thư mục, render phía server |
| **JavaScript** (không TypeScript) | Ngôn ngữ lập trình — ưu tiên đơn giản, dễ đọc cho người mới |
| **Tailwind CSS** | Styling bằng class trực tiếp trong JSX, không cần viết CSS file riêng |
| **ESLint** | Bắt lỗi cú pháp và bad practice khi code |

---

## Cấu trúc thư mục

```
client/
├─ src/
│   ├─ app/                     ← ROUTING — mỗi folder = 1 route URL
│   │   ├─ layout.js            ← Khung chung (Sidebar) — bọc tất cả trang
│   │   ├─ page.js              ← Route / → redirect về /catalog
│   │   ├─ catalog/
│   │   │   └─ page.js          ← Route /catalog (trang sản phẩm)
│   │   ├─ orders/
│   │   │   └─ page.js          ← Route /orders (lịch sử đơn hàng)
│   │   └─ wallet/
│   │       └─ page.js          ← Route /wallet (ví tiền)
│   │
│   ├─ components/              ← Component dùng chung ở 2+ trang
│   │   ├─ Sidebar.js           ← Thanh điều hướng trái (Server Component)
│   │   └─ Header.js            ← Tiêu đề trang (Server Component)
│   │
│   └─ lib/
│       └─ api.js               ← Hàm fetch() duy nhất + xử lý response envelope
│
├─ .env.local                   ← Biến môi trường (KHÔNG commit — đã gitignore)
├─ .env.example                 ← Template (commit được — không có giá trị thật)
└─ next.config.mjs              ← Cấu hình Next.js
```

### Quy tắc App Router: folder = route

Khác với React Router cũ (khai báo routes thủ công trong code), Next.js App Router
dùng **cấu trúc thư mục** để tạo route tự động:

```
src/app/catalog/page.js  →  URL: /catalog
src/app/orders/page.js   →  URL: /orders
src/app/wallet/page.js   →  URL: /wallet
```

Muốn thêm trang mới `/admin` → chỉ cần tạo file `src/app/admin/page.js`. Không cần
sửa bất kỳ file cấu hình nào.

---

## Server Component vs Client Component

> Đây là khái niệm quan trọng nhất của Next.js App Router. Người mới thường nhầm.

### Hãy tưởng tượng thế này:

- **Server Component** = nhà bếp (khuất sau sân khấu): nấu xong, bưng món ra cho khách.
  Khách không thấy bếp, không tương tác với bếp.

- **Client Component** = bàn ăn (trước mắt khách): khách gọi thêm, lắc bình nước, bấm nút.
  Mọi tương tác trực tiếp xảy ra ở đây.

### Luật đơn giản:

| Component cần gì? | Loại component | Cần `"use client"`? |
|---|---|---|
| Chỉ render HTML tĩnh | Server Component | ❌ Không |
| Dùng `useState` / `useEffect` | Client Component | ✅ Có |
| Có `onClick`, `onChange`... | Client Component | ✅ Có |
| Gọi fetch() khi trang load | Client Component | ✅ Có |

### Ví dụ từ code thật trong project này:

**`src/components/Sidebar.js` — KHÔNG có `"use client"`:**
```js
// Sidebar chỉ render menu — không có state, không có sự kiện
// → Server Component, Next.js render phía server → HTML gửi về browser
import Link from 'next/link';

export default function Sidebar() {
  return (
    <aside>
      <Link href="/catalog">🛍️ Sản phẩm</Link>
      {/* ... */}
    </aside>
  );
}
```

**`src/app/catalog/page.js` — CÓ `"use client"` ở dòng đầu tiên:**
```js
'use client';
// ☝️ PHẢI có dòng này vì dùng useState và useEffect
// Nếu không có → Next.js nghĩ đây là Server Component →
// useState/useEffect không tồn tại ở server → lỗi ngay

import { useState, useEffect } from 'react';

export default function CatalogPage() {
  const [products, setProducts] = useState([]);  // ← lý do cần use client

  useEffect(() => {                               // ← lý do cần use client
    // load data...
  }, []);

  return <table>...</table>;
}
```

### Tại sao quan trọng?

- Server Component render phía server → HTML gửi thẳng về browser → **trang load nhanh hơn**.
- Client Component gửi JavaScript về browser → browser chạy JS → **tương tác được**.
- Quy tắc: **mặc định là Server Component** — chỉ thêm `"use client"` khi thật sự cần.

---

## Mock data

Hiện tại mỗi trang dùng mock data khai báo trong file (hằng số `MOCK_*`).
Khi backend sẵn sàng, chỉ cần:
1. Bỏ `setTimeout` + mock data.
2. Thay bằng `await apiFetch('/api/v1/...')` từ `@/lib/api.js`.

Không cần sửa phần render (JSX) vì data shape giữ nguyên.

---

## Xử lý response envelope (ADR-007)

Backend trả về dạng:
```json
{ "success": true, "data": {...}, "meta": { "timestamp": "..." } }
```

Hàm `apiFetch()` trong `src/lib/api.js` tự bóc tách envelope — component chỉ nhận
thẳng phần `data`.

Chi tiết envelope: xem [02-decisions.md — ADR-007](./02-decisions.md).

---



## Biến môi trường

| Biến | Mô tả |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | URL gốc của backend (ví dụ: `http://localhost:8080`) |

> **Tại sao phải có tiền tố `NEXT_PUBLIC_`?**
> Next.js mặc định KHÔNG expose biến môi trường ra browser (vì security).
> Chỉ biến có `NEXT_PUBLIC_` mới được gửi về browser.
> Biến không có prefix chỉ dùng được ở Server Component / API routes.
