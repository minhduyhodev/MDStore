/**
 * src/app/page.js — Trang chủ (route /).
 *
 * ❓ Tại sao KHÔNG cần "use client"?
 *    Trang này chỉ redirect — không có state hay browser event.
 *    redirect() là Server Component API của Next.js, chạy phía server.
 */
import { redirect } from 'next/navigation';

export default function Home() {
  // Redirect về /catalog làm trang mặc định
  redirect('/catalog');
}
