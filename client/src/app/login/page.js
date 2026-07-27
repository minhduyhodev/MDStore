'use client';

import { useStore } from '@/store/useStore';
import { useRouter } from 'next/navigation';
import { ShieldAlert, User, ArrowRight, LogIn } from 'lucide-react';
import { motion } from 'framer-motion';
import { useEffect, useState } from 'react';

export default function LoginPage() {
  const login = useStore(state => state.login);
  const user = useStore(state => state.user);
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setMounted(true);
    if (user) {
      if (user.role === 'ADMIN') router.replace('/admin/suppliers');
      else router.replace('/catalog');
    }
  }, [user, router]);

  const handleLogin = (role) => {
    login(role);
    if (role === 'ADMIN') {
      router.push('/admin/suppliers');
    } else {
      router.push('/catalog');
    }
  };

  if (!mounted || user) return null;

  return (
    <div className="min-h-[80vh] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white rounded-2xl border border-slate-200 shadow-xl overflow-hidden">
        <div className="p-8 text-center bg-slate-50 border-b border-slate-100">
          <div className="w-16 h-16 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <LogIn size={28} />
          </div>
          <h1 className="text-2xl font-bold text-slate-900 mb-2">Đăng Nhập MDStore</h1>
          <p className="text-slate-500 text-sm">Vui lòng chọn tài khoản giả lập để tiếp tục trải nghiệm hệ thống</p>
        </div>
        
        <div className="p-8 space-y-4">
          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => handleLogin('ADMIN')}
            className="w-full group flex items-center justify-between p-4 bg-indigo-50 border border-indigo-100 rounded-xl hover:bg-indigo-100 hover:border-indigo-200 transition-colors shadow-sm"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-indigo-600 text-white rounded-lg flex items-center justify-center shadow-md">
                <ShieldAlert size={20} />
              </div>
              <div className="text-left">
                <div className="font-bold text-indigo-900">Quản trị viên (Admin)</div>
                <div className="text-xs text-indigo-600 mt-0.5">Truy cập Dashboard Admin</div>
              </div>
            </div>
            <ArrowRight className="text-indigo-400 group-hover:text-indigo-600 transition-colors" size={20} />
          </motion.button>

          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => handleLogin('USER')}
            className="w-full group flex items-center justify-between p-4 bg-emerald-50 border border-emerald-100 rounded-xl hover:bg-emerald-100 hover:border-emerald-200 transition-colors shadow-sm"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-emerald-500 text-white rounded-lg flex items-center justify-center shadow-md">
                <User size={20} />
              </div>
              <div className="text-left">
                <div className="font-bold text-emerald-900">Khách hàng (User)</div>
                <div className="text-xs text-emerald-600 mt-0.5">Mua hàng & Nạp ví</div>
              </div>
            </div>
            <ArrowRight className="text-emerald-400 group-hover:text-emerald-600 transition-colors" size={20} />
          </motion.button>
        </div>
      </div>
    </div>
  );
}
