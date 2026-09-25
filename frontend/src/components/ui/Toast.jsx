import { useEffect } from 'react';
import styles from './Toast.module.css';

const DURATIONS = { success: 5000, info: 5000, warning: 6000, error: 8000 };

export default function Toast({ message, type = 'info', onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, DURATIONS[type] ?? 6000);
    return () => clearTimeout(timer);
  }, [onClose, type]);

  return (
    <div className={`${styles.toast} ${styles[type]}`}>
      <span>{message}</span>
      <button className={styles.close} onClick={onClose}>&times;</button>
    </div>
  );
}
