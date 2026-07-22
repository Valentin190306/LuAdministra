import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import MateriasPrimas from './pages/MateriasPrimas';
import Compras from './pages/Compras';
import CategoriasMP from './pages/CategoriasMP';
import ProductosTerminados from './pages/ProductosTerminados';
import CategoriasPT from './pages/CategoriasPT';
import Recetas from './pages/Recetas';
import Produccion from './pages/Produccion';
import Ventas from './pages/Ventas';
import Colaboradoras from './pages/Colaboradoras';
import Despachos from './pages/Despachos';


export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/materias-primas" element={<MateriasPrimas />} />
        <Route path="/categorias-mp" element={<CategoriasMP />} />
        <Route path="/compras" element={<Compras />} />
        <Route path="/productos-terminados" element={<ProductosTerminados />} />
        <Route path="/categorias-pt" element={<CategoriasPT />} />
        <Route path="/recetas" element={<Recetas />} />
        <Route path="/produccion" element={<Produccion />} />
        <Route path="/ventas" element={<Ventas />} />
        <Route path="/colaboradoras" element={<Colaboradoras />} />
        <Route path="/despachos" element={<Despachos />} />

      </Route>
    </Routes>
  );
}
