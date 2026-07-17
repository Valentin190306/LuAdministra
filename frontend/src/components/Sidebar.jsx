import { NavLink } from 'react-router-dom';
import styles from './Sidebar.module.css';

const links = [
  { to: '/dashboard', label: 'Resumen' },
  { to: '/materias-primas', label: 'Materias Primas' },
  { to: '/compras', label: 'Compras' },
  { to: '/productos-terminados', label: 'Productos Terminados' },
  { to: '/recetas', label: 'Recetas' },
  { to: '/produccion', label: 'Producción' },
  { to: '/ventas', label: 'Ventas' },
];

export default function Sidebar() {
  return (
    <nav className={styles.sidebar}>
      <h1 className={styles.title}>LuAdministra</h1>
      {links.map((l) => (
        <NavLink
          key={l.to}
          to={l.to}
          end={l.to === '/dashboard'}
          className={({ isActive }) => `${styles.link} ${isActive ? styles.active : ''}`}
        >
          {l.label}
        </NavLink>
      ))}
    </nav>
  );
}
