import { useState, useEffect } from 'react';
import { api } from '../api/client';
import Modal from '../components/ui/Modal';
import Loading from '../components/ui/Loading';
import styles from './Consignaciones.module.css';

export default function StockConsignadoModal({ consignacion, onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!consignacion) return;
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const result = await api.get(`/consignaciones/stock-consignado?consignatarioId=${consignacion.consignatarioId}`);
        if (!cancelled) setData(result);
      } catch {
        // silently fail
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [consignacion]);

  function handleClose() {
    setData(null);
    onClose();
  }

  return (
    <Modal isOpen={!!consignacion} onClose={handleClose} title="Stock en consignación">
      {loading ? <Loading /> : (
        data && data.length > 0 ? (
          <table className={styles.stockTable}>
            <thead>
              <tr>
                <th>Producto</th>
                <th>Consignado</th>
                <th>Rendido</th>
                <th>Pendiente</th>
              </tr>
            </thead>
            <tbody>
              {data.map((s, i) => (
                <tr key={i}>
                  <td>{s.productoNombre}</td>
                  <td>{s.cantidadConsignada}</td>
                  <td>{s.cantidadRendida}</td>
                  <td className={styles.stockPendiente}>{s.cantidadPendiente}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>No hay productos en consignación con esta consignataria.</p>
        )
      )}
    </Modal>
  );
}
