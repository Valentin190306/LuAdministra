import { useEffect, useState } from 'react';
import { api } from '../api/client';

export default function Dashboard() {
  const [data, setData] = useState(null);

  useEffect(() => {
    api.get('/dashboard/stock').then(setData).catch(console.error);
  }, []);

  if (!data) return <p>Cargando...</p>;

  return (
    <div>
      <h1>Dashboard</h1>

      <h2>Alertas de stock mínimo</h2>
      {data.alertasMP.length === 0 && data.alertasPT.length === 0 && <p>Sin alertas</p>}
      {data.alertasMP.map((mp) => (
        <p key={mp.id} style={{ color: 'red' }}>MP: {mp.nombre} — stock actual {mp.stockActual} {mp.unidadMedida}</p>
      ))}
      {data.alertasPT.map((pt) => (
        <p key={pt.id} style={{ color: 'red' }}>PT: {pt.nombre} — stock actual {pt.stockActual}</p>
      ))}
    </div>
  );
}
