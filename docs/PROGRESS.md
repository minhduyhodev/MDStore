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

## Current focus (đang làm — LUÔN chỉ có 1 task ở đây)

### Task: Viết VietShareConnector (implement SupplierConnector)

**Trạng thái**: Coding — bước 2/6 (Code)

**1. Plan** ✅ đã xong
- [x] Đọc docs/04-suppliers/vietshare.md (catalog API đã được document sau Q-002 resolved)
- [x] Đọc docs/02-decisions.md (ADR-002 Virtual Threads, ADR-003 HMAC, ADR-004 Connector pattern, ADR-007 envelope)
- [x] Phạm vi: tạo mới toàn bộ project Spring Boot tại `Server/`, sau đó viết `VietShareSigner` và `VietShareConnector.fetchCatalog()`
- [x] Không có điểm mơ hồ nào cần hỏi (Q-002 đã giải quyết)
- [x] Chia bước: pom.xml + cấu hình → SupplierConnector interface → VietShareSigner → fetchCatalog() → tests

**2. Code** ✅ fetchCatalog() xong — placeOrder() còn lại
- [x] Khởi tạo project Spring Boot tại `Server/` (pom.xml, MdstoreApplication, application.yml)
- [x] `SupplierConnector` interface + model `SupplierProduct`, `OrderRequest`, `OrderResult`
- [x] `VietShareProperties` (@ConfigurationProperties)
- [x] `VietShareSigner` — canonical string + HMAC-SHA256
- [x] `VietShareConnector.fetchCatalog()` — gọi GET /v1/products, map sang `List<SupplierProduct>`
- [ ] `VietShareConnector.placeOrder()` — xử lý mã lỗi 402/409/429/503

**3. Test**
- [x] Unit test `VietShareSigner` — **8/8 pass** (format + deterministic HMAC vector)
- [x] Integration test `fetchCatalog()` bằng WireMock — **9/9 pass** (happy path + errors + is_active logic)
- [ ] Integration test `placeOrder()` bằng WireMock (case thành công + từng mã lỗi 402/409/429/503)
- [ ] Chạy `mvn test` toàn bộ sau khi placeOrder() xong — kết quả gần nhất: **17/17 pass** ✅

**4. Self-review** — chưa tới
**5. Cập nhật tài liệu** — chưa tới
**6. Done** — chưa tới

**Bước tiếp theo cụ thể**: viết `VietShareConnector.placeOrder()` — POST /v1/orders với
`Idempotency-Key` header, parse response thành công + từng mã lỗi (402, 409 PRICE_CHANGED,
409 OUT_OF_STOCK, 429, 503), rồi viết integration test WireMock cho từng case.

---

## Backlog (chưa làm, thứ tự ưu tiên từ trên xuống)

> **Quy ước độ phức tạp:**
> - `[S]` = xong trong 1 lần code liền mạch (1 vòng Plan→Test)
> - `[M]` = cần vài vòng Plan→Test lặp lại
> - `[L]` = quá to, **PHẢI tách nhỏ hơn nữa trước khi bắt đầu**, không được giữ nguyên

- [ ] [M] Viết ConnectorRegistry + đăng ký VietShareConnector vào Spring context
- [ ] [M] Viết CatalogSyncService (job đồng bộ 10-30s/lần) ← block bởi Q-002 ✅ đã giải quyết
- [ ] [M] Viết OrderOrchestrationService: routing logic (chọn supplier theo product_id)
- [ ] [S] Viết OrderOrchestrationService: PRICE_CHANGED flow (thông báo user, cập nhật order status)
- [ ] [M] Viết OrderOrchestrationService: retry/backoff (Exponential Backoff, giữ Idempotency-Key)
- [ ] [S] L.1: Confirm schema `supplier_products` (field list + index, đối chiếu fetchCatalog mapping mới)
- [ ] [M] L.2: Thiết kế schema `orders` (field list, status enum, indexes)
- [ ] [S] L.4: Viết DDL hoàn chỉnh + cập nhật `03-db-schema.md` (làm sau L.1 và L.2 xong)
- [ ] [S] Viết Flyway migration script từ DDL đã thiết kế ở L.4
- [ ] [M] L.3: Thiết kế schema `users` + balance ← **block bởi Q-005** (ví nội bộ vs payment gateway chưa quyết định)


---

## Done (chỉ 1 dòng/task, chi tiết xem git log)

- [x] 2026-07-26 — Dọn dẹp + sửa lỗi header tài liệu (xem báo cáo đầy đủ bên dưới)

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
