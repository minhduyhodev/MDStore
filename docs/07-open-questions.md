# 07 — Open Questions (Câu Hỏi Cần Làm Rõ)

> **Đây là nơi Agent GHI câu hỏi thay vì tự đoán rồi im lặng code sai.**
>
> **Quy tắc dành cho Agent:**
> - Khi gặp tình huống chưa rõ → ghi câu hỏi vào đây theo format mẫu.
> - Nêu rõ: (1) tình huống gặp phải, (2) 2 hướng hiểu có thể có, (3) hướng đề xuất + lý do.
> - Không được chọn im lặng như lựa chọn thứ 4.
>
> **Quy tắc dành cho Owner (Minh Duy):**
> - Đọc lướt file này định kỳ (không cần lục chat).
> - Trả lời câu hỏi → ghi vào phần "Quyết định" của từng mục → cập nhật file liên quan.

---

## Format câu hỏi mẫu

```markdown
## Q-XXX: [Tiêu đề ngắn gọn]

**Ngày ghi:** YYYY-MM-DD  
**Trạng thái:** 🟡 Chờ quyết định | ✅ Đã quyết định | ❌ Không cần thiết  
**Tác động:** [file/feature nào bị block]

**Tình huống:**
[Mô tả ngắn gọn tình huống cụ thể gặp phải]

**Hướng hiểu A:**
[Mô tả hướng A] → [Hệ quả nếu chọn A]

**Hướng hiểu B:**
[Mô tả hướng B] → [Hệ quả nếu chọn B]

**Đề xuất:** Hướng [A/B] vì [lý do ngắn gọn].

**Quyết định:** *(Owner điền vào)*
```

---

## Q-001: Cơ chế encryption API Secret trong DB

**Ngày ghi:** 2026-07-25  
**Trạng thái:** 🟡 Chờ quyết định  
**Tác động:** `suppliers` table, `VietShareAdapter` — không implement được bước đọc API Secret nếu chưa rõ cơ chế.

**Tình huống:**
`suppliers.api_key` và `suppliers.api_secret` được lưu trong DB. Nếu DB bị lộ, toàn bộ API credential của VietShare bị compromise. Cần quyết định cách lưu trữ an toàn.

**Hướng hiểu A:**
Lưu plaintext trong DB, bảo vệ bằng cách giới hạn quyền truy cập DB.  
→ Đơn giản, không cần thêm code. Rủi ro: DB dump = lộ hết credential.

**Hướng hiểu B:**
Encrypt trước khi lưu, decrypt khi đọc (AES-256). Key encryption lưu trong env var.  
→ An toàn hơn, cần implement `EncryptionService`. Nếu mất encryption key = mất luôn data.

**Đề xuất:** Hướng B (AES-256) vì project là thương mại, bảo vệ credential của bên thứ ba là bắt buộc. Encryption key lưu trong `.env`, không commit lên git.

**Quyết định:** *(Minh Duy điền vào)*

---

## Q-002: API lấy catalog sản phẩm từ VietShare

**Ngày ghi:** 2026-07-25  
**Ngày quyết định:** 2026-07-26  
**Trạng thái:** ✅ Đã quyết định  
**Tác động:** `05-business-flows.md` Flow 3, `04-suppliers/vietshare.md`

**Tình huống:**
Cần cập nhật giá sỉ (`supply_price`) và danh sách sản phẩm từ VietShare định kỳ. Nhưng tài liệu VietShare hiện tại chỉ document API đặt hàng, không có API lấy catalog.

**Hướng hiểu A:**
VietShare có API lấy catalog — cần hỏi VietShare để lấy tài liệu.  
→ Implement sync tự động, `supply_price` luôn fresh.

**Hướng hiểu B:**
VietShare không có API catalog, Admin phải cập nhật giá thủ công qua Admin UI.  
→ Đơn giản hơn, nhưng Admin phải chủ động check giá.

**Đề xuất:** Hỏi VietShare trước. Nếu có API → chọn A. Nếu không → chọn B và implement Admin UI cho manual update.

**Quyết định:** Hướng A — VietShare CÓ API catalog (đọc thiếu trước đó).

- API dùng: `GET /v1/products` (alias: `GET /v1/catalog`) — lấy toàn bộ danh sách.  
  `GET /v1/products/{id}` hoặc `GET /v1/stock/{id}` — chi tiết 1 sản phẩm.
