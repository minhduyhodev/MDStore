/**
 * src/app/layout.js — Layout gốc, bọc TOÀN BỘ các trang.
 */
import { Inter } from 'next/font/google';
import './globals.css';
import Sidebar from '@/components/Sidebar';

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' });

export const metadata = {
  title: 'MDStore — Dropshipping tài khoản số',
  description: 'Mua và giao tài khoản số tự động qua API',
};

export default function RootLayout({ children }) {
  return (
    <html lang="vi" className={inter.variable} suppressHydrationWarning>
      <body className="antialiased min-h-screen selection:bg-indigo-500/30" suppressHydrationWarning>
        {/* Layout 2 cột: Sidebar cố định trái + nội dung trang phải */}
        <div className="flex min-h-screen">
          <Sidebar />

          {/* Vùng nội dung — Next.js tự inject trang hiện tại vào đây */}
          <div className="flex-1 flex flex-col relative z-10">
            {children}
          </div>
        </div>
      </body>
    </html>
  );
}
