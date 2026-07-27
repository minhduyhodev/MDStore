'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import { motion } from 'framer-motion';
import { Tag, CheckCircle2, XCircle, ArrowLeft, Plus, Minus, ShoppingCart, RefreshCw, AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { toast } from 'sonner';

const MOCK_CATALOG = {
  'p1': { id: 'p1', name: 'Netflix 1 tháng',  category: 'Streaming', price: 85000,  isActive: true,  image: 'https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=500&q=80', description: 'Gói tài khoản tự động gia hạn, bảo hành trọn thời gian sử dụng. Thích hợp cho cá nhân sử dụng trên 1 thiết bị.', warranty: 'Bảo hành 1 đổi 1 trong 30 ngày.' },
  'p2': { id: 'p2', name: 'Spotify Premium',   category: 'Music',     price: 59000,  isActive: true,  image: 'https://images.unsplash.com/photo-1614680376573-df3480f0c6ff?w=500&q=80', description: 'Nghe nhạc không quảng cáo, tải nhạc ngoại tuyến, chất lượng âm thanh cao cấp.', warranty: 'Bảo hành toàn bộ thời gian sử dụng.' },
  'p3': { id: 'p3', name: 'ChatGPT Plus',      category: 'AI Tool',   price: 520000, isActive: false, image: 'https://images.unsplash.com/photo-1676299081847-824916de030a?w=500&q=80', description: 'Trải nghiệm mô hình GPT-4 mới nhất, tốc độ phản hồi nhanh hơn, ưu tiên truy cập khi quá tải.', warranty: 'Bảo hành 1 đổi 1.' },
  'p4': { id: 'p4', name: 'Canva Pro 1 năm',   category: 'Design',    price: 299000, isActive: true,  image: 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=500&q=80', description: 'Mở khóa toàn bộ tính năng Canva Pro, thư viện ảnh, video, âm thanh khổng lồ.', warranty: 'Bảo hành 1 năm.' },
  'p5': { id: 'p5', name: 'Youtube Premium',   category: 'Streaming', price: 45000,  isActive: true,  image: 'https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0?w=500&q=80', description: 'Xem video không quảng cáo, phát trong nền, tải video ngoại tuyến. Kèm Youtube Music.', warranty: 'Bảo hành trọn thời gian gói.' },
  'p6': { id: 'p6', name: 'Discord Nitro',     category: 'Social',    price: 99000,  isActive: true,  image: 'https://images.unsplash.com/photo-1614680376408-81e91ffe3db7?w=500&q=80', description: 'Nâng cấp trải nghiệm Discord với avatar động, banner, tải file lớn hơn, boost server.', warranty: 'Bảo hành 1 đổi 1.' },
};

export default function ProductDetailPage({ params }) {
  const { id } = params;
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (id === 'error-test') {
        setError('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.');
        setLoading(false);
        return;
      }
      
      const foundProduct = MOCK_CATALOG[id];
      if (foundProduct) {
        setProduct(foundProduct);
      }
      // If not found, product is null -> handled by not-found state
      setLoading(false);
    }, 600);
    return () => clearTimeout(timer);
  }, [id]);

  const handleDecrease = () => {
    if (quantity > 1) setQuantity(quantity - 1);
  };

  const handleIncrease = () => {
    if (quantity < 10) setQuantity(quantity + 1);
  };

  const handlePurchase = () => {
    // Không tự trừ ví hoặc gọi API thật, chỉ toast báo chuẩn bị cho task FE-03
    toast.info('Chuẩn bị tạo đơn...', {
      description: `Sẽ mở xác nhận thanh toán cho ${quantity}x ${product.name} trong task FE-03.`,
    });
  };

  if (loading) {
    return (
      <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50 ">
        <Header title="Chi tiết Sản phẩm" />
        <main className="flex-1 p-8 flex justify-center items-center">
          <div className="animate-pulse flex flex-col items-center gap-4">
            <RefreshCw className="animate-spin text-slate-400" size={32} />
            <p className="text-slate-500 font-medium">Đang tải thông tin sản phẩm...</p>
          </div>
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50 ">
        <Header title="Lỗi tải dữ liệu" />
        <main className="flex-1 p-8 flex flex-col items-center justify-center">
          <AlertCircle size={48} className="text-rose-500 mb-4 opacity-80" />
          <h2 className="text-xl font-bold text-slate-900  mb-2">Đã xảy ra lỗi</h2>
          <p className="text-slate-500  mb-6">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-6 py-2.5 bg-slate-900  text-white  rounded-lg hover:bg-slate-800  transition-colors font-medium"
          >
            Thử lại
          </button>
        </main>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50 ">
        <Header title="Không tìm thấy" />
        <main className="flex-1 p-8 flex flex-col items-center justify-center">
          <AlertCircle size={48} className="text-slate-400 mb-4 opacity-50" />
          <h2 className="text-xl font-bold text-slate-900  mb-2">Sản phẩm không tồn tại</h2>
          <p className="text-slate-500  mb-6">Sản phẩm bạn đang tìm kiếm có thể đã bị xóa hoặc không còn bán.</p>
          <Link 
            href="/catalog"
            className="px-6 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-medium"
          >
            Về Cửa hàng
          </Link>
        </main>
      </div>
    );
  }

  const totalPrice = product.price * quantity;

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-50/50 ">
      <Header title="Chi tiết Sản phẩm" />
      
      <main className="flex-1 overflow-y-auto p-4 sm:p-8">
        <div className="max-w-5xl mx-auto">
          
          {/* Breadcrumb */}
          <div className="mb-6 flex items-center">
            <Link 
              href="/catalog" 
              className="flex items-center gap-2 text-sm font-medium text-slate-500  hover:text-slate-900  transition-colors"
            >
              <ArrowLeft size={16} /> Quay lại cửa hàng
            </Link>
          </div>

          <motion.div 
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col md:flex-row gap-8"
          >
            {/* Left: Image */}
            <div className="md:w-5/12 shrink-0">
              <div className="bg-white  rounded-2xl border border-slate-200  overflow-hidden shadow-sm relative">
                <div className="aspect-[4/3] bg-gradient-to-br from-slate-100 to-slate-200  ">
                  <img src={product.image} alt={product.name} className="w-full h-full object-cover opacity-90" />
                </div>
                {/* Category Badge on Image */}
                <div className="absolute top-4 left-4 px-3 py-1.5 rounded-lg bg-black/50 backdrop-blur-md text-white text-sm font-medium flex items-center gap-1.5">
                  <Tag size={14} />
                  {product.category}
                </div>
              </div>
            </div>

            {/* Right: Info */}
            <div className="flex-1 flex flex-col">
              <div className="mb-2">
                {product.isActive ? (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-emerald-50  text-emerald-700  text-xs font-bold uppercase tracking-wider border border-emerald-200 ">
                    <CheckCircle2 size={14} /> Còn hàng
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-rose-50  text-rose-700  text-xs font-bold uppercase tracking-wider border border-rose-200 ">
                    <XCircle size={14} /> Hết hàng
                  </span>
                )}
              </div>
              
              <h1 className="text-2xl sm:text-3xl font-bold text-slate-900  mb-4">{product.name}</h1>
              
              <div className="text-3xl font-black text-slate-900  mb-6 pb-6 border-b border-slate-200 ">
                {product.price.toLocaleString('vi-VN')}₫
              </div>

              <div className="space-y-6 mb-8">
                <div>
                  <h3 className="text-sm font-bold text-slate-900  mb-2">Mô tả sản phẩm</h3>
                  <p className="text-slate-600  leading-relaxed text-sm">
                    {product.description || 'Gói tài khoản tự động gia hạn, bảo hành trọn thời gian sử dụng.'}
                  </p>
                </div>
                
                <div>
                  <h3 className="text-sm font-bold text-slate-900  mb-2">Chính sách bảo hành</h3>
                  <p className="text-slate-600  leading-relaxed text-sm">
                    {product.warranty || 'Bảo hành 1 đổi 1 trong thời gian sử dụng.'}
                  </p>
                </div>
              </div>

              {/* Purchase Action Box */}
              <div className="mt-auto bg-white  rounded-xl border border-slate-200  p-5 shadow-sm">
                <div className="flex flex-col sm:flex-row gap-4 items-end sm:items-center justify-between mb-5">
                  <div>
                    <label className="block text-sm font-bold text-slate-700  mb-2">Số lượng</label>
                    <div className="flex items-center border border-slate-200  rounded-lg overflow-hidden w-32">
                      <button 
                        onClick={handleDecrease}
                        disabled={quantity <= 1 || !product.isActive}
                        className="w-10 h-10 flex items-center justify-center bg-slate-50  hover:bg-slate-100  disabled:opacity-50 text-slate-600  transition-colors"
                      >
                        <Minus size={16} />
                      </button>
                      <div className="flex-1 text-center font-bold text-slate-900  border-x border-slate-200  h-10 flex items-center justify-center bg-white ">
                        {quantity}
                      </div>
                      <button 
                        onClick={handleIncrease}
                        disabled={quantity >= 10 || !product.isActive}
                        className="w-10 h-10 flex items-center justify-center bg-slate-50  hover:bg-slate-100  disabled:opacity-50 text-slate-600  transition-colors"
                      >
                        <Plus size={16} />
                      </button>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <div className="text-sm font-medium text-slate-500  mb-1">Tổng cộng</div>
                    <div className="text-2xl font-black text-emerald-600 ">
                      {totalPrice.toLocaleString('vi-VN')}₫
                    </div>
                  </div>
                </div>

                <button 
                  disabled={!product.isActive}
                  onClick={handlePurchase}
                  className={`
                    w-full py-3.5 rounded-xl font-bold flex items-center justify-center gap-2 transition-all
                    ${product.isActive 
                      ? 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-lg shadow-indigo-600/20 active:scale-[0.98]' 
                      : 'bg-slate-100  text-slate-400  cursor-not-allowed'
                    }
                  `}
                >
                  <ShoppingCart size={18} strokeWidth={2.5} />
                  {product.isActive ? 'Tiến hành mua hàng' : 'Tạm hết hàng'}
                </button>
              </div>

            </div>
          </motion.div>
        </div>
      </main>
    </div>
  );
}
