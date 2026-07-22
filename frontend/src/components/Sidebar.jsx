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
      { to: '/productos-terminados', label: 'Productos', icon: '📦' },
    ],
  },
  {
    label: 'Operaciones',
    links: [
      { to: '/compras', label: 'Compras', icon: '🛒' },
      { to: '/recetas', label: 'Recetas', icon: '📝' },
      { to: '/produccion', label: 'Producción', icon: '⚙️' },
      { to: '/ventas', label: 'Ventas', icon: '💰' },
      { to: '/despachos', label: 'Despachos', icon: '📤' },
    ],
  },
  {
    label: 'Configuración',
    links: [
      { to: '/categorias-mp', label: 'Categorías MP', icon: '📁' },
      { to: '/categorias-pt', label: 'Categorías PT', icon: '📂' },
      { to: '/colaboradoras', label: 'Colaboradoras', icon: '👤' },
    ],
  },
];

export default function Sidebar({ onNavigate }) {
  return (
    <nav className={styles.sidebar}>
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
