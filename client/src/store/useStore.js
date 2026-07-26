import { create } from 'zustand';

export const useStore = create((set) => ({
  user: {
    id: 'user-001',
    name: 'Khách hàng',
    email: 'customer@mdstore.com',
  },
  walletBalance: 1250000,
  
  decreaseBalance: (amount) => 
    set((state) => ({
      walletBalance: Math.max(0, state.walletBalance - amount)
    })),
    
  increaseBalance: (amount) => 
    set((state) => ({
      walletBalance: state.walletBalance + amount
    })),
}));
