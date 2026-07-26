import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import { NotificationProvider } from '../context/NotificationContext';
import styles from './Layout.module.css';

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <NotificationProvider>
      <div className={styles.layout}>
        <button className={styles.hamburger} onClick={() => setSidebarOpen(true)} aria-label="Abrir menú">
          <span className={styles.hamburgerLine} />
          <span className={styles.hamburgerLine} />
          <span className={styles.hamburgerLine} />
        </button>

        {sidebarOpen && <div className={styles.overlay} onClick={() => setSidebarOpen(false)} />}

        <div className={`${styles.sidebarWrapper} ${sidebarOpen ? styles.sidebarOpen : ''}`}>
          <Sidebar onNavigate={() => setSidebarOpen(false)} />
        </div>

        <main className={styles.main}>
          <Outlet />
        </main>
      </div>
    </NotificationProvider>
  );
}
