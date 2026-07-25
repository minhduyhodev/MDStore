# 06 — Glossary (Thuật Ngữ Dự Án)

> Đọc khi gặp từ chưa chắc chắn về nghĩa trong ngữ cảnh MDStore.
> Các từ này có thể khác nghĩa thông thường hoặc có quy ước riêng trong dự án.

---

## Thuật ngữ theo domain

### Supply chain

| Thuật ngữ | Nghĩa trong MDStore |
|---|---|
| **Supplier** | Nhà cung cấp tài khoản số sỉ (VD: VietShare). Không phải nhà cung cấp dịch vụ cloud. |
| **Product** | Sản phẩm nội bộ của MDStore — thường là 1 loại tài khoản (VD: "NordVPN 1 tháng"). Không phải `supplier_product`. |
| **Supplier Product** | Ánh xạ giữa Product MDStore và mã sản phẩm bên Supplier (VD: `VS_NORDVPN_1M`). Có thể 1 Product map tới nhiều Supplier. |
| **External Code** | Mã sản phẩm theo định nghĩa của Supplier (VD: `VS_NORDVPN_1M`). Không được tự đặt — phải lấy từ tài liệu supplier. |
| **Supply Price** | Giá mua sỉ từ Supplier, lưu trong `supplier_products.supply_price`. Không phải giá bán cho user. |
| **Delivered Account** | Dữ liệu tài khoản số đã giao cho user (Token, Key, Email\|Pass...). Lưu trong `delivered_accounts.account_data`. |

### Order states

| Thuật ngữ | Nghĩa |
|---|---|
| **PENDING** | Đơn đã tạo trong DB MDStore, chưa gửi đến Supplier. |
| **COMPLETED** | Supplier xác nhận thành công, tài khoản đã được giao cho user. |
| **PROCESSING_RETRY** | Gọi Supplier thất bại tạm thời (timeout/429/5xx), đang chờ Background Job retry. |
| **FAILED_PRICE_CHANGED** | Supplier từ chối vì giá thay đổi. User cần xác nhận giá mới. |
| **FAILED_OUT_OF_STOCK** | Supplier báo hết hàng. |
| **FAILED** | Thất bại không thể recover (sau hết lần retry, hoặc lỗi logic). |

### Technical

| Thuật ngữ | Nghĩa trong MDStore |
|---|---|
| **Idempotency-Key** | UUID gắn với mỗi đơn hàng MDStore, gửi kèm lên Supplier để chống trùng đơn. Giữ nguyên khi retry — KHÔNG tạo mới. |
| **Canonical String** | Chuỗi được dùng để ký HMAC-SHA256 khi gọi VietShare. Format bắt buộc: `timestamp\|nonce\|METHOD\|PATH_WITH_QUERY\|sha256(raw_body)`. |
| **Logical FK** | Cột tham chiếu sang bảng khác nhưng KHÔNG có DB-level FOREIGN KEY constraint. Validation được thực hiện ở Service layer. |
| **Adapter** | Class Java implement `SupplierAdapter` interface cho một nhà cung cấp cụ thể. Mỗi Supplier có 1 Adapter riêng. |
| **Virtual Thread** | Thread của Java 21 (Project Loom), không phải OS thread. Được dùng để xử lý I/O bound calls (gọi API Supplier). |
| **Backoff** | Chiến lược chờ giữa các lần retry: 5s → 15s → 30s. Viết tắt của Exponential Backoff. |

### Viết tắt hay dùng

| Viết tắt | Đầy đủ |
|---|---|
| VS | VietShare |
| MDO | MDStore Order (prefix của `order_no`) |
| FK | Foreign Key |
| DoD | Definition of Done (trong ROADMAP) |
| ADR | Architecture Decision Record (trong 02-decisions.md) |

---

## Thuật ngữ KHÔNG dùng trong codebase

Tránh những cách gọi dễ gây nhầm lẫn:

| Đừng dùng | Dùng thay bằng | Lý do |
|---|---|---|
| "supplier foreign key" | "logical FK" | Dự án không có FK thật |
| "product của VietShare" | "supplier product" hoặc "external product" | Product là khái niệm nội bộ MDStore |
| "hoàn tiền" | "hoàn số dư" | Hệ thống dùng ví nội bộ, không phải payment gateway |
| "cancel order" | "failed order" | Đơn không bị cancel chủ động — chỉ fail |
