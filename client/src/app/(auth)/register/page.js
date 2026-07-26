'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import { Box, Mail, Lock, User as UserIcon, ArrowRight } from 'lucide-react';
import { toast } from 'sonner';

export default function RegisterPage() {
  const router = useRouter();

  const handleRegister = (e) => {
    e.preventDefault();
    toast.success('Đăng ký thành công!', {
      description: 'Tài khoản của bạn đã được tạo.',
    });
    router.push('/catalog');
  };

  const handleGoogleLogin = () => {
    toast.success('Đăng ký với Google thành công!', {
      description: 'Đang chuyển hướng vào hệ thống...',
    });
    router.push('/catalog');
  };

  return (
    <div className="min-h-screen w-full flex bg-slate-50 dark:bg-slate-950">
      
      {/* Left Column: Image/Branding (Hidden on mobile) */}
      <div className="hidden lg:flex w-1/2 relative bg-indigo-50 overflow-hidden items-center justify-center">
        {/* Abstract shapes & Background */}
        <div className="absolute inset-0">
          <img 
            src="https://images.unsplash.com/photo-1557682250-33bd709cbe85?q=80&w=2564&auto=format&fit=crop" 
            alt="Abstract Background" 
            className="w-full h-full object-cover opacity-50 mix-blend-multiply"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-white via-white/80 to-transparent" />
        </div>

        {/* Content Overlay */}
        <div className="relative z-10 w-full max-w-lg px-12 text-slate-900">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, ease: 'easeOut' }}
          >
            <div className="flex items-center gap-3 mb-12">
              <div className="w-12 h-12 rounded-2xl bg-indigo-600/10 backdrop-blur-md flex items-center justify-center border border-indigo-600/20">
                <Box size={28} className="text-indigo-600" strokeWidth={2.5} />
              </div>
              <span className="font-black text-3xl tracking-tight text-slate-900">MDStore</span>
            </div>
            
            <h1 className="text-4xl md:text-5xl font-bold mb-6 leading-tight text-slate-900">
              Gia nhập cộng đồng dropshipping số 1.
            </h1>
            <p className="text-slate-600 text-lg leading-relaxed">
              Bắt đầu hành trình kinh doanh tài khoản số hoàn toàn tự động chỉ với vài thao tác đơn giản.
            </p>
          </motion.div>
        </div>
      </div>

      {/* Right Column: Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12 lg:p-24 relative">
        {/* Mobile Logo */}
        <div className="absolute top-8 left-8 flex lg:hidden items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white">
            <Box size={20} strokeWidth={2.5} />
          </div>
          <span className="font-bold text-xl text-slate-900 dark:text-white">MDStore</span>
        </div>

        <motion.div 
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="w-full max-w-md"
        >
          <div className="mb-10 text-center lg:text-left">
            <h2 className="text-3xl font-bold text-slate-900 dark:text-white mb-3">Tạo tài khoản mới</h2>
            <p className="text-slate-500 dark:text-slate-400">Đăng ký để truy cập vào kho sản phẩm khổng lồ</p>
          </div>

          <form onSubmit={handleRegister} className="space-y-5">
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Họ và Tên</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <UserIcon size={18} />
                </div>
                <input 
                  type="text" 
                  required
                  placeholder="Nguyễn Văn A" 
                  className="w-full pl-10 pr-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Email</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Mail size={18} />
                </div>
                <input 
                  type="email" 
                  required
                  placeholder="name@example.com" 
                  className="w-full pl-10 pr-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Mật khẩu</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Lock size={18} />
                </div>
                <input 
                  type="password" 
                  required
                  placeholder="••••••••" 
                  className="w-full pl-10 pr-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
                />
              </div>
            </div>

            <button 
              type="submit"
              className="w-full flex items-center justify-center gap-2 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-medium transition-all shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50 active:scale-[0.98] group mt-6"
            >
              Đăng ký ngay
              <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />
            </button>
          </form>

          <div className="mt-8 flex items-center gap-4">
            <div className="flex-1 h-px bg-slate-200 dark:bg-slate-800"></div>
            <div className="text-xs text-slate-400 uppercase font-medium tracking-wider">Hoặc</div>
            <div className="flex-1 h-px bg-slate-200 dark:bg-slate-800"></div>
          </div>

          <div className="mt-8 grid grid-cols-1 gap-3">
            <button 
              onClick={handleGoogleLogin}
              type="button" 
              className="flex items-center justify-center gap-3 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-xl font-medium transition-all hover:shadow-sm"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Tiếp tục với Google
            </button>
          </div>

          <p className="mt-10 text-center text-sm text-slate-500 dark:text-slate-400">
            Đã có tài khoản?{' '}
            <Link href="/login" className="font-semibold text-indigo-600 hover:text-indigo-500 transition-colors">
              Đăng nhập tại đây
            </Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
