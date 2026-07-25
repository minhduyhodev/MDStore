# _template.md — Khung Tài Liệu Nhà Cung Cấp Mới

> Copy file này, đổi tên thành `{tên-supplier}.md`, điền đầy đủ trước khi implement.
> Không implement khi còn ô `TODO`.

---

## Thông Tin Chung

| Thông số | Giá trị |
|---|---|
| Tên nhà cung cấp | TODO |
| Base URL | `TODO` |
| Content-Type | `application/json` |
| Auth method | TODO (VD: HMAC-SHA256, Bearer Token, API Key in Header) |
| Rate Limit | TODO (VD: 60 req/phút/IP) |
| Idempotency | TODO (có hỗ trợ không? Nếu có, cách dùng như thế nào?) |
| Môi trường test | TODO (URL sandbox nếu có) |

---

## Xác Thực (Authentication)

> Điền chi tiết cách tạo chữ ký / token. Phải đủ để implement mà không cần hỏi lại.

TODO: Mô tả algorithm, format, header names, canonical string (nếu có).

### Ví dụ Request Header

```http
TODO
```

---

## API: Đặt Hàng

### Request

```http
TODO: HTTP Method + Endpoint
```

```json
{
  "TODO": "payload"
}
```

### Response — Thành công

```json
{
  "TODO": "response"
}
```

### Mapping về MDStore internal format

| Field từ Supplier | Field MDStore | Ghi chú |
|---|---|---|
| TODO | `delivered_accounts.account_data` | TODO |

---

## Bảng Mã Lỗi

| HTTP Status | Code | Ý nghĩa | MDStore phải làm |
|---|---|---|---|
| TODO | TODO | TODO | TODO |

---

## Quy Tắc Retry

TODO: Lỗi nào được retry? Lỗi nào không?

---

## Adapter Class

Khi implement, tạo class tại:
```
mdstore-backend/src/main/java/com/mdstore/adapter/{TenSupplier}Adapter.java
```

Implement interface `SupplierAdapter` — xem [`02-decisions.md` ADR-004](../02-decisions.md#adr-004-adapter-pattern-cho-tích-hợp-nhà-cung-cấp).

---

## Biến Môi Trường Cần Thêm

```env
{SUPPLIER_NAME}_API_URL=TODO
{SUPPLIER_NAME}_API_KEY=TODO
{SUPPLIER_NAME}_API_SECRET=TODO
```

Sau khi thêm biến, cập nhật `suppliers` table trong DB với `code = '{supplier_code}'`.
