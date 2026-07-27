# PROGRESS.md — Trạng thái công việc (agent đọc mục "Current focus" trước tiên)

> Quy tắc đọc: agent CHỈ cần đọc mục "Current focus" để biết làm gì tiếp theo.
> Mục "Done" chỉ đọc khi cần tra lại quyết định cũ, không cần đọc để resume task.
> Sau khi hoàn thành 1 task, agent tự cập nhật file này rồi mới báo cáo cho người dùng.
>
> MỌI task khi được nhận vào "Current focus" PHẢI triển khai đủ 6 giai đoạn theo
> đúng thứ tự bên dưới (mục "Khung 6 giai đoạn"). Không được nhảy thẳng vào code
> mà bỏ qua Plan, không được báo "xong" mà bỏ qua Test hoặc Self-review.

---

## Khung 6 giai đoạn — áp dụng cho MỌI task (agent copy khung này khi nhận task mới)

```
### Task: [tên task, lấy từ Backlog]

**Trạng thái**: [Plan / Coding / Testing / Review / Docs / Done] — bước hiện tại: [...]

**1. Plan (phân tích + lên kế hoạch)**
- [ ] Đọc các file liên quan (ghi rõ file nào): ...
- [ ] Xác định phạm vi: file/class nào sẽ tạo mới, file nào sẽ sửa
- [ ] Nếu có điểm mơ hồ ảnh hưởh DB/tiền/bảo mật → dừng, hỏi theo giao thức CLAUDE.md mục 6
- [ ] Chia nhỏ task thành các bước code cụ thể (liệt kê ra đây)

**2. Code**
- [ ] Bước 2.1: ...
- [ ] Bước 2.2: ...
  (mỗi bước nên nhỏ đến mức làm xong là commit được ngay)

**3. Test**
- [ ] Viết unit test cho logic mới/thay đổi
- [ ] Viết integration test nếu có gọi API/DB thật (VD: WireMock cho supplier API)
- [ ] Chạy toàn bộ test suite (không chỉ test mới viết) — ghi lại kết quả: [x/y pass]
- [ ] Nếu có test fail → quay lại bước 2, KHÔNG được bỏ qua hoặc comment out test

**4. Self-review (agent tự soát trước khi báo cáo)**
- [ ] Đọc lại code vừa viết như người review, không phải người viết
- [ ] Không còn TODO/FIXME/console.log/debug code sót lại
- [ ] Không có secret/credential hardcode trong code
- [ ] Đặt tên biến/hàm nhất quán với convention đã có trong dự án
- [ ] Kiểm tra lại có phá vỡ quy tắc nào trong CLAUDE.md không (VD: lỡ thêm FK,
      lỡ tạo file tài liệu trùng)

**5. Cập nhật tài liệu (nếu có ảnh hưởng)**
- [ ] Nếu đổi schema DB → cập nhật docs/03-db-schema.md
- [ ] Nếu đổi/thêm quyết định kiến trúc → cập nhật docs/02-decisions.md
- [ ] Nếu phát hiện tài liệu supplier sai/thiếu → cập nhật docs/04-suppliers/{tên}.md
- [ ] Nếu không có gì cần cập nhật → ghi rõ "Không cần cập nhật docs" (không được im lặng bỏ qua bước này)

**6. Done — chỉ tick khi 1-5 đã xong hết**
- [ ] Di chuyển task xuống mục "Done" (1 dòng tóm tắt)
- [ ] Điền "Bước tiếp theo cụ thể" cho task mới lấy từ Backlog vào Current focus
- [ ] Gửi báo cáo cho người dùng theo đúng mẫu cuối file
```

---

### Task: [S] FE-13 — Chuẩn hóa loading, empty và error states

**Trạng thái**: Plan — bước 1/6

**1. Plan (phân tích + lên kế hoạch)**
- [ ] Đọc các file liên quan: UI components hiện tại.
- [ ] Xác định phạm vi: Tạo các component tái sử dụng (LoadingState, ErrorState, EmptyState).
- [ ] Chia nhỏ task: (1) tạo các base component, (2) thay thế vào Catalog, Orders, Wallet, Supplier Admin.

