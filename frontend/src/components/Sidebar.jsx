import { NavLink } from 'react-router-dom';

const links = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/materias-primas', label: 'Materias Primas' },
  { to: '/compras', label: 'Compras' },
  { to: '/productos-terminados', label: 'Productos Terminados' },
  { to: '/recetas', label: 'Recetas' },
  { to: '/produccion', label: 'Producción' },
  { to: '/ventas', label: 'Ventas' },
];

export default function Sidebar() {
  return (
    <nav style={{ width: 220, minHeight: '100vh', borderRight: '1px solid #ccc', padding: '1rem' }}>
      <h2>LuAdministra</h2>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {links.map((l) => (
          <li key={l.to} style={{ marginBottom: '0.5rem' }}>
            <NavLink
              to={l.to}
              style={({ isActive }) => ({ fontWeight: isActive ? 'bold' : 'normal' })}
            >
              {l.label}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
