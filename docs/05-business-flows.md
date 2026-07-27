# 05 — Luồng Nghiệp Vụ (Business Flows)

> Mỗi luồng được viết với **ví dụ cụ thể**, không chỉ lý thuyết.
> Input/Output mẫu, xử lý lỗi từng bước — đủ để implement mà không cần đoán.

---

## Flow 1: Người Dùng Đặt Đơn Hàng

### Điều kiện đầu vào (Pre-conditions)
- User đã đăng nhập và có số dư đủ.
- `product_id` tồn tại trong DB và `is_active = true`.
- `supplier_products` cho product này tồn tại, `is_active = true`.

### Các bước xử lý

```
1. Frontend → POST /api/v1/orders { product_id, quantity }

2. Backend: Rate Limit check (Bucket4j)
   - Nếu vượt giới hạn → trả HTTP 429, không tạo đơn

3. Backend: Validate
   - Product tồn tại? is_active?
   - Supplier của product tồn tại? is_active?
   - Số dư user >= total_amount?
   → Nếu fail bất kỳ check nào → trả HTTP 400 với message rõ ràng

4. Backend: Tạo đơn hàng
   - INSERT INTO orders (status = PENDING, idempotency_key = UUID)
   - Trừ số dư user (reserve, chưa phải commit)

5. Backend: Ký HMAC-SHA256 và gọi VietShare API
   - POST /v1/orders với Idempotency-Key = orders.idempotency_key

6A. VietShare trả HTTP 200 (SUCCESS):
    - UPDATE orders SET status = 'COMPLETED'
    - INSERT INTO delivered_accounts (order_id, account_data)
    - Commit số dư (trừ hẳn)
    - Trả về HTTP 200 + dữ liệu tài khoản cho Frontend

6B. VietShare trả HTTP 409 PRICE_CHANGED:
    - UPDATE orders SET status = 'FAILED_PRICE_CHANGED'
    - Hoàn số dư user (rollback reserve)
    - Trả về HTTP 409 cho Frontend
    - Frontend hiển thị: "Giá đã thay đổi, vui lòng xác nhận lại"

6C. VietShare trả HTTP 409 OUT_OF_STOCK:
    - UPDATE orders SET status = 'FAILED_OUT_OF_STOCK'
    - Hoàn số dư user
    - Trả về HTTP 409 cho Frontend
    - Frontend hiển thị: "Sản phẩm tạm hết hàng"

6D. Network Timeout / HTTP 429 / HTTP 5xx:
    - UPDATE orders SET status = 'PROCESSING_RETRY'
    - GIỮ NGUYÊN số dư đã reserve (chưa hoàn)
    - Đẩy vào Background Job queue để retry
    - Trả về HTTP 202 cho Frontend: "Đơn hàng đang được xử lý"
    → Xem Flow 2 (Retry)
```

### Ví dụ request/response

**Request:**
```http
POST /api/v1/orders
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "product_id": 42,
  "quantity": 1
}
```

**Response thành công (HTTP 200):**
```json
{
  "order_id": 1001,
  "order_no": "MDO-20260725-001",
  "status": "COMPLETED",
  "accounts": [
    {
      "type": "EMAIL_PASS",
      "data": "user@example.com|pass123"
    }
  ]
}
```

**Response giá thay đổi (HTTP 409):**
```json
{
  "error": "PRICE_CHANGED",
  "message": "Giá sản phẩm đã thay đổi. Giá mới: 18,000đ. Vui lòng xác nhận để tiếp tục.",
  "new_price": 18000.00
}
```

---

## Flow 2: Background Job Retry Đơn Hàng

### Điều kiện kích hoạt
- `orders.status = 'PROCESSING_RETRY'`
- Job chạy định kỳ (cron) kiểm tra các đơn đang chờ retry.

### Các bước xử lý

```
1. Job tìm tất cả orders WHERE status = 'PROCESSING_RETRY' AND updated_at < NOW() - interval

2. Với mỗi đơn:
   a. Lấy idempotency_key gốc (KHÔNG tạo key mới)
   b. Gọi VietShare API với idempotency_key gốc
   
3A. VietShare trả HTTP 200:
    - VietShare đã xử lý trước đó (idempotency) hoặc xử lý lần này
    - UPDATE orders SET status = 'COMPLETED'
    - INSERT delivered_accounts
    - Commit số dư
    
3B. VietShare trả HTTP 4xx (trừ 429):
    - UPDATE orders SET status = 'FAILED'
    - Hoàn số dư user
    - Alert Admin
    
3C. Vẫn timeout/429/5xx hoặc 409 REQUEST_IN_PROGRESS:
    - Nếu chưa chạy đủ 3 retry: cập nhật retry_attempt + next_retry_at và chờ backoff tiếp
    - Nếu retry thứ 3 vẫn fail: UPDATE orders SET status = 'FAILED', alert Admin
```

