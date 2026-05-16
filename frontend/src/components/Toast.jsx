import { useEffect, useState } from 'react';

let _setToast = null;

export function showToast(msg, type = 'info') {
  _setToast?.({ msg, type, key: Date.now() });
}

export function Toast() {
  const [toast, setToast] = useState(null);

  useEffect(() => {
    _setToast = setToast;
    return () => { _setToast = null; };
  }, []);

  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToast(null), 3500);
    return () => clearTimeout(id);
  }, [toast]);

  if (!toast) return null;

  return (
    <div className={`toast toast-${toast.type}`} key={toast.key}>
      {toast.msg}
      <button className="toast-close" onClick={() => setToast(null)} aria-label="关闭">×</button>
    </div>
  );
}
