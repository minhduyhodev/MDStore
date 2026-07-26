'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import { motion } from 'framer-motion';
import { Search, Filter, ShoppingCart, Tag, CheckCircle2, XCircle } from 'lucide-react';

const MOCK_PRODUCTS = [
  { id: 'p1', name: 'Netflix 1 tháng',  category: 'Streaming', price: 85000,  isActive: true,  image: 'https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=500&q=80' },
  { id: 'p2', name: 'Spotify Premium',   category: 'Music',     price: 59000,  isActive: true,  image: 'https://images.unsplash.com/photo-1614680376573-df3480f0c6ff?w=500&q=80' },
  { id: 'p3', name: 'ChatGPT Plus',      category: 'AI Tool',   price: 520000, isActive: false, image: 'https://images.unsplash.com/photo-1676299081847-824916de030a?w=500&q=80' },
  { id: 'p4', name: 'Canva Pro 1 năm',   category: 'Design',    price: 299000, isActive: true,  image: 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=500&q=80' },
  { id: 'p5', name: 'Youtube Premium',   category: 'Streaming', price: 45000,  isActive: true,  image: 'https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0?w=500&q=80' },
  { id: 'p6', name: 'Discord Nitro',     category: 'Social',    price: 99000,  isActive: true,  image: 'https://images.unsplash.com/photo-1614680376408-81e91ffe3db7?w=500&q=80' },
];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.1 }
  }
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: 'easeOut' } }
};

export default function CatalogPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const timer = setTimeout(() => {
      setProducts(MOCK_PRODUCTS);
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden">
      <Header title="Cửa hàng sản phẩm" />

      <main className="flex-1 overflow-y-auto p-8">
        <div className="max-w-7xl mx-auto space-y-8">
          
          {/* Toolbar */}
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white/50 dark:bg-slate-900/50 p-4 rounded-2xl border border-slate-200 dark:border-slate-800/60 backdrop-blur-md">
            <div className="relative w-full sm:w-96">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={18} className="text-slate-400" />
              </div>
              <input 
                type="text" 
                placeholder="Tìm kiếm sản phẩm..." 
                className="w-full pl-10 pr-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all text-sm"
              />
            </div>
            
            <div className="flex gap-3 w-full sm:w-auto">
              <button className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-4 py-2.5 bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-900 text-sm font-medium transition-colors">
                <Filter size={16} />
                Lọc
              </button>
            </div>
          </div>

          {/* Content */}
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {[1, 2, 3, 4, 5, 6].map((i) => (
                <div key={i} className="animate-pulse bg-white dark:bg-slate-900 rounded-2xl h-80 border border-slate-100 dark:border-slate-800"></div>
              ))}
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center py-20 text-red-500">
              <XCircle size={48} className="mb-4 opacity-50" />
              <p className="text-lg font-medium">Lỗi: {error}</p>
            </div>
          ) : (
            <motion.div 
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
            >
              {products.map((product) => (
                <motion.div 
                  key={product.id} 
                  variants={itemVariants}
                  className="group relative bg-white dark:bg-slate-900 rounded-2xl overflow-hidden border border-slate-200/60 dark:border-slate-800/60 shadow-sm hover:shadow-xl hover:shadow-indigo-500/10 hover:-translate-y-1 transition-all duration-300 flex flex-col"
                >
                  {/* Image Placeholder with Gradient */}
                  <div className="relative h-40 bg-gradient-to-br from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-900 overflow-hidden">
                    <img src={product.image} alt={product.name} className="w-full h-full object-cover opacity-90 group-hover:opacity-100 group-hover:scale-105 transition-all duration-500" />
                    
                    {/* Category Badge */}
                    <div className="absolute top-3 left-3 px-2.5 py-1 rounded-lg bg-black/40 backdrop-blur-md text-white text-xs font-medium flex items-center gap-1.5">
                      <Tag size={12} />
                      {product.category}
                    </div>

                    {/* Status Badge */}
                    <div className="absolute top-3 right-3">
                      {product.isActive ? (
                        <span className="flex items-center gap-1 px-2 py-1 rounded-md bg-emerald-500/90 text-white text-[10px] font-bold uppercase tracking-wider backdrop-blur-sm">
                          <CheckCircle2 size={12} /> Còn hàng
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 px-2 py-1 rounded-md bg-rose-500/90 text-white text-[10px] font-bold uppercase tracking-wider backdrop-blur-sm">
                          <XCircle size={12} /> Hết hàng
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Info */}
                  <div className="p-5 flex-1 flex flex-col">
                    <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100 mb-1 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                      {product.name}
                    </h3>
                    <div className="text-sm text-slate-500 dark:text-slate-400 mb-4 line-clamp-2">
                      Gói tài khoản tự động gia hạn, bảo hành trọn thời gian sử dụng.
                    </div>
                    
                    <div className="mt-auto pt-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                      <div className="flex flex-col">
                        <span className="text-xs text-slate-400 font-medium">Giá bán</span>
                        <span className="text-lg font-black text-slate-900 dark:text-white bg-clip-text">
                          {product.price.toLocaleString('vi-VN')}₫
                        </span>
                      </div>
                      <button 
                        disabled={!product.isActive}
                        className={`
                          flex items-center justify-center p-3 rounded-xl transition-all
                          ${product.isActive 
                            ? 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-lg shadow-indigo-600/30 hover:shadow-indigo-600/50 hover:scale-105 active:scale-95' 
                            : 'bg-slate-100 dark:bg-slate-800 text-slate-400 cursor-not-allowed'
                          }
                        `}
                      >
                        <ShoppingCart size={18} strokeWidth={2.5} />
                      </button>
                    </div>
                  </div>
                </motion.div>
              ))}
            </motion.div>
          )}
        </div>
      </main>
    </div>
  );
}