**Thời gian backoff (lần gọi đầu không tính là retry):**
```
Lần gọi đầu fail → Retry 1: chờ 5 giây
Retry 1 fail     → Retry 2: chờ 15 giây
Retry 2 fail     → Retry 3: chờ 30 giây
Retry 3 fail     → đánh dấu FAILED ngay
```

Nếu supplier trả `Retry-After` hợp lệ, dùng `min(60 giây, max(backoff, Retry-After))`. Job dựng lại request từ snapshot đã lưu và luôn dùng `idempotency_key` gốc.

> **Lưu ý quan trọng:** Nếu VietShare trả HTTP 200 sau lần retry (idempotent response), dữ liệu trả về vẫn là dữ liệu tài khoản thực — phải xử lý giống như lần đầu thành công.

---

## Flow 3: Sync Catalog Sản Phẩm từ VietShare (CatalogSyncService)

**Quyết định (Q-002, 2026-07-26):** Hướng A — sync tự động qua API, không phải Admin UI thủ công.

### Mục tiêu
Cập nhật `supplier_products.supply_price` và `is_active` theo dữ liệu thực tế của VietShare, đảm bảo `max_unit_price` khi đặt hàng luôn sát với giá hiện tại.

### Tần suất & trigger
- Job cron chạy mỗi **10–30 giây** (cấu hình qua `application.yml`).
- KHÔNG gọi VietShare API theo từng request của khách hàng.
- Kết quả sync lưu vào **Redis cache** — `VietShareConnector.fetchCatalog()` đọc từ cache, không gọi API trực tiếp.

### Các bước xử lý

```
1. CatalogSyncService trigger (cron, mỗi 10-30s)

2. Gọi VietShare API: GET /v1/products
   - Ký HMAC-SHA256 như mọi request khác
   - Không có Idempotency-Key (GET request, idempotent tự nhiên)

3. Với mỗi item trong response:
   a. Tìm supplier_products WHERE external_code = item.id AND supplier_code = mã supplier (VD: `VIETSHARE`)
   b. Nếu tìm thấy:
      - UPDATE supply_price = item.price
      - UPDATE is_active = (item.is_active AND item.stock > 0)
   c. Nếu KHÔNG tìm thấy → bỏ qua (sản phẩm VietShare chưa được map vào MDStore)

4. Ghi kết quả mới vào Redis cache (key: "catalog:vietshare", TTL: 60s)

5. Log: tổng số item sync được, số item is_active thay đổi
```

### Ví dụ: Phát hiện giá thay đổi

```
Trước sync: supplier_products.supply_price = 15,000đ
VietShare trả về: item.price = 18,000đ

→ UPDATE supplier_products SET supply_price = 18000 WHERE external_code = 'VS_NORDVPN_1M'
→ Lần đặt hàng tiếp theo: max_unit_price = 18,000đ (lấy từ cache mới)
→ Nếu user đang chờ với giá cũ → VietShare sẽ trả PRICE_CHANGED → xử lý theo Flow 1 nhánh 6B
```

### Ví dụ: Phát hiện hết hàng

```
VietShare trả về: item.stock = 0 hoặc item.is_active = false

→ UPDATE supplier_products SET is_active = false WHERE external_code = 'VS_NORDVPN_1M'
→ Trang sản phẩm ẩn item này (Frontend đọc is_active từ DB)
→ Nếu user đặt đơn trong khoảng lag vài giây → VietShare trả OUT_OF_STOCK → Flow 1 nhánh 6C
```

### Xử lý khi gọi API thất bại

```
HTTP 429 / 5xx từ VietShare khi sync catalog:
→ Log cảnh báo, GIỮ NGUYÊN data cũ trong cache (không xóa)
→ Job sẽ thử lại ở lần cron tiếp theo (10-30s sau)
→ Không alert Admin trừ khi fail liên tục > 5 phút
```


---

## Flow 4: Quản Lý Giá Bán Lẻ (Admin)

> ⚠️ Flow này chưa được implement. Chưa có yêu cầu rõ ràng về cách Admin cập nhật giá. Xem [`07-open-questions.md`](./07-open-questions.md) — Q-003.

---

## Flow 5: Xử Lý Khi VietShare Trả PRICE_CHANGED — User Confirm Giá Mới

### Điều kiện
- Đơn hàng đang ở `FAILED_PRICE_CHANGED`
- User chọn "Xác nhận giá mới và mua lại"

### Các bước

```
1. Frontend → POST /api/v1/orders/{order_id}/confirm-new-price
   Body: { "confirmed_price": 18000.00 }

2. Backend: Kiểm tra confirmed_price == giá hiện tại của supplier
   - Nếu giá lại thay đổi tiếp → trả về lỗi mới

3. Backend: Tạo đơn hàng MỚI (order_id mới, idempotency_key mới)
   - Đơn cũ giữ nguyên trạng thái FAILED_PRICE_CHANGED (audit trail)
   
4. Tiếp tục từ bước 5 của Flow 1
```

> **Không update đơn cũ** — tạo đơn mới để giữ audit trail. Đây là quyết định chưa được confirm rõ, xem [`07-open-questions.md`](./07-open-questions.md) — Q-004.