**2. Code**
- [ ] Bước 2.1: Tạo `LoadingState.js`, `ErrorState.js`, `EmptyState.js` trong `src/components/ui/`.
- [ ] Bước 2.2: Thay thế logic hardcode loading/error ở trang Catalog và Catalog Detail.
- [ ] Bước 2.3: Thay thế ở trang Orders, Wallet, Admin Suppliers.

**3. Test**
- [ ] Test lại các state trên các trang (bằng mock).
- [ ] Chạy lint/tests.

**4. Self-review (agent tự soát trước khi báo cáo)**
- [ ] Đảm bảo UI nhất quán, chuẩn Design System.
- [ ] Không còn code crash trang khi lỗi.

**5. Cập nhật tài liệu (nếu có ảnh hưởng)**
- [ ] Không cần.

**6. Done — chỉ tick khi 1-5 đã xong hết**
- [ ] Di chuyển task xuống mục "Done".

**Bước tiếp theo cụ thể**: Bắt đầu tạo thư mục `src/components/ui` và các base components.

---

## Backlog (chưa làm, thứ tự ưu tiên từ trên xuống)

> **Quy ước độ phức tạp:**
> - `[S]` = xong trong 1 lần code liền mạch (1 vòng Plan→Test)
> - `[M]` = cần vài vòng Plan→Test lặp lại
> - `[L]` = quá to, **PHẢI tách nhỏ hơn nữa trước khi bắt đầu**, không được giữ nguyên

- [ ] [S] L.1: Confirm schema `supplier_products` (đã xong một phần, cần review thêm field)
- [ ] [M] L.2: Thiết kế schema `orders` ✅ (Đã hoàn thành cơ bản qua Routing Logic)
- [ ] [S] WalletService: Đọc balance qua GET /v1/account từ VietShare (Task mới bổ sung)
- [ ] [M] OrderReconciliationService: Đối soát đơn qua GET /v1/orders từ VietShare (Task mới bổ sung)
- [ ] [S] L.4: Viết DDL hoàn chỉnh + cập nhật `03-db-schema.md` (làm sau L.1 và L.2 xong)
- [ ] [S] Viết Flyway migration script từ DDL đã thiết kế ở L.4
- [ ] [M] L.3: Thiết kế schema `users` + balance ← **block bởi Q-005** (ví nội bộ vs payment gateway chưa quyết định)


---

## Backlog Frontend (chưa làm, thứ tự ưu tiên từ trên xuống)

### Luồng MVP: Catalog → Đặt đơn → Nhận tài khoản

- [x] [M] **Đã hoàn thành: Trang chi tiết sản phẩm** (`catalog/[id]`) — Hiển thị UI sản phẩm, selector, liên kết từ catalog, các state.
- [x] [S] **Đã hoàn thành: Chuẩn hóa API client theo domain** (FE-11) — Tách API Catalog/Order/Wallet/Supplier trên nền `apiFetch`; thống nhất xử lý response.
- [ ] [S] **FE-02: Kết nối catalog với API thật** (`/catalog`) — thay `MOCK_PRODUCTS` bằng `GET /api/catalog`; map DTO sang UI, có retry/empty/error/image fallback, tìm kiếm theo tên và lọc còn hàng/hết hàng. **Blocked:** cần backend hoàn thành `GET /api/catalog`.
- [ ] [M] **FE-03: Xác nhận và tạo đơn** — dialog/trang xác nhận sản phẩm, số lượng, đơn giá, tổng tiền và số dư; chống double-submit; gọi `POST /api/orders`; xử lý 200 success, 202 retry, 409 giá thay đổi và các lỗi supplier. Không optimistic-update số dư trước khi backend xác nhận.
- [ ] [M] **FE-04: Xác nhận giá mới** — tại trang chi tiết đơn hoặc dialog, hiển thị giá cũ/mới/chênh lệch; cho phép đồng ý giá mới hoặc hủy; refresh trạng thái đơn. **Blocked:** cần API contract backend cho thao tác xác nhận/hủy khi `PRICE_CHANGED`.
- [ ] [S] **FE-05: Kết nối danh sách đơn với API** (`/orders`) — thay `MOCK_ORDERS` bằng `GET /api/orders`; tính lại số liệu tổng quan, lọc theo status, có empty/error states và điều hướng theo ID thật. **Blocked:** cần backend hoàn thành `GET /api/orders`.
- [ ] [M] **FE-06: Kết nối chi tiết đơn + polling retry** (`/orders/[id]`) — dùng `GET /api/orders/{id}`; hiển thị trạng thái, giá/số lượng/tổng, account đã giao; polling 5–10 giây cho `PROCESSING_RETRY` và cleanup khi rời trang; chỉ hiển thị/copy credentials khi đơn completed. **Blocked:** cần backend hoàn thành `GET /api/orders/{id}`.

