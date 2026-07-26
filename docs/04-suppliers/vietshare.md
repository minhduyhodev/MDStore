# VietShare API — Tài Liệu Tích Hợp

> **Đọc toàn bộ file này trước khi implement bất cứ thứ gì liên quan đến VietShare.**
> Không tóm tắt lại — bản gốc giữ các chi tiết quan trọng như canonical string format.

---

## Thông Tin Chung

| Thông số | Giá trị |
|---|---|
| Base URL | `https://token.vietshare.site/v1` |
| Content-Type | `application/json` |
| Auth Headers | `X-Shop-API-ID`, `X-Timestamp`, `X-Nonce`, `X-Signature` (HMAC-SHA256) |
| Rate Limit | 60 requests / phút / IP |
| Idempotency | Header `Idempotency-Key` (UUID, hiệu lực 24h) |

---

## Xác Thực: HMAC-SHA256

### Canonical String (ký đúng thứ tự, không được đổi)

```
timestamp|nonce|METHOD|PATH_WITH_QUERY|sha256(raw_body)
```

| Field | Mô tả |
|---|---|
| `timestamp` | Unix Epoch milliseconds. Lệch > 5 phút → bị từ chối |
| `nonce` | UUID ngẫu nhiên, chống Replay Attack |
| `METHOD` | HTTP method (GET, POST, PUT, DELETE) — viết hoa |
| `PATH_WITH_QUERY` | Đường dẫn + query params. VD: `/v1/orders?type=email` |
| `sha256(raw_body)` | SHA-256 của raw request body. Nếu không có body → băm chuỗi rỗng `""` |

### Tạo chữ ký

```
signature = HMAC-SHA256(canonical_string, API_SECRET)
```

### Gửi trong Header

```http
X-Shop-API-ID: {API_KEY}
X-Timestamp: {timestamp}
X-Nonce: {nonce}
X-Signature: {signature}
```

> **⚠️ Chi tiết quan trọng:**
> - `PATH_WITH_QUERY` phải bao gồm toàn bộ query string theo thứ tự gốc.
> - `sha256(raw_body)` tính trên raw bytes, KHÔNG phải JSON sau khi parse.
> - Chữ ký phải được encode dưới dạng **hex lowercase**.

---

## API: Đặt Hàng (Create Order)

**Mô tả:** Gọi khi người dùng MDStore thanh toán thành công. MDStore mua tài khoản từ VietShare và nhận về dữ liệu để giao cho user.

### Request

```http
POST /v1/orders
Content-Type: application/json
X-Shop-API-ID: {API_KEY}
X-Timestamp: {timestamp}
X-Nonce: {nonce}
X-Signature: {signature}
Idempotency-Key: {uuid-của-đơn-hàng-nội-bộ}
```

```json
{
  "product_code": "VS_NORDVPN_1M",
  "quantity": 1,
  "max_unit_price": 15000.00,
  "coupon_code": "DISCOUNT10",
  "flash_sale_id": "FS_2026_07"
}
```

**Lưu ý về `max_unit_price`, `coupon_code`, `flash_sale_id`:**
- `max_unit_price`: Là mức giá tối đa MDStore chấp nhận mua.
- Nếu giá VietShare hiện tại **cao hơn** `max_unit_price` → VietShare trả HTTP 409 `PRICE_CHANGED`.
- `coupon_code`: Mã giảm giá áp dụng (nếu có).
- `flash_sale_id`: Gửi kèm nếu sản phẩm này thuộc chương trình flash sale (lấy từ catalog API) để đảm bảo an toàn nếu flash sale vừa kết thúc.

### Response — Thành công (HTTP 200)

```json
{
  "status": "success",
  "data": {
    "order_id": "VS-987654321",
    "product_code": "VS_NORDVPN_1M",
    "unit_price": 15000.00,
    "total_price": 15000.00,
    "delivered_accounts": [
      {
        "type": "EMAIL_PASS",
        "data": "user@example.com|pass123"
      }
    ]
  }
}
```

**Xử lý sau khi nhận HTTP 200:**
1. Update `orders.status` → `COMPLETED`
2. Insert vào `delivered_accounts` với `account_data` = giá trị trong `data.delivered_accounts[*].data`
3. Trả về dữ liệu tài khoản cho user

---

## Bảng Mã Lỗi

| HTTP Status | Code | Ý nghĩa | MDStore phải làm |
|---|---|---|---|
| `400` | `INVALID_REQUEST` | Payload sai format hoặc thiếu/sai Signature | Log lỗi, alert Developer. **Không retry.** |
| `401` | `UNAUTHORIZED` | Sai API_KEY hoặc Signature invalid | Ngừng gọi API, alert Admin ngay lập tức. **Không retry.** |
| `409` | `PRICE_CHANGED` | Giá VietShare > `max_unit_price` | Update `orders.status` → `FAILED_PRICE_CHANGED`. Hoàn tiền user hoặc yêu cầu confirm giá mới. **Không retry.** |
| `409` | `OUT_OF_STOCK` | Hết hàng | Update `orders.status` → `FAILED_OUT_OF_STOCK`. Hoàn tiền user. **Không retry.** |
| `409` | `REQUEST_IN_PROGRESS` | Có một request khác đang xử lý | **Bắt buộc giữ Idempotency-Key**, chờ theo header `Retry-After` rồi retry. |
| `429` | `TOO_MANY_REQUESTS` | Vượt 60 req/min | Update `orders.status` → `PROCESSING_RETRY`. Background job retry với Exponential Backoff. **Giữ nguyên Idempotency-Key.** |
| `500/503` | `SERVER_ERROR` | Lỗi phía VietShare | Tương tự 429. |
| Network Timeout | N/A | Không nhận được response | Tương tự 429 — không biết VietShare có nhận được không, **bắt buộc giữ Idempotency-Key khi retry.** |

