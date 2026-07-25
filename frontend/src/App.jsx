import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import MateriasPrimas from './pages/MateriasPrimas';
import Compras from './pages/Compras';
import Categorias from './pages/Categorias';
import Productos from './pages/Productos';
import Recetas from './pages/Recetas';
import Lotes from './pages/Lotes';
import Ventas from './pages/Ventas';
import Consignatarios from './pages/Consignatarios';
import Consignaciones from './pages/Consignaciones';


export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/materias-primas" element={<MateriasPrimas />} />
        <Route path="/categorias" element={<Categorias />} />
        <Route path="/compras" element={<Compras />} />
        <Route path="/productos" element={<Productos />} />
        <Route path="/recetas" element={<Recetas />} />
        <Route path="/lotes" element={<Lotes />} />
        <Route path="/ventas" element={<Ventas />} />
        <Route path="/consignatarios" element={<Consignatarios />} />
        <Route path="/consignaciones" element={<Consignaciones />} />

      </Route>
    </Routes>
  );
}
