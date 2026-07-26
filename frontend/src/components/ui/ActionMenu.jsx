import { useState, useRef, useEffect } from 'react';
import styles from './ActionMenu.module.css';

export default function ActionMenu({ actions }) {
  const [open, setOpen] = useState(false);
  const [pos, setPos] = useState({ top: 0, right: 0 });
  const triggerRef = useRef(null);
  const menuRef = useRef(null);

  useEffect(() => {
    if (!open) return;
    const handler = (e) => {
      if (
        menuRef.current && !menuRef.current.contains(e.target) &&
        triggerRef.current && !triggerRef.current.contains(e.target)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    document.addEventListener('touchstart', handler);
    return () => {
      document.removeEventListener('mousedown', handler);
      document.removeEventListener('touchstart', handler);
    };
  }, [open]);

  if (!actions || actions.length === 0) return null;

  const handleOpen = () => {
    const rect = triggerRef.current.getBoundingClientRect();
    setPos({ top: rect.bottom, right: window.innerWidth - rect.right });
    setOpen(true);
  };

  return (
    <div className={styles.wrapper}>
      <button
        ref={triggerRef}
        className={styles.trigger}
        onClick={handleOpen}
        aria-label="Acciones"
      >
        ⋮
      </button>
      {open && (
        <div className={styles.menu} ref={menuRef} style={{ top: pos.top, right: pos.right }}>
          {actions.map((a) => (
            <button
              key={a.label}
              className={styles.menuItem}
              onClick={() => { a.onClick(); setOpen(false); }}
            >
              {a.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