---

## Quy Tắc Retry

```
Được phép retry: Network Timeout | HTTP 429 | HTTP 5xx | HTTP 409 REQUEST_IN_PROGRESS
KHÔNG retry:     HTTP 400 | HTTP 401 | HTTP 409 PRICE_CHANGED/OUT_OF_STOCK/không nhận diện
```

**Thời gian Exponential Backoff:**
```
Lần gọi đầu: gọi ngay, chưa tính là retry
Retry 1: chờ 5 giây
Retry 2: chờ 15 giây
Retry 3: chờ 30 giây
→ Retry 3 vẫn fail: cập nhật status = FAILED, alert Admin
```

Nếu response có `Retry-After` hợp lệ, delay là `min(60 giây, max(backoff, Retry-After))`.

**Bắt buộc:** Khi retry, giữ nguyên `Idempotency-Key` của lần gửi đầu tiên. Timestamp, nonce và HMAC signature được tạo mới cho từng HTTP request.

---

## Catalog Sản Phẩm (Lấy Danh Sách & Chi Tiết)

> Q-002 đã quyết định (2026-07-26): VietShare CÓ API catalog — dùng sync tự động.

### Lưu ý quan trọng về field `price`

> Field `price` trong mọi response catalog là **giá bán của VietShare đã gồm markup của họ** — không phải giá vốn gốc.  
> MDStore coi đây là **"giá nhập"**, rồi tự cộng markup riêng khi hiển thị cho khách cuối.  
> Không có endpoint nào trả về giá vốn thật của VietShare. **Không tìm thêm.**

### API 1 — Lấy toàn bộ danh sách (`GET /v1/products`)

Alias: `GET /v1/catalog` (cùng response format).

```http
GET /v1/products
X-Shop-API-ID: {API_KEY}
X-Timestamp: {timestamp}
X-Nonce: {nonce}
X-Signature: {signature}
```

**Response (HTTP 200):**
```json
{
  "data": [
    {
      "id": "VS_NORDVPN_1M",
      "name": "NordVPN 1 Tháng",
      "price": 15000.00,
      "stock": 142,
      "is_active": true,
      "flash_sale_id": "FS_2026_07"
    }
  ]
}
```

**Mapping sang MDStore:**
| Field VietShare | Field MDStore | Ghi chú |
|---|---|---|
| `id` | `supplier_products.external_code` | Dùng để đặt hàng (`product_code`) |
| `price` | `supplier_products.supply_price` | Giá nhập của MDStore — cộng markup khi bán ra |
| `stock` | Không lưu DB | Chỉ dùng để set `is_active = false` nếu `stock == 0` |
| `is_active` | `supplier_products.is_active` | AND với `stock > 0` |
| `flash_sale_id` | `supplier_products.flash_sale_id` | Truyền lại lúc đặt đơn nếu khác null |

---

### API 2 — Chi tiết 1 sản phẩm (`GET /v1/products/{id}`)

Alias: `GET /v1/stock/{id}` (cùng response format, dùng khi cần check stock nhanh 1 item).

```http
GET /v1/products/VS_NORDVPN_1M
X-Shop-API-ID: {API_KEY}
X-Timestamp: {timestamp}
X-Nonce: {nonce}
X-Signature: {signature}
```

**Response (HTTP 200):**
```json
{
  "data": {
    "id": "VS_NORDVPN_1M",
    "name": "NordVPN 1 Tháng",
    "price": 15000.00,
    "stock": 142,
    "is_active": true
  }
}
```

---

### Tần suất sync & chiến lược cache

- Tần suất sync & chiến lược cache:
  - **CatalogSyncService** chạy định kỳ **10–30 giây/lần** (cron job, không phải per-request).
  - Khi đặt đơn (`POST /v1/orders`): lấy `max_unit_price` và `flash_sale_id` từ **cache catalog gần nhất**.

---

## Các API Khác

### 1. Lấy số dư ví (`GET /v1/account`)
- Dùng cho `WalletService` để đối chiếu số dư hiện tại của MDStore trên VietShare.

### 2. Danh sách đơn hàng (`GET /v1/orders`) & Chi tiết đơn (`GET /v1/orders/{order_code}`)
- Dùng cho `OrderReconciliationService` để đối soát trạng thái đơn hàng khi xảy ra lỗi network hoặc timeout, đảm bảo đồng bộ trạng thái `COMPLETED` / `FAILED` nội bộ với VietShare.

## Biến Môi Trường Cần Cấu Hình

```env
VIETSHARE_API_URL=https://token.vietshare.site/v1
VIETSHARE_API_KEY=your_api_key_here
VIETSHARE_API_SECRET=your_secret_key_here
```
