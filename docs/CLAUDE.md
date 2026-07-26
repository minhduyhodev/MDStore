# CLAUDE.md — Entrypoint cho AI Agent

> **ĐỌC FILE NÀY TRƯỚC TIÊN.** Sau đó đọc các file liên quan dựa trên task cụ thể (không cần đọc toàn bộ).

---

## 1. Dự án là gì?

**MDStore** là hệ thống Dropshipping tài khoản số tự động — mua tài khoản từ nhà cung cấp (VD: VietShare) và bán lại cho người dùng cuối, toàn bộ quá trình giao hàng tự động qua API.

→ Đọc [`01-overview.md`](./01-overview.md) để hiểu bối cảnh đầy đủ.

---

## 2. Kiến trúc & quyết định đã chốt

Trước khi code bất cứ thứ gì, đọc [`02-decisions.md`](./02-decisions.md).

Các quyết định quan trọng nhất:
- **NO FOREIGN KEY** ở mức DB — bắt buộc tuân theo, không tự đảo ngược.
- **Virtual Threads (Java 21)** — không dùng `@Async` hay thread pool thủ công.
- **HMAC-SHA256** để ký mọi request ra VietShare — không bỏ qua bước này.

---

## 3. Database

Nguồn sự thật duy nhất về schema: [`03-db-schema.md`](./03-db-schema.md)

- Đọc file này thay vì suy ra schema từ code cũ.
- Mọi thay đổi schema phải được cập nhật vào file này trước khi implement.

---

## 4. Nhà cung cấp (Suppliers)

Mỗi nhà cung cấp có một file tài liệu riêng trong [`04-suppliers/`](./04-suppliers/):
- [`vietshare.md`](./04-suppliers/vietshare.md) — API VietShare (nguồn duy nhất hiện tại)
- [`_template.md`](./04-suppliers/_template.md) — khung để thêm nguồn mới

> **Không tóm tắt lại nội dung supplier** — đọc bản gốc để implement đúng signing format.

---

## 5. Luồng nghiệp vụ

Đọc [`05-business-flows.md`](./05-business-flows.md) khi implement bất kỳ luồng nào liên quan đến:
- Đặt đơn hàng
- Sync catalog sản phẩm
- Xử lý lỗi từ supplier
- Retry logic

---

## 6. Thuật ngữ

Dự án có nhiều từ có nghĩa riêng. Đọc [`06-glossary.md`](./06-glossary.md) khi gặp từ không chắc chắn.

---

## 7. Quy tắc khi agent gặp điều chưa rõ

**KHÔNG được tự đoán rồi im lặng code** khi gặp bất kỳ trường hợp nào sau:

| Tình huống | Hành động bắt buộc |
|---|---|
| Requirement mâu thuẫn nhau | Dừng lại, ghi vào `07-open-questions.md` |
| Logic nghiệp vụ chưa được định nghĩa | Dừng lại, ghi vào `07-open-questions.md` |
| Cần thay đổi quyết định đã chốt trong `02-decisions.md` | Dừng lại, ghi vào `07-open-questions.md` |
| Schema DB không khớp với yêu cầu mới | Dừng lại, ghi vào `07-open-questions.md` |

Ghi xong câu hỏi vào 07-open-questions.md PHẢI dừng hẳn, không code phần liên quan cho tới khi có câu trả lời — không được vừa ghi chú vừa tự đoán tiếp để khỏi delay.

**Danh sách file chính thức (không tự tạo file ngoài danh sách này):**
- `CLAUDE.md`, `01-overview.md`, `02-decisions.md`, `03-db-schema.md`
- `04-suppliers/{tên}.md`, `04-suppliers/_template.md`
- `05-business-flows.md`, `06-glossary.md`, `07-open-questions.md`
- `08-roadmap.md` (định hướng dài hạn theo Phase — không cập nhật task-level)
- `09-frontend.md` (cấu trúc + hướng dẫn project Next.js)
- `10-backend-structure-explained.md` (cấu trúc code backend, luồng chạy, giải thích cho người mới)
- `PROGRESS.md` (task đang code — cập nhật liên tục)

**Format khi ghi câu hỏi:**
```
Nêu rõ: (1) tình huống gặp phải, (2) 2 hướng hiểu có thể có, (3) hướng agent đề xuất + lý do.
Không được chọn im lặng như lựa chọn thứ 4.
```

