import { useState, useRef, useEffect, useLayoutEffect } from 'react';
import styles from './ActionMenu.module.css';

export default function ActionMenu({ actions }) {
  const [open, setOpen] = useState(false);
  const [pos, setPos] = useState({ top: 0, left: 0 });
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

  useLayoutEffect(() => {
    if (!open || !triggerRef.current || !menuRef.current) return;
    const trigger = triggerRef.current.getBoundingClientRect();
    const menu = menuRef.current.getBoundingClientRect();
    const margin = 8;
    const viewportW = window.innerWidth;
    const viewportH = window.innerHeight;

    let top = trigger.bottom;
    let left = trigger.left;

    if (top + menu.height > viewportH - margin) {
      top = trigger.top - menu.height;
    }
    if (left + menu.width > viewportW - margin) {
      left = viewportW - menu.width - margin;
    }
    setPos({
      top: Math.max(margin, top),
      left: Math.max(margin, left),
    });
  }, [open]);

  if (!actions || actions.length === 0) return null;

  return (
    <div className={styles.wrapper}>
      <button
        ref={triggerRef}
        className={styles.trigger}
        onClick={() => setOpen((o) => !o)}
        aria-label="Acciones"
      >
        ⋮
      </button>
      {open && (
        <div className={styles.menu} ref={menuRef} style={{ top: pos.top, left: pos.left }}>
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
