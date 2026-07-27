# 08 — Roadmap (Định Hướng Theo Phase)

> **Phân biệt với PROGRESS.md:**
> - **File này (08-roadmap.md)** = định hướng dài hạn theo Phase/tuần. Cập nhật khi Phase hoàn thành hoặc kế hoạch thay đổi lớn. KHÔNG cập nhật task-level hàng ngày.
> - **PROGRESS.md** = task đang code ngay bây giờ. Cập nhật liên tục sau mỗi bước nhỏ.

---

## Các Giai Đoạn Phát Triển (Phases)

### Phase 1: Core System & Tích Hợp VietShare ✅ Đang hoàn thiện

**Mục tiêu:** Xây dựng nền tảng vững chắc, kết nối thành công với nguồn cung cấp tài khoản.

**Backend:**
- [x] Khởi tạo project Spring Boot 3 + Java 21 Virtual Threads
- [x] Thiết lập PostgreSQL 16 (Không sử dụng Foreign Key) & Redis
- [x] Định nghĩa interface `SupplierConnector` + `ConnectorRegistry`
- [x] Viết `VietShareSigner` — tạo canonical string + HMAC-SHA256
- [ ] Viết `VietShareConnector.fetchCatalog()` — gọi GET /v1/products
- [ ] Viết `VietShareConnector.placeOrder()` — xử lý đủ mã lỗi
- [ ] Integration test với WireMock
- [ ] Cấu hình Bucket4j cho Rate Limiting
- [ ] Viết ConnectorRegistry + đăng ký vào Spring context

**Tài liệu:** ✅ Hoàn thiện 100% thư mục `docs/`

---

### Phase 2: User Dashboard & Quản Lý Đơn Hàng

**Mục tiêu:** Người dùng có thể tự thao tác mua hàng và xem lịch sử trên giao diện.

**Frontend:**
- [ ] Khởi tạo Next.js App Router + JavaScript + Tailwind CSS
- [ ] UI/UX trang chủ (Danh sách sản phẩm)
- [ ] Tính năng Giỏ hàng / Đặt hàng
- [ ] Màn hình lịch sử đơn hàng & Chi tiết tài khoản đã giao

**Backend:**
- [ ] REST API cho Frontend (Authentication, Product List, Order History)
- [ ] CatalogSyncService (job đồng bộ 10-30s/lần)
- [ ] OrderOrchestrationService (chọn nguồn, gọi connector, xử lý PRICE_CHANGED)
- [ ] Rà soát và đồng bộ DDL/JPA schema cho `orders`, `supplier_products` (Hibernate `ddl-auto=update`, không dùng Flyway)

---

### Phase 3: Mở Rộng & Tối Ưu

**Mục tiêu:** Khả năng scale, thêm nhiều nhà cung cấp ngoài VietShare.

- [ ] Thêm Provider thứ 2 (chỉ cần viết 1 Adapter mới — xem ADR-004)
- [ ] Tối ưu hóa truy vấn Database với Composite Indexes
- [ ] CI/CD Pipelines (GitHub Actions → Docker Hub → VPS)

---

## Tiêu Chí Hoàn Thành (Definition of Done — DoD)

Một task chỉ được coi là **Hoàn Thành (Done)** khi đáp ứng **TẤT CẢ** các tiêu chí sau:

1. **Code:** Đã push lên nhánh chính không có lỗi cú pháp.
2. **Review:** Vượt qua Code Review của Tech Lead.
3. **No-FK Compliance:** Không có câu lệnh `ADD FOREIGN KEY` nào trong DB schema.
4. **Testing:** Unit test cốt lõi (nhất là hàm ký HMAC và hàm gọi API VietShare) có Coverage > 80%.
5. **Postman/Swagger:** Mọi API Endpoints được mô tả trên Swagger UI hoặc Postman Collection.
6. **Lỗi 409/429:** Đã test thực tế các kịch bản lỗi khi giá thay đổi hoặc vượt rate limit.
