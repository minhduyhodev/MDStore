import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useStore = create(
  persist(
    (set) => ({
      user: null, // null khi chưa đăng nhập
      walletBalance: 0,
      
      login: (role) => set({
        user: {
          id: role === 'ADMIN' ? 'admin-001' : 'user-001',
          name: role === 'ADMIN' ? 'Quản trị viên' : 'Khách hàng',
          email: role === 'ADMIN' ? 'admin@mdstore.com' : 'customer@mdstore.com',
          role: role,
          avatar: role === 'ADMIN' 
            ? 'https://ui-avatars.com/api/?name=Admin&background=4f46e5&color=fff' 
            : 'https://ui-avatars.com/api/?name=Khach+Hang&background=10b981&color=fff'
        },
        walletBalance: role === 'ADMIN' ? 999999999 : 1250000
      }),

      logout: () => set({ user: null, walletBalance: 0 }),
      
      decreaseBalance: (amount) => 
        set((state) => ({
          walletBalance: Math.max(0, state.walletBalance - amount)
        })),
        
      increaseBalance: (amount) => 
        set((state) => ({
          walletBalance: state.walletBalance + amount
        })),
    }),
    {
      name: 'mdstore-auth-storage', // lưu trạng thái vào localStorage
    }
  )
);