- Field `price` trong response là **giá bán của VietShare đã gồm markup của họ**, không phải giá vốn gốc. MDStore coi đây là "giá nhập", rồi tự cộng markup riêng khi hiển thị cho khách cuối. Không có endpoint nào trả giá vốn thật.
- Tần suất sync: 10–30 giây/lần qua `CatalogSyncService` (cron job). KHÔNG gọi API theo từng request của khách hàng.
- Khi đặt đơn (`POST /v1/orders`): gửi `max_unit_price` = giá `price` lấy từ cache catalog gần nhất. Cache cũ vài giây vẫn ổn vì VietShare chốt lại giá/tồn cuối cùng khi xử lý đơn.


---

## Q-003: Quy trình Admin cập nhật giá bán lẻ

**Ngày ghi:** 2026-07-25  
**Trạng thái:** 🟡 Chờ quyết định  
**Tác động:** `05-business-flows.md` Flow 4, Admin UI scope.

**Tình huống:**
`products.price` (giá bán lẻ) cần được cập nhật khi muốn thay đổi biên lợi nhuận. Chưa rõ quy trình vận hành.

**Hướng hiểu A:**
Admin cập nhật giá trực tiếp qua Admin UI (CRUD đơn giản).  
→ Nhanh, nhưng không có audit trail, dễ sai sót.

**Hướng hiểu B:**
Admin tạo "price change request", có bước preview tác động (biên lợi nhuận mới), sau đó confirm.  
→ An toàn hơn, có audit. Phức tạp hơn để implement.

**Đề xuất:** Hướng A cho Phase 1 (MVP). Hướng B khi có nhiều Admin hoặc cần audit trail.

**Quyết định:** *(Minh Duy điền vào)*

---

## Q-004: Khi user confirm giá mới (PRICE_CHANGED) — tạo đơn mới hay update đơn cũ?

**Ngày ghi:** 2026-07-25  
**Trạng thái:** 🟡 Chờ quyết định  
**Tác động:** `05-business-flows.md` Flow 5, `OrderService`.

**Tình huống:**
Đơn `FAILED_PRICE_CHANGED` — user chọn "xác nhận giá mới và mua lại". Có 2 cách xử lý.

**Hướng hiểu A:**
Tạo đơn hàng mới (order_id mới, idempotency_key mới). Đơn cũ giữ nguyên `FAILED_PRICE_CHANGED`.  
→ Có audit trail đầy đủ. Nhưng user có thể thấy 2 đơn cho 1 giao dịch — confusing.

**Hướng hiểu B:**
Update đơn cũ: reset `status = PENDING`, cập nhật `idempotency_key` mới, retry với giá mới.  
→ User chỉ thấy 1 đơn. Nhưng mất audit trail về lần fail đầu.

**Đề xuất:** Hướng A (tạo đơn mới) vì audit trail quan trọng trong hệ thống tài chính. Frontend ẩn đơn `FAILED_PRICE_CHANGED` khỏi danh sách mặc định của user.

**Quyết định:** *(Minh Duy điền vào)*

---

## Q-005: Cơ chế ví người dùng (user balance)

**Ngày ghi:** 2026-07-25  
**Trạng thái:** 🟡 Chờ quyết định  
**Tác động:** Auth, `OrderService`, `users` table — chưa define.

**Tình huống:**
Flow đặt hàng cần "kiểm tra số dư" và "trừ số dư", nhưng `users` table chưa được define trong schema. Cần biết cơ chế ví hoạt động như thế nào.

**Hướng hiểu A:**
Ví nội bộ: user nạp tiền vào MDStore, số dư lưu trong `users.balance`. Mua hàng trừ thẳng.  
→ Cần implement nạp tiền flow, cần payment gateway integration.

**Hướng hiểu B:**
Không có ví: mỗi lần mua, user thanh toán trực tiếp qua payment gateway (MoMo, VNPay...).  
→ Không cần quản lý số dư, nhưng mỗi giao dịch phải qua payment gateway → latency cao hơn.

**Đề xuất:** Hỏi Owner về model kinh doanh. Nếu target user mua nhiều lần → Hướng A. Nếu mua 1 lần → Hướng B.

**Quyết định:** *(Minh Duy điền vào)*
