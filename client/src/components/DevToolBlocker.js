'use client';

import { useEffect } from 'react';

export default function DevToolBlocker() {
  useEffect(() => {
    // Chặn chuột phải (Context Menu)
    const handleContextMenu = (e) => {
      e.preventDefault();
    };

    // Chặn các phím tắt mở DevTools
    const handleKeyDown = (e) => {
      // F12
      if (e.key === 'F12') {
        e.preventDefault();
      }
      
      // Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+U
      if (
        e.ctrlKey &&
        (e.key === 'I' || e.key === 'i' || e.key === 'J' || e.key === 'j' || e.key === 'U' || e.key === 'u')
      ) {
        e.preventDefault();
      }

      // Ctrl+Shift+C
      if (e.ctrlKey && e.shiftKey && (e.key === 'C' || e.key === 'c')) {
        e.preventDefault();
      }
    };

    window.addEventListener('contextmenu', handleContextMenu);
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      window.removeEventListener('contextmenu', handleContextMenu);
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  return null;
}