### Ví điện tử và thanh toán

- [ ] [S] **FE-12: Đồng bộ trạng thái mua hàng** — rà soát `useStore`; bỏ logic demo tự trừ ví, chỉ cập nhật sau response backend/refetch thành công; chống race condition và double-click.
- [ ] [S] **FE-07: Kết nối số dư ví** (`/wallet`) — thay `MOCK_WALLET` bằng `GET /api/wallet`, hỗ trợ loading/error/retry/refresh và đồng bộ số dư với luồng mua. **Blocked:** `GET /api/wallet` và mô hình wallet chưa chốt.
- [ ] [M] **FE-08: Lịch sử giao dịch ví** — thay `MOCK_TRANSACTIONS`, có phân trang/xem thêm, filter loại giao dịch, empty/error states, hiển thị tiền và thời gian Việt Nam. **Blocked:** cần schema/service/API wallet transaction.
- [ ] [L] **FE-09: Nạp tiền và liên kết thanh toán** — trước khi code phải tách thành: chọn payment gateway → tạo yêu cầu nạp → QR/redirect/status page → backend webhook → refresh số dư. **Blocked:** Q-005 và quyết định payment gateway.

### Admin, chất lượng và các task bị block

- [ ] [M] **FE-10: Kết nối Supplier Admin với API** (`/admin/suppliers`) — thay mock bằng `GET /api/admin/suppliers`, kết nối health check/kill switch khi API có sẵn, xác nhận thao tác phá hủy, không lộ API key/secret/signature. **Blocked:** cần API admin thao tác thật và authentication/authorization.
- [ ] [M] **FE-15: Trang Admin Dashboard Tổng Quan** (`/admin`) — Thống kê doanh thu, số lượng đơn hàng, lợi nhuận, hiển thị các cảnh báo hệ thống nhanh.
- [ ] [M] **FE-16: Trang Admin Quản lý Đơn Hàng** (`/admin/orders`) — Xem toàn bộ đơn hàng của mọi người dùng để support, có filter theo ngày, status, nhà cung cấp, và user.
- [ ] [M] **FE-17: Trang Admin Quản lý Người Dùng** (`/admin/users`) — Hiển thị danh sách khách hàng, xem số dư, lịch sử giao dịch của họ, và chức năng cộng/trừ tiền thủ công. **Blocked:** chờ backend schema users.
- [ ] [S] **FE-18: Trang Hỗ Trợ / FAQ** (`/support`) — Hiển thị chính sách bảo hành, hướng dẫn nạp tiền, và các câu hỏi thường gặp (UI tĩnh kết hợp thiết kế đẹp mắt).
- [ ] [S] **FE-13: Chuẩn hóa loading, empty và error states** — **Current focus**. Tạo component tái sử dụng và áp dụng cho Catalog, Orders, Order Detail, Wallet, Supplier Admin; không để trang crash hoặc chỉ log console khi API lỗi.
- [ ] [M] **FE-14: Test frontend luồng cốt lõi** — test catalog (loading/data/error/empty), hết hàng/thiếu số dư, single submit, 202 retry, 409 price changed và bảo mật hiển thị delivered account. Chạy được độc lập bằng mock API.
- [x] [S] **Đã hoàn thành: Trang chi tiết đơn hàng** (`orders/[id]`) — UI core flow, hiển thị thông tin tài khoản và xử lý `PRICE_CHANGED`; còn cần FE-06 để kết nối API thật.
- [x] [M] **Đã hoàn thành: Trang quản lý Supplier Admin** (`admin/suppliers`) — UI quản lý nguồn cung; còn cần FE-10 để kết nối API/auth thật.