→ Xem mẫu và file câu hỏi thực tế: [`07-open-questions.md`](./07-open-questions.md)

---

## 7a. Quy tắc "1 chủ đề = 1 file" — không tự tạo file tài liệu mới

Danh sách file tài liệu CHÍNH THỨC: `01-overview`, `02-decisions`, `03-db-schema`,
`04-suppliers/`, `05-business-flows`, `06-glossary`, `07-open-questions`,
`08-roadmap`, `09-frontend`, `10-backend-structure-explained`, `PROGRESS.md`, `CLAUDE.md` (file này). Không có file nào khác.

- Thiếu thông tin kiến trúc → bổ sung vào `02-decisions.md`, KHÔNG tạo file mới.
- Thiếu thông tin DB → bổ sung vào `03-db-schema.md`.
- Thiếu thông tin supplier → bổ sung vào `04-suppliers/{tên}.md`.
- Cần loại tài liệu hoàn toàn mới → hỏi người dùng trước (theo mục 7), không tự tạo.

---

## 7b. Bắt đầu MỌI session — đọc PROGRESS.md trước tiên

Trước khi hỏi người dùng cần làm gì, đọc [`PROGRESS.md`](./PROGRESS.md), mục
**Current focus**. Đây là nguồn sự thật duy nhất về "đang làm đến đâu" — không
cần đọc lại toàn bộ code hay lịch sử chat để suy luận.

Nếu Current focus trống → lấy task đầu Backlog. Báo ngắn gọn: "Đang tiếp tục:
[task], bước [x/6], bước tiếp theo: [...]" rồi bắt đầu làm luôn.

---

## 7c. Làm việc trong task — khung 6 giai đoạn bắt buộc

Mọi task đi qua đủ: **Plan → Code → Test → Self-review → Cập nhật tài liệu →
Done** (chi tiết từng bước xem trong `PROGRESS.md`, mục "Khung 6 giai đoạn").
Không nhảy cóc, không báo "xong" khi test chưa 100% pass hoặc self-review chưa
soát TODO/secret hardcode.

Chỉ khi cả 6 giai đoạn tick xong mới được: chuyển task sang Done trong
`PROGRESS.md`, đưa task tiếp theo vào Current focus, rồi báo cáo người dùng
theo mẫu trong `PROGRESS.md` (mục "Mẫu báo cáo").

Khi nhận task [L] từ Backlog, agent phải tách nhỏ trước ở giai đoạn Plan, không được giữ nguyên độ lớn [L] rồi code thẳng.

---

## 8. Thứ tự đọc theo task

| Task | Đọc theo thứ tự |
|---|---|
| Implement tính năng mới | `02-decisions` → `03-db-schema` → `05-business-flows` |
| Tích hợp supplier | `04-suppliers/{tên}.md` → `02-decisions` |
| Fix bug liên quan đến đơn hàng | `05-business-flows` → `03-db-schema` |
| Hiểu tổng thể dự án | `01-overview` → `02-decisions` |
| Không chắc về một thuật ngữ | `06-glossary` |
| Hỏi về tiến độ hoặc ưu tiên công việc | `PROGRESS.md` → `08-roadmap.md` |
| Implement / đọc code Frontend Next.js | `09-frontend.md` |
| Hiểu cấu trúc package backend, luồng chạy | `10-backend-structure-explained.md` |

---

## 9. Quy ước Git & Push Code

**Cú pháp tên nhánh (Branch naming convention):**
Luôn tạo nhánh mới cho mỗi task theo cú pháp `<type>/<tên-task-viết-tắt>`:
- `feat/`: Tính năng mới (VD: `feat/vietshare-connector`)
- `fix/`: Sửa lỗi (VD: `fix/hmac-signature`)
- `docs/`: Cập nhật tài liệu (VD: `docs/cleanup-files`)
- `chore/`: Cấu hình, cài đặt không liên quan logic (VD: `chore/init-spring-boot`)
- `refactor/`: Sửa cấu trúc code không đổi logic.

> **⚠️ QUY TẮC CẤM (CRITICAL):**
> Agent **TUYỆT ĐỐI KHÔNG ĐƯỢC TỰ ĐỘNG COMMIT VÀ PUSH CODE** lên Git (không chạy `git commit`, `git push`).
> Việc review code, commit và push code hoặc tạo Pull Request là quyền quyết định hoàn toàn của người dùng. Agent chỉ được phép viết code trên file local.
