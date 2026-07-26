# 10 — Cấu Trúc Backend: Thực Tế vs Lý Thuyết

> File này giải thích code backend **đang thực sự tồn tại** khớp với docs/02-decisions.md
> thế nào. Đọc file này khi muốn hiểu toàn cảnh mà không cần mở từng file một.

---

## 1. Cây thư mục thật (chụp ngày 2026-07-26)

```
Server/src/main/java/com/mdstore/
│
├─ MdstoreApplication.java              ← điểm khởi động của cả ứng dụng
│
├─ common/
│   └─ ApiResponse.java                 ← khuôn response chung gửi về frontend
│
└─ connector/                           ← toàn bộ logic giao tiếp với nhà cung cấp
    │
    ├─ SupplierConnector.java           ← bản hợp đồng (interface)
    ├─ SupplierProduct.java             ← kiểu dữ liệu: 1 sản phẩm
    ├─ OrderRequest.java                ← kiểu dữ liệu: yêu cầu đặt hàng
    ├─ OrderResult.java                 ← kiểu dữ liệu: kết quả đặt hàng
    ├─ SupplierException.java           ← kiểu lỗi riêng cho supplier
    │
    └─ vietshare/                       ← mọi thứ riêng của VietShare
        ├─ VietShareProperties.java     ← đọc cấu hình từ .env
        ├─ VietShareSigner.java         ← tính chữ ký HMAC-SHA256
        ├─ VietShareConnector.java      ← ký hợp đồng, gọi API thật
        └─ dto/
            ├─ VsProductListResponse.java   ← map JSON response từ API
            └─ VsProductItem.java           ← map 1 sản phẩm trong response
```

**Nhận xét:** Code hiện tại chỉ có tầng `connector/` — chưa có `catalog/`, `order/`,
`wallet/` hay REST controller nào cả. Đó là vì task hiện tại (xem PROGRESS.md)
đang ở giai đoạn viết connector trước, service/controller viết sau.

---

## 2. Vai trò từng file

| File | Package | Vai trò (1 câu) | Gọi / được gọi bởi |
|---|---|---|---|
| `MdstoreApplication.java` | `com.mdstore` | Điểm khởi động — Spring Boot đọc file này đầu tiên khi chạy | Không ai gọi, nó khởi động tất cả |
| `ApiResponse.java` | `com.mdstore.common` | Khuôn chuẩn cho mọi response gửi về frontend (`{success, data, error, meta}`) | Sẽ được gọi bởi mọi REST Controller (chưa viết) |
| `SupplierConnector.java` | `com.mdstore.connector` | **Bản hợp đồng** — ai muốn trở thành connector phải có đủ 3 hàm: `getSupplierCode()`, `fetchCatalog()`, `placeOrder()` | Được implement bởi `VietShareConnector`; sẽ được gọi bởi `CatalogSyncService`, `OrderOrchestrationService` (chưa viết) |
| `SupplierProduct.java` | `com.mdstore.connector` | Kiểu dữ liệu đầu ra của `fetchCatalog()` — đại diện 1 sản phẩm | Tạo ra trong `VietShareConnector`, trả về cho `CatalogSyncService` |
| `OrderRequest.java` | `com.mdstore.connector` | Kiểu dữ liệu đầu vào của `placeOrder()` — yêu cầu đặt 1 đơn hàng | Tạo ra trong `OrderOrchestrationService` (chưa viết), gửi vào `VietShareConnector` |
| `OrderResult.java` | `com.mdstore.connector` | Kiểu dữ liệu đầu ra của `placeOrder()` — kết quả đơn thành công | Trả về từ `VietShareConnector`, nhận bởi `OrderOrchestrationService` |
| `SupplierException.java` | `com.mdstore.connector` | Lỗi có nghĩa nghiệp vụ từ supplier (giá đổi, hết hàng, rate limit...) với phương thức `isRetryable()` để hỏi "lỗi này có nên thử lại không?" | Ném ra bởi `VietShareConnector`, bắt bởi `OrderOrchestrationService` |
| `VietShareProperties.java` | `com.mdstore.connector.vietshare` | Đọc các biến môi trường từ `.env` (api-url, api-key, api-secret...) vào 1 Java object | Được inject vào `VietShareSigner` và `VietShareConnector` khi khởi động |
| `VietShareSigner.java` | `com.mdstore.connector.vietshare` | Tính chữ ký HMAC-SHA256 và trả về 4 header cần gắn vào request | Được gọi bởi `VietShareConnector` trước mỗi lần gọi API |
| `VietShareConnector.java` | `com.mdstore.connector.vietshare` | Gọi API VietShare thật, ký chữ ký, map response về kiểu dữ liệu chung | Implement `SupplierConnector`; gọi `VietShareSigner`; gọi API VietShare qua `RestClient` |
| `VsProductListResponse.java` | `...vietshare.dto` | Map JSON nguyên văn trả về từ `GET /v1/products` vào Java object | Được dùng trong `VietShareConnector.fetchCatalog()` để parse response |
| `VsProductItem.java` | `...vietshare.dto` | Map 1 sản phẩm trong danh sách trả về từ VietShare | Được dùng trong `VsProductListResponse`, sau đó convert thành `SupplierProduct` |