**Các trang đang bị block (Chờ Backend / Quyết định):**

- [ ] Trang Đăng nhập / Hồ sơ cá nhân (Auth/Profile) — Blocked bởi Q-005 và backend schema `users`.
- [ ] Trang Admin Pricing — Blocked bởi Q-003.
- [ ] Trang Order Reconciliation (Đối soát) — Blocked bởi task Backend tương ứng.

---

## Done (chỉ 1 dòng/task, chi tiết xem git log)

- [x] 2026-07-27 — Hoàn thiện FE-11: Chuẩn hóa API client theo domain (Tạo thư mục services, cải tiến xử lý lỗi apiFetch).
- [x] 2026-07-27 — Hoàn thiện FE-01: Trang chi tiết sản phẩm (`catalog/[id]`) với layout responsive, quantity selector, mock data và các state tải dữ liệu. Cập nhật list catalog để điều hướng.
- [x] 2026-07-26 — Hoàn thiện UI Trang Quản lý Supplier Admin (admin/suppliers): bảng mật độ cao, kill switch có xác nhận modal, health check có loading, không lộ API key. Cập nhật Sidebar sang theme Emerald.
- [x] 2026-07-26 — Hoàn thiện OrderOrchestrationService retry/backoff: persisted state machine, 3 retry 5s/15s/30s, giữ Idempotency-Key, Retry-After tối đa 60s, worker claim/lease, HTTP 202; toàn bộ 51/51 test pass. Không thêm Flyway migration theo quyết định owner.
- [x] 2026-07-26 — Thiết kế UI Trang Chi tiết Đơn hàng (orders/[id]) với layout 2 cột, logic hiển thị account và Form xử lý giá đổi (PRICE_CHANGED) theo đúng Design System. Cập nhật routing trong orders list.

- [x] 2026-07-26 — Hoàn thiện PRICE_CHANGED flow: lưu `FAILED_PRICE_CHANGED`, trả error envelope `ORDER_PRICE_CHANGED` với HTTP 409, kết nối POST order vào orchestration service; toàn bộ 36/36 test pass.
- [x] 2026-07-26 — Dọn dẹp + sửa lỗi header tài liệu (xem báo cáo đầy đủ bên dưới)
- [x] 2026-07-26 — Bảo mật secrets: tạo .gitignore, .env.example, ẩn toàn bộ URL/credentials vào env var, sửa application.yml bỏ fallback hardcode, sửa README.md (task chèn ngang)
- [x] 2026-07-26 — Setup base FE React: Next.js App Router + JavaScript + Tailwind CSS tại client/, layout.js + 3 trang (catalog/orders/wallet) với "use client", api.js xử lý envelope ADR-007, tạo docs/09-frontend.md (task chèn ngang)
- [x] 2026-07-26 — Viết VietShareConnector (implement SupplierConnector): fetchCatalog, placeOrder, HMAC signer, xử lý response envelope và mock server integration test.
- [x] 2026-07-26 — Tổ chức lại tầng RESTful API theo domain: tạo cấu trúc web/ cho catalog, order, wallet, supplier với các controller stub, thêm GlobalExceptionHandler và ADR-009.
- [x] 2026-07-26 — Viết ConnectorRegistry: Quản lý danh sách các SupplierConnector và tự động đăng ký qua Spring injection.
- [x] 2026-07-26 — Viết CatalogSyncService: Tạo job đồng bộ 10-30s/lần, cập nhật entity SupplierProductEntity vào DB, và ghi Redis cache TTL 60s. Thêm test bằng Mockito.
- [x] 2026-07-26 — Viết OrderOrchestrationService: Routing logic (chọn supplier theo giá rẻ nhất), tạo OrderEntity lưu vào DB, gọi placeOrder lên Connector và test bằng Mockito. Cấu trúc bảng `orders` (L.2) đã được định hình.
- [x] 2026-07-26 — Sửa 5 thiếu sót của VietShare API: thêm `REQUEST_IN_PROGRESS`, bổ sung `couponCode` và `flashSaleId` vào luồng đặt đơn, cập nhật tài liệu `vietshare.md`.
- [x] 2026-07-26 — Nâng cấp toàn diện giao diện Frontend (Premium UI Revamp): Thêm Tailwind CSS, Framer Motion, Lucide React; Thiết kế lại Layout, Catalog (Grid), Orders (Status badges), Wallet (Credit card style) đẹp mắt (task chèn ngang).
---

