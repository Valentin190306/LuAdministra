import { useState, useEffect } from 'react';
import { api } from '../api/client';
import Modal from '../components/ui/Modal';
import Loading from '../components/ui/Loading';
import styles from './Consignaciones.module.css';

export default function VerRendiciones({ consignacion, onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!consignacion) return;
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const result = await api.get(`/rendiciones/consignacion/${consignacion.id}`);
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
    <Modal isOpen={!!consignacion} onClose={handleClose} title={`Rendiciones - Consignación #${consignacion?.id}`}>
      {loading ? <Loading /> : (
        data && data.length > 0 ? (
          <div className={styles.rendicionesList}>
            {data.map((r) => (
              <div key={r.id} className={styles.rendicionItem}>
                <div className={styles.rendicionHeader}>
                  <span className={styles.rendicionFecha}>{r.fecha}</span>
                  <span>Monto: <strong>${r.montoEntregado.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</strong></span>
                </div>
                <div className={styles.rendicionDetalles}>
                  {r.productos.map((p, i) => (
                    <div key={i} className={styles.rendicionDetalle}>
                      {p.productoNombre}: vendido <strong>{p.cantidadVendida}</strong>, devuelto <strong>{p.cantidadDevuelta}</strong>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p>No hay rendiciones registradas para esta consignación.</p>
        )
      )}
    </Modal>
  );
}
