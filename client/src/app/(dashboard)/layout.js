import Sidebar from '@/components/Sidebar';

export default function DashboardLayout({ children }) {
  return (
    <div className="flex min-h-screen">
      <Sidebar />
      {/* Vùng nội dung */}
      <div className="flex-1 flex flex-col relative z-10">
        {children}
      </div>
    </div>
  );
}
