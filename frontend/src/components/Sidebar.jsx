import { NavLink } from 'react-router-dom';
import styles from './Sidebar.module.css';

const links = [
  { to: '/dashboard', label: 'Resumen', icon: '📊' },
  { to: '/materias-primas', label: 'Materias Primas', icon: '🧪' },
  { to: '/categorias-mp', label: 'Categorías MP', icon: '📁' },
  { to: '/compras', label: 'Compras', icon: '🛒' },
  { to: '/productos-terminados', label: 'Productos Terminados', icon: '📦' },
  { to: '/categorias-pt', label: 'Categorías PT', icon: '📂' },
  { to: '/recetas', label: 'Recetas', icon: '📝' },
  { to: '/produccion', label: 'Producción', icon: '⚙️' },
  { to: '/ventas', label: 'Ventas', icon: '💰' },
];

export default function Sidebar({ onNavigate }) {
  return (
    <nav className={styles.sidebar}>
      <h1 className={styles.title}>LuAdministra</h1>
      {links.map((l) => (
        <NavLink
          key={l.to}
          to={l.to}
          end={l.to === '/dashboard'}
          className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
          onClick={onNavigate}
        >
          <span className={styles.linkIcon}>{l.icon}</span>
          {l.label}
        </NavLink>
      ))}
    </nav>
  );
}