---

## 3. Luồng chạy thật của fetchCatalog()

Lấy ví dụ cụ thể: `CatalogSyncService` (sẽ viết sau) gọi `fetchCatalog()` mỗi 30 giây.
Sau đây là từng bước đi của code:

```
[Khởi động app]
  → Spring đọc VIETSHARE_API_KEY, VIETSHARE_API_SECRET từ .env
  → Tạo VietShareProperties (object chứa config)
  → Tạo VietShareSigner (nhận config từ Properties)
  → Tạo VietShareConnector (nhận Properties + Signer)
     └─ tạo sẵn RestClient với baseUrl và timeout

[Sau 30 giây — CatalogSyncService gọi connector.fetchCatalog()]

Bước 1 — VietShareConnector.fetchCatalog() bắt đầu
  → Gọi signer.sign("GET", "/v1/products", null)

Bước 2 — VietShareSigner.sign() làm việc
  → Lấy timestamp hiện tại (milliseconds)
  → Tạo nonce ngẫu nhiên (UUID)
  → Tính SHA-256 của body rỗng (body = null → byte[0])
  → Nối chuỗi: "timestamp|nonce|GET|/v1/products|sha256_of_empty"
  → Tính HMAC-SHA256 của chuỗi đó với apiSecret làm key
  → Trả về SignedHeaders{xShopApiId, xTimestamp, xNonce, xSignature}

Bước 3 — VietShareConnector gắn headers và gửi request
  → restClient.get().uri("/v1/products")
             .header("X-Shop-API-ID", ...)
             .header("X-Timestamp", ...)
             .header("X-Nonce", ...)
             .header("X-Signature", ...)
             .retrieve()
             .body(VsProductListResponse.class)
                 ↑ Spring tự parse JSON từ VietShare → VsProductListResponse

Bước 4 — VietShareConnector nhận response, convert kiểu dữ liệu
  → response.data() → List<VsProductItem>  (kiểu riêng của VietShare)
  → .map(item → new SupplierProduct(...))   (convert sang kiểu chung)
     • item.id()       → externalCode
     • item.name()     → name
     • item.price()    → supplyPrice
     • item.isActive() AND item.stock() > 0 → isActive
  → Trả về List<SupplierProduct>             (kiểu chung, không biết là VietShare)

Bước 5 — CatalogSyncService nhận List<SupplierProduct>
  → Lưu vào DB bảng supplier_products (chưa implement)
  → Lưu cache Redis (chưa implement)

[Nếu có lỗi ở bước 3]
  → HTTP 401 → throw SupplierException(UNAUTHORIZED)
  → HTTP 429 → throw SupplierException(RATE_LIMITED)
  → HTTP 5xx → throw SupplierException(SERVER_ERROR)
  → Timeout  → throw SupplierException(NETWORK_TIMEOUT)
  → CatalogSyncService bắt exception, quyết định giữ cache cũ hay alert
```

**Điểm quan trọng nhất:** Bước 4 là nơi "dịch" từ ngôn ngữ VietShare sang ngôn ngữ
chung của MDStore. `CatalogSyncService` chỉ biết `SupplierProduct` — không biết
cấu trúc JSON của VietShare trông như thế nào.

---

## 4. SupplierConnector là gì — giải thích bằng ví dụ thực tế

### Hãy tưởng tượng thế này:

MDStore muốn nhập hàng từ nhiều kho khác nhau. Mỗi kho có cách làm việc riêng —
kho A cần fax đơn hàng, kho B dùng app, kho C gửi email. Nhưng MDStore muốn
chỉ cần 1 người quản lý chung, không cần biết chi tiết từng kho.

