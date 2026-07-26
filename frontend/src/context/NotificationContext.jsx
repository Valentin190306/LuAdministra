import { createContext, useContext, useState, useCallback } from 'react';
import Toast from '../components/ui/Toast';
import styles from '../components/ui/Toast.module.css';

const NotificationContext = createContext(null);

export function useNotify() {
  return useContext(NotificationContext);
}

export function NotificationProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const notify = useCallback((err, type) => {
    let mensaje = typeof err === 'string' ? err : 'Error inesperado';
    let effectiveType = type || 'error';
    try {
      if (typeof err === 'object' && err?.message) {
        const parsed = JSON.parse(err.message);
        mensaje = parsed.mensaje || parsed.message || err.message;
        effectiveType = type || (parsed.tipo === 'VALIDACION' ? 'warning' : 'error');
      } else if (typeof err === 'object' && err?.message) {
        mensaje = err.message;
      }
    } catch {
      mensaje = typeof err === 'object' && err?.message ? err.message : String(err);
    }
    addToast(mensaje, effectiveType);
  }, [addToast]);

  const notifySuccess = useCallback((msg) => addToast(msg, 'success'), [addToast]);

  return (
    <NotificationContext.Provider value={{ notify, notifySuccess, addToast }}>
      {children}
      <div className={styles.container}>
        {toasts.map((t) => (
          <Toast key={t.id} message={t.message} type={t.type} onClose={() => removeToast(t.id)} />
        ))}
      </div>
    </NotificationContext.Provider>
  );
}
