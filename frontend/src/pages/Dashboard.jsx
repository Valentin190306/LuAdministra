import { useEffect, useState } from 'react';
import { api } from '../api/client';
import Loading from '../components/ui/Loading';
import Button from '../components/ui/Button';
import { downloadCSV } from '../utils/csv';
import styles from './Dashboard.module.css';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    api.get('/dashboard/stock')
      .then(setData)
      .catch(setError);
  }, []);

  if (error) return <p className={styles.errorMsg}>Error: {error.message}</p>;
  if (!data) return <Loading />;

  const { materiasPrimas, productos, alertasMP, alertasProductos, alertasVencimiento } = data;
  const productosTerminados = productos ?? [];

  const handleExportMP = async () => {
    setExporting(true);
    try {
      await downloadCSV(materiasPrimas, [
        { key: 'nombre', label: 'Nombre' },
        { key: 'unidadMedida', label: 'Unidad' },
        { key: 'stockActual', label: 'Stock Actual' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ], 'materias-primas.csv');
    } finally {
      setExporting(false);
    }
  };

  const handleExportPT = async () => {
    setExporting(true);
    try {
      await downloadCSV(productos, [
        { key: 'nombre', label: 'Nombre' },
        { key: 'precioVenta', label: 'Precio Venta' },
        { key: 'stockActual', label: 'En Depósito' },
        { key: 'stockConsignado', label: 'Consignado' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ], 'productos.csv');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Resumen</h1>
        <div className={styles.exportBtns}>
          <Button variant="ghost" disabled={exporting} onClick={handleExportMP}>Exportar MP</Button>
          <Button variant="ghost" disabled={exporting} onClick={handleExportPT}>Exportar PT</Button>
        </div>
      </div>

      <div className={styles.cards}>
        <div className={styles.card}>
          <span className={styles.cardValue}>{materiasPrimas.length}</span>
          <span className={styles.cardLabel}>Materias Primas</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardValue}>{productos.length}</span>
          <span className={styles.cardLabel}>Productos</span>
        </div>
        <div className={`${styles.card} ${(alertasMP.length > 0 || alertasProductos.length > 0 || (alertasVencimiento?.length ?? 0) > 0) ? styles.cardAlert : ''}`}>
          <span className={styles.cardValue}>{alertasMP.length + alertasProductos.length + (alertasVencimiento?.length ?? 0)}</span>
          <span className={styles.cardLabel}>Alertas</span>
        </div>
      </div>

      {(alertasMP.length > 0 || alertasProductos.length > 0) && (
        <section className={styles.alerts}>
          <h2 className={styles.sectionTitle}>Alertas de stock mínimo</h2>
          {alertasMP.map((mp) => (
            <div key={mp.id} className={styles.alert}>
              <span className={styles.alertType}>MP</span>
              <span>{mp.nombre}</span>
              <span className={styles.alertStock}>{mp.stockActual} {mp.unidadMedida}</span>
              <span className={styles.alertMin}>(mín: {mp.stockMinimo})</span>
            </div>
          ))}
          {alertasProductos.map((pt) => (
            <div key={pt.id} className={styles.alert}>
              <span className={styles.alertType}>PT</span>
              <span>{pt.nombre}</span>
              <span className={styles.alertStock}>{pt.stockActual - (pt.stockConsignado ?? 0)} u (depósito)</span>
              <span className={styles.alertMin}>(mín: {pt.stockMinimo})</span>
            </div>
          ))}
        </section>
      )}

      {alertasVencimiento && alertasVencimiento.length > 0 && (
        <section className={styles.alerts}>
          <h2 className={styles.sectionTitle}>Alertas de vencimiento</h2>
          {alertasVencimiento.map((av, i) => (
            <div key={i} className={`${styles.alert} ${styles.alertVencido}`}>
              <span className={styles.alertType}>PT</span>
              <span>{av.productoNombre}</span>
              <span className={styles.alertStock}>Vencido: {av.fechaVencimiento}</span>
              <span className={styles.alertMin}>Lote #{av.loteId}</span>
            </div>
          ))}
        </section>
      )}

      <div className={styles.lists}>
        <section>
          <h2 className={styles.sectionTitle}>Materias Primas</h2>
          <div className={styles.miniTableWrapper}>
          <table className={styles.miniTable}>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Stock</th>
                <th>Ud.</th>
                <th>Mínimo</th>
              </tr>
            </thead>
            <tbody>
              {materiasPrimas.map((mp) => (
                <tr key={mp.id} className={mp.stockMinimo !== null && mp.stockMinimo !== undefined && mp.stockActual < mp.stockMinimo ? styles.lowStockRow : undefined}>
                  <td>{mp.nombre}</td>
                  <td>{mp.stockActual}</td>
                  <td>{mp.unidadMedida}</td>
                  <td>{mp.stockMinimo ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          </div>
        </section>

        <section>
          <h2 className={styles.sectionTitle}>Productos</h2>
          <div className={styles.miniTableWrapper}>
          <table className={styles.miniTable}>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>En Depósito</th>
                <th>Consignado</th>
                <th>Precio</th>
                <th>Mínimo</th>
              </tr>
            </thead>
            <tbody>
              {productos.map((pt) => {
                const enDeposito = pt.stockActual - (pt.stockConsignado ?? 0);
                return (
                <tr key={pt.id} className={pt.stockMinimo !== null && pt.stockMinimo !== undefined && enDeposito < pt.stockMinimo ? styles.lowStockRow : undefined}>
                  <td>{pt.nombre}</td>
                  <td>{enDeposito}</td>
                  <td>{pt.stockConsignado ?? 0}</td>
                  <td>${pt.precioVenta.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</td>
                  <td>{pt.stockMinimo ?? '—'}</td>
                </tr>
                );
              })}
            </tbody>
          </table>
          </div>
        </section>
      </div>
    </div>
  );
}