## Mẫu báo cáo agent phải gửi khi hoàn thành 1 task

```
Đã hoàn thành: [tên task]
- Test: [số lượng test, kết quả chạy — VD: 12/12 pass]
- File đã sửa/tạo: [danh sách]
- Đã cập nhật PROGRESS.md: chuyển task sang Done, cập nhật Current focus sang task tiếp theo
- Task tiếp theo trong backlog: [tên]
```

Agent KHÔNG được báo "đã xong" nếu test chưa chạy hoặc chưa pass 100%.

---

## Báo Cáo: Dọn Dẹp + Sửa Lỗi Header Tài Liệu (2026-07-26)

**Giai đoạn 1 — Đọc CLAUDE.md:** Xác nhận rule "1 chủ đề = 1 file", danh sách file chính thức.

**Giai đoạn 2 — So sánh từng cặp:** Phát hiện 5 cặp trùng lặp và 1 lỗi nghiêm trọng:
- Header VietShare SAI ở cả 2 bản cũ (`X-Api-*`). Tên đúng: `X-Shop-API-ID`, `X-Timestamp`, `X-Nonce`, `X-Signature`.

**Giai đoạn 3 — Xác nhận với Owner:** Owner xác nhận header đúng + chọn phương án A cho Quick Start.

**Giai đoạn 4 — Thực thi:**
- Sửa header trong `04-suppliers/vietshare.md` (3 chỗ).
- Gộp Mermaid diagrams + Virtual Threads explanation → `02-decisions.md` (Phụ lục A/B/C).
- Xóa `ARCHITECTURE.md`, `DATABASE.md`, `API_INTEGRATION.md`, `docs/README.md`, `ROADMAP.md`.
- Viết lại root `README.md`: 3-5 dòng intro + Quick Start + links.
- Gộp directory structure từ `docs/README.md` → `01-overview.md`.
- Tạo `08-roadmap.md` từ ROADMAP.md, cập nhật tiến độ Phase 1 theo PROGRESS.md.
- Cập nhật `CLAUDE.md` mục 7: thêm danh sách file chính thức + `08-roadmap.md` vào bảng đọc.

**Giai đoạn 5 — Self-review:**
- Tìm kiếm toàn bộ codebase với ripgrep: `X-Api-Signature`, `X-Api-Key`, `X-Api-Timestamp`, `X-Api-Nonce`.
- Kết quả: **Không có header sai trong code Java/frontend**. Lỗi chỉ tồn tại trong docs cũ đã bị xóa.
- `04-suppliers/vietshare.md` đã dùng đúng `X-Shop-API-ID`, `X-Timestamp`, `X-Nonce`, `X-Signature`.
- `02-decisions.md` ADR-003 mô tả canonical string format (không nhắc tên header cụ thể — đúng).

**Giai đoạn 6 — Cấu trúc cuối cùng:** (xem bên dưới)
