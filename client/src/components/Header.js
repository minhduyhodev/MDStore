import { Bell, User } from 'lucide-react';

export default function Header({ title }) {
  return (
    <header className="sticky top-0 z-40 bg-white/70 dark:bg-slate-900/70 backdrop-blur-md border-b border-slate-200 dark:border-slate-800/60 px-8 py-4 flex items-center justify-between shadow-sm">
      <h1 className="text-xl font-semibold bg-clip-text text-transparent bg-gradient-to-r from-slate-800 to-slate-500 dark:from-white dark:to-slate-400">
        {title}
      </h1>
      
      <div className="flex items-center gap-5">
        <button className="relative p-2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors rounded-full hover:bg-slate-100 dark:hover:bg-slate-800">
          <Bell size={20} />
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 border-2 border-white dark:border-slate-900"></span>
        </button>
        
        <div className="h-8 w-[1px] bg-slate-200 dark:bg-slate-800/60"></div>
        
        <button className="flex items-center gap-3 hover:opacity-80 transition-opacity">
          <div className="text-right hidden sm:block">
            <div className="text-sm font-semibold text-slate-700 dark:text-slate-200">Admin User</div>
            <div className="text-xs text-slate-500 dark:text-slate-400">Quản trị viên</div>
          </div>
          <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-500 to-indigo-500 flex items-center justify-center text-white shadow-md border-2 border-white dark:border-slate-800">
            <User size={18} />
          </div>
        </button>
      </div>
    </header>
  );
}
