# 01 — Tổng Quan Dự Án (Context, không phải kỹ thuật)

> File này trả lời: **Dự án giải quyết vấn đề gì? Cho ai? Thành công trông như thế nào?**
> Không có code. Không có schema. Chỉ có bối cảnh.

---

## Vấn đề cần giải quyết

Thị trường tài khoản số (Netflix, NordVPN, phần mềm bản quyền, proxy...) hiện có nhiều nhà cung cấp sỉ (wholesale) như VietShare cung cấp API để mua số lượng lớn. Tuy nhiên:

- Người mua lẻ không thể tiếp cận API trực tiếp (yêu cầu doanh nghiệp, volume tối thiểu).
- Việc mua thủ công qua website của từng nhà cung cấp tốn thời gian và không tự động hóa được.
- Người bán lẻ phải tự giao hàng thủ công — không scale được khi có nhiều đơn đồng thời.

**MDStore giải quyết bài toán này** bằng cách đứng ra làm trung gian: tự động mua từ API nhà cung cấp và giao hàng tức thì cho người dùng cuối.

---

## Đối tượng người dùng

| Nhóm | Ai | Họ cần gì |
|---|---|---|
| **Người mua** | Cá nhân mua lẻ tài khoản số | Mua nhanh, nhận hàng tức thì, giá tốt |
| **Admin** | Chủ store (Minh Duy) | Quản lý sản phẩm, xem doanh thu, cấu hình nhà cung cấp |
| **AI Agent** | Claude, Gemini, Copilot... | Đọc tài liệu này để code đúng mục tiêu |

---

## Luồng giá trị (Value Flow)

```
Nhà cung cấp (VietShare)
       ↓  [Giá sỉ - API]
   MDStore
       ↓  [Giá bán lẻ - Website/API]
Người dùng cuối
```

**Biên lợi nhuận** = Giá bán lẻ − Giá sỉ − Chi phí vận hành.

---

## Định nghĩa "thành công" (Success Criteria)

### Phase 1 — Hệ thống hoạt động được (MVP)
- [ ] Người dùng đặt đơn → hệ thống tự động mua từ VietShare → giao hàng trong < 5 giây.
- [ ] Xử lý đúng tất cả mã lỗi từ VietShare (PRICE_CHANGED, OUT_OF_STOCK, timeout).
- [ ] Không bao giờ trừ tiền người dùng 2 lần cho 1 đơn hàng (Idempotency).

### Phase 2 — Người dùng tự phục vụ được
- [ ] Có giao diện web để xem sản phẩm, đặt hàng, xem lịch sử.
- [ ] Admin quản lý sản phẩm và cấu hình nhà cung cấp qua UI.

### Phase 3 — Có thể mở rộng
- [ ] Thêm nhà cung cấp mới chỉ cần viết 1 Adapter, không sửa code core.
- [ ] Hệ thống chịu được 1,000 đơn đồng thời mà không bị nghẽn.

---

## Ràng buộc quan trọng (Constraints)

1. **Tính tiền trước khi giao hàng** — không có cơ chế "mua trước trả sau" ở phiên bản này.
2. **Hàng hóa là kỹ thuật số** — không thể "hoàn hàng" sau khi giao. Logic hoàn tiền phức tạp hơn e-commerce thông thường.
3. **Phụ thuộc vào API bên thứ ba** — khi VietShare down hoặc đổi API, hệ thống bị ảnh hưởng trực tiếp.
4. **Chi phí thấp** — dự án cá nhân, ưu tiên giải pháp đơn giản và rẻ (self-hosted VPS, không dùng cloud managed services đắt tiền).

---

## Tech Stack (tóm tắt, không phải nguồn sự thật về kỹ thuật)

- Backend: Java 21 + Spring Boot 3.2+
- Frontend: ReactJS (Vite) + Tailwind CSS + shadcn/ui
- DB: PostgreSQL 16
- Cache / Rate Limit: Redis + Bucket4j

→ Quyết định chi tiết và lý do: [`02-decisions.md`](./02-decisions.md)

---

## Cấu Trúc Thư Mục Dự Án

```
MDStore/
├── README.md                  ← Quick Start cho developer mới
├── docs/                      ← Toàn bộ tài liệu hệ thống
│   ├── CLAUDE.md              ← Entrypoint cho agent
│   ├── 01-overview.md         ← File này
│   ├── 02-decisions.md        ← ADR + diagrams kiến trúc
│   ├── 03-db-schema.md        ← Schema DB (nguồn sự thật)
│   ├── 04-suppliers/          ← Tài liệu từng nhà cung cấp
│   │   ├── vietshare.md
│   │   └── _template.md
│   ├── 05-business-flows.md   ← Luồng nghiệp vụ
│   ├── 06-glossary.md         ← Thuật ngữ dự án
│   ├── 07-open-questions.md   ← Câu hỏi chưa được quyết định
│   ├── 08-roadmap.md          ← Định hướng dài hạn theo Phase
│   └── PROGRESS.md            ← Task đang code (cập nhật liên tục)
├── mdstore-backend/           ← Java 21 + Spring Boot 3.2+
└── mdstore-frontend/          ← ReactJS + Vite + Tailwind CSS
```
