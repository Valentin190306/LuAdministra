import { useEffect, useState } from 'react';
import { api } from '../api/client';
import Loading from '../components/ui/Loading';
import styles from './Dashboard.module.css';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.get('/dashboard/stock')
      .then(setData)
      .catch(setError);
  }, []);

  if (error) return <p className={styles.errorMsg}>Error: {error.message}</p>;
  if (!data) return <Loading />;

  const { materiasPrimas, productosTerminados, alertasMP, alertasPT } = data;

  return (
    <div>
      <h1 className={styles.pageTitle}>Dashboard</h1>

      <div className={styles.cards}>
        <div className={styles.card}>
          <span className={styles.cardValue}>{materiasPrimas.length}</span>
          <span className={styles.cardLabel}>Materias Primas</span>
        </div>
        <div className={styles.card}>
          <span className={styles.cardValue}>{productosTerminados.length}</span>
          <span className={styles.cardLabel}>Productos Terminados</span>
        </div>
        <div className={`${styles.card} ${alertasMP.length > 0 || alertasPT.length > 0 ? styles.cardAlert : ''}`}>
          <span className={styles.cardValue}>{alertasMP.length + alertasPT.length}</span>
          <span className={styles.cardLabel}>Alertas de Stock</span>
        </div>
      </div>

      {(alertasMP.length > 0 || alertasPT.length > 0) && (
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
          {alertasPT.map((pt) => (
            <div key={pt.id} className={styles.alert}>
              <span className={styles.alertType}>PT</span>
              <span>{pt.nombre}</span>
              <span className={styles.alertStock}>{pt.stockActual} u</span>
              <span className={styles.alertMin}>(mín: {pt.stockMinimo})</span>
            </div>
          ))}
        </section>
      )}

      <div className={styles.lists}>
        <section>
          <h2 className={styles.sectionTitle}>Materias Primas</h2>
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
                <tr key={mp.id} className={mp.stockMinimo != null && mp.stockActual < mp.stockMinimo ? styles.lowStockRow : undefined}>
                  <td>{mp.nombre}</td>
                  <td>{mp.stockActual}</td>
                  <td>{mp.unidadMedida}</td>
                  <td>{mp.stockMinimo ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>

        <section>
          <h2 className={styles.sectionTitle}>Productos Terminados</h2>
          <table className={styles.miniTable}>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Stock</th>
                <th>Precio</th>
                <th>Mínimo</th>
              </tr>
            </thead>
            <tbody>
              {productosTerminados.map((pt) => (
                <tr key={pt.id} className={pt.stockMinimo != null && pt.stockActual < pt.stockMinimo ? styles.lowStockRow : undefined}>
                  <td>{pt.nombre}</td>
                  <td>{pt.stockActual}</td>
                  <td>${pt.precioVenta.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</td>
                  <td>{pt.stockMinimo ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  );
}