→ MDStore viết ra **1 bản hợp đồng** (interface `SupplierConnector`):

> *"Ai muốn là kho cung cấp cho MDStore phải làm được 3 việc:*
> *1. Cho biết mã kho của bạn là gì (`getSupplierCode`)*
> *2. Gửi danh sách hàng hiện có (`fetchCatalog`)*
> *3. Nhận đơn đặt hàng và xác nhận giao được không (`placeOrder`)*"*

`VietShareConnector` chính là **kho đầu tiên ký hợp đồng đó**. Nó làm đủ 3 việc
trên — theo cách riêng của VietShare (HMAC-SHA256, REST API, JSON format...).

### Nếu sau này thêm nhà cung cấp mới — "ShopB"

Bạn chỉ cần làm **3 việc, không sửa gì cũ**:

1. **Tạo file mới** `Server/src/main/java/com/mdstore/connector/shopb/ShopBConnector.java`
   ```java
   @Component
   public class ShopBConnector implements SupplierConnector {
       public String getSupplierCode() { return "SHOPB"; }
       public List<SupplierProduct> fetchCatalog() { /* gọi API ShopB */ }
       public OrderResult placeOrder(OrderRequest req) { /* gọi API ShopB */ }
   }
   ```

2. **Tạo file config** `ShopBProperties.java` (đọc `SHOPB_API_KEY` từ `.env`)

3. **Thêm biến môi trường** `SHOPB_API_KEY`, `SHOPB_API_SECRET` vào `.env`

**Không cần sửa bất kỳ file nào trong số này:**
- `SupplierConnector.java` — hợp đồng không thay đổi
- `CatalogSyncService.java` — không biết là VietShare hay ShopB, chỉ gọi `fetchCatalog()`
- `OrderOrchestrationService.java` — tương tự, chỉ gọi `placeOrder()`
- `ApiResponse.java` — response format không liên quan
- `VietShareConnector.java` — code VietShare không bị đụng tới

Đây là lý do tại sao ADR-004 gọi đây là "Adapter Pattern" — bạn thêm một adapter
mới mà không cần mở nội thất ra để lắp lại dây điện.

---

## 5. Phân biệt 3 loại "model" trong connector package

Người mới dễ nhầm giữa các kiểu dữ liệu. Bảng này giải thích sự khác nhau:

| Kiểu | File | Ai tạo ra | Ai đọc | Giải thích |
|---|---|---|---|---|
| **DTO** (Data Transfer Object) | `VsProductItem`, `VsProductListResponse` | Jackson (thư viện parse JSON) | Chỉ `VietShareConnector` | Ánh xạ JSON nguyên văn từ VietShare. Tên field phải khớp field trong JSON (hoặc dùng `@JsonProperty`). Không dùng ngoài package vietshare. |
| **Domain model** (chung) | `SupplierProduct`, `OrderRequest`, `OrderResult` | `VietShareConnector` (sau khi convert từ DTO) | Mọi service trong MDStore | Ngôn ngữ chung của MDStore. Không biết là VietShare hay ShopB. |
| **Exception** | `SupplierException` | `VietShareConnector` (khi có lỗi) | `OrderOrchestrationService` | Lỗi có nghĩa nghiệp vụ, phân loại rõ ràng để tầng service biết phải làm gì tiếp. |

---

## 6. Phần chưa có (sẽ viết theo thứ tự Backlog)

Nhìn vào cây thư mục thật, rõ ràng còn thiếu nhiều:

| Thiếu | Trong PROGRESS.md Backlog | Chú thích |
|---|---|---|
| `placeOrder()` trong VietShareConnector | Current focus | Đang làm |
| `ConnectorRegistry` | Backlog [M] | Quản lý danh sách các connector |
| `CatalogSyncService` | Backlog [M] | Gọi `fetchCatalog()` mỗi 30 giây, lưu Redis |
| `OrderOrchestrationService` | Backlog [M] | Gọi `placeOrder()`, xử lý PRICE_CHANGED, retry |
| REST Controller (API endpoint) | Chưa có trong Backlog | Cần thêm để frontend gọi được |
| DB migration (Flyway) | Backlog [S] L.4 | Schema SQL cho `orders`, `supplier_products`... |

---

*Cập nhật lần cuối: 2026-07-26*
