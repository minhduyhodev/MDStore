import { Inter } from 'next/font/google';
import './globals.css';
import { Toaster } from 'sonner';
import DevToolBlocker from '@/components/DevToolBlocker';

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' });

export const metadata = {
  title: 'MDStore — Dropshipping tài khoản số',
  description: 'Mua và giao tài khoản số tự động qua API',
};

export default function RootLayout({ children }) {
  return (
    <html lang="vi" className={inter.variable} suppressHydrationWarning>
      <body className="antialiased min-h-screen selection:bg-indigo-500/30" suppressHydrationWarning>
        <DevToolBlocker />
        <Toaster position="bottom-right" richColors />
        {children}
      </body>
    </html>
  );
}
