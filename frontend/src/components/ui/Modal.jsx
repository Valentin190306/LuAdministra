import { useEffect } from 'react';
import styles from './Modal.module.css';

export default function Modal({ isOpen, onClose, title, children }) {
  useEffect(() => {
    if (!isOpen) return;
    const handler = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>{title}</h2>
          <button className={styles.close} onClick={onClose} aria-label="Cerrar">&times;</button>
        </div>
        <div className={styles.body}>
          {children}
        </div>
      </div>
    </div>
  );
}
