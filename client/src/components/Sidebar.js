'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Package, ShoppingBag, Wallet, Box } from 'lucide-react';

const NAV_ITEMS = [
  { href: '/catalog', label: 'Sản phẩm', icon: Package },
  { href: '/orders',  label: 'Đơn hàng', icon: ShoppingBag },
  { href: '/wallet',  label: 'Ví tiền', icon: Wallet },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 min-h-screen border-r border-slate-200 dark:border-slate-800/60 bg-white/50 dark:bg-slate-900/50 backdrop-blur-xl flex flex-col py-8 px-4 gap-6 sticky top-0 h-screen overflow-y-auto">
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 mb-2">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center text-white shadow-lg shadow-indigo-500/30">
          <Box size={24} strokeWidth={2.5} />
        </div>
        <div className="font-bold text-2xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-slate-900 to-slate-600 dark:from-white dark:to-slate-400">
          MDStore
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 flex flex-col gap-1.5">
        <div className="px-4 text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
          Quản lý
        </div>
        {NAV_ITEMS.map((item) => {
          const isActive = pathname === item.href || pathname?.startsWith(item.href + '/');
          const Icon = item.icon;
          
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`
                group flex items-center gap-3 px-4 py-3 rounded-2xl transition-all duration-300
                ${isActive 
                  ? 'bg-indigo-50 dark:bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 font-medium' 
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800/50 hover:text-slate-900 dark:hover:text-slate-200'
                }
              `}
            >
              <Icon 
                size={20} 
                className={`transition-transform duration-300 ${isActive ? 'scale-110' : 'group-hover:scale-110'}`} 
                strokeWidth={isActive ? 2.5 : 2} 
              />
              {item.label}
              
              {isActive && (
                <div className="ml-auto w-1.5 h-1.5 rounded-full bg-indigo-500 shadow-[0_0_8px_rgba(99,102,241,0.8)]" />
              )}
            </Link>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className="mt-auto px-4 py-4 rounded-2xl bg-slate-50 dark:bg-slate-800/30 border border-slate-100 dark:border-slate-800/50">
        <div className="text-xs font-medium text-slate-500 dark:text-slate-400 mb-1">Phiên bản 1.0</div>
        <div className="text-xs text-slate-400 dark:text-slate-500">
          Kết nối API tự động tới các Supplier.
        </div>
      </div>
    </aside>
  );
}
