import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import Loading from '../components/ui/Loading';
import styles from './PreciosML.module.css';

export default function PreciosML() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [consultando, setConsultando] = useState(null);
  const [historialModal, setHistorialModal] = useState(null);
  const [historialData, setHistorialData] = useState(null);
  const [loadingHistorial, setLoadingHistorial] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await api.get('/ml/compras-con-ml');
      setData(result);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  async function consultarML(row) {
    setConsultando(row.compraId);
    try {
      const result = await api.post(`/compras/${row.compraId}/consultar-precio-ml`);
      alert(`Precio de referencia ML: $${result.precio.toLocaleString('es-AR', { minimumFractionDigits: 2 })} (${new Date(result.fechaHora).toLocaleString()})`);
      fetchData();
    } catch (err) {
      alert(err.message);
    } finally {
      setConsultando(null);
    }
  }

  async function openHistorial(row) {
    setHistorialModal(row);
    setLoadingHistorial(true);
    setHistorialData(null);
    try {
      const result = await api.get(`/compras/${row.compraId}/consultas-ml`);
      setHistorialData(result);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoadingHistorial(false);
    }
  }

  const columns = [
    { key: 'materiaPrimaNombre', label: 'Materia Prima' },
    { key: 'fechaCompra', label: 'Fecha Compra' },
    {
      key: 'precioCompra',
      label: 'Precio Compra',
      render: (r) => `$${r.precioCompra.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    { key: 'lugar', label: 'Proveedor', render: (r) => r.lugar ?? '—' },
    { key: 'mlId', label: 'ML ID' },
    {
      key: 'ultimoPrecioML',
      label: 'Últ. Precio ML',
      render: (r) => r.ultimoPrecioML != null
        ? `$${r.ultimoPrecioML.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`
        : '—',
    },
    {
      key: 'ultimaConsultaML',
      label: 'Últ. Consulta',
      render: (r) => r.ultimaConsultaML ? new Date(r.ultimaConsultaML).toLocaleString() : '—',
    },
    {
      key: 'diferencia',
      label: 'Diferencia',
      render: (r) => {
        if (r.ultimoPrecioML == null) return '—';
        const diff = r.ultimoPrecioML - r.precioCompra;
        const pct = (diff / r.precioCompra * 100).toFixed(1);
        const cls = diff > 0 ? styles.positivo : diff < 0 ? styles.negativo : '';
        return <span className={cls}>{diff > 0 ? '+' : ''}{pct}%</span>;
      },
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => consultarML(row)} disabled={consultando === row.compraId}>
            {consultando === row.compraId ? 'Consultando...' : 'Consultar'}
          </Button>
          <Button variant="ghost" onClick={() => openHistorial(row)}>Historial</Button>
        </div>
      ),
    },
  ];

  if (loading) return <Loading />;
  if (error) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Precios Mercado Libre</h1>
        <Button variant="ghost" onClick={fetchData}>Actualizar</Button>
      </div>

      <p className={styles.hint}>
        Compras con ID de Mercado Libre asociado. Consultá el precio de referencia actual y comparalo con el precio de compra original.
      </p>

      <Table columns={columns} data={data ?? []} emptyMessage="No hay compras con ID de Mercado Libre asociado." />

      <Modal isOpen={!!historialModal} onClose={() => { setHistorialModal(null); setHistorialData(null); }}
        title={`Historial ML - ${historialModal?.materiaPrimaNombre ?? ''}`}>
        {loadingHistorial ? <Loading /> : (
          historialData && historialData.length > 0 ? (
            <div className={styles.historialList}>
              {historialData.map((c) => (
                <div key={c.id} className={styles.historialItem}>
                  <span className={styles.historialFecha}>{new Date(c.fechaHora).toLocaleString()}</span>
                  <span className={styles.historialPrecio}>${c.precio.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</span>
                </div>
              ))}
            </div>
          ) : (
            <p>No hay consultas previas para esta compra.</p>
          )
        )}
      </Modal>
    </div>
  );
}
