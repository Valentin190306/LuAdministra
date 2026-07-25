import { NavLink } from 'react-router-dom';
import styles from './Sidebar.module.css';

const sections = [
  {
    label: null,
    links: [
      { to: '/dashboard', label: 'Inicio', icon: '📊' },
    ],
  },
  {
    label: 'Stock',
    links: [
      { to: '/materias-primas', label: 'Materias Primas', icon: '🧪' },
      { to: '/productos', label: 'Productos', icon: '📦' },
    ],
  },
  {
    label: 'Operaciones',
    links: [
      { to: '/compras', label: 'Compras', icon: '🛒' },
      { to: '/recetas', label: 'Recetas', icon: '📝' },
      { to: '/lotes', label: 'Lotes', icon: '⚙️' },
      { to: '/ventas', label: 'Ventas', icon: '💰' },
      { to: '/consignaciones', label: 'Consignaciones', icon: '📤' },
    ],
  },
  {
    label: 'Configuración',
    links: [
      { to: '/categorias', label: 'Categorías', icon: '📁' },
      { to: '/consignatarios', label: 'Consignatarios', icon: '👤' },
    ],
  },
];

export default function Sidebar({ onNavigate }) {
  return (
    <nav className={styles.sidebar}>
      <div className={styles.logo}>
        <img src="/logo.png" alt="Casa Del Sol" className={styles.logoImg} />
      </div>
      {sections.map((section, i) => (
        <div key={i} className={styles.section}>
          {section.label && (
            <span className={styles.sectionLabel}>{section.label}</span>
          )}
          {section.links.map((l) => (
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
        </div>
      ))}
    </nav>
  );
}
