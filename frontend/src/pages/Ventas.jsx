import { useState, useCallback } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Ventas.module.css';

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function monthAgo() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
}

export default function Ventas() {
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');
  const [desde, setDesde] = useState(monthAgo());
  const [hasta, setHasta] = useState(todayStr());
  const [usarPeriodo, setUsarPeriodo] = useState(false);
  const { data: ptList } = useApi('/productos');

  const buildUrl = useCallback((page, size) => {
    if (usarPeriodo) {
      return `/ventas/periodo?desde=${desde}&hasta=${hasta}&page=${page}&size=${size}`;
    }
    return `/ventas?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
  }, [sortBy, sortDir, usarPeriodo, desde, hasta]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);

  const [modalOpen, setModalOpen] = useState(false);
  const [fecha, setFecha] = useState(todayStr());
  const [lineas, setLineas] = useState([{ productoId: '', cantidad: '' }]);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [detailVenta, setDetailVenta] = useState(null);

  function openCreate() {
    setFecha(todayStr());
    setLineas([{ productoId: '', cantidad: '' }]);
    setModalOpen(true);
  }

  function addLinea() {
    setLineas((prev) => [...prev, { productoId: '', cantidad: '' }]);
  }

  function updateLinea(index, field, value) {
    setLineas((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  }

  function removeLinea(index) {
    setLineas((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSave(e) {
    e.preventDefault();
    const valid = lineas.filter((l) => l.productoId && l.cantidad);
    if (valid.length === 0) return;
    setSaving(true);
    try {
      await api.post('/ventas', {
        fecha,
        lineas: valid.map((l) => ({
          productoId: Number(l.productoId),
          cantidad: Number(l.cantidad),
        })),
      });
      setModalOpen(false);
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await api.delete(`/ventas/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'cantidadDeProductos',
      label: 'Cant. Productos',
      render: (r) => `${r.lineas.length} u`,
    },
    {
      key: 'total',
      label: 'Total',
      render: (r) => `$${Number(r.total).toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <ActionMenu actions={[
          { label: 'Ver detalle', onClick: () => setDetailVenta(row) },
          { label: 'Eliminar', onClick: () => setDeleteTarget(row) },
        ]} />
      ),
    },
  ];

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Ventas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data?.map((v) => ({
            id: v.id,
            fecha: v.fecha,
            cantidadDeProductos: v.lineas.length,
            total: v.total,
          })) ?? [], [
            { key: 'id', label: 'ID' },
            { key: 'fecha', label: 'Fecha' },
            { key: 'cantidadDeProductos', label: 'Cant. Productos' },
            { key: 'total', label: 'Total' },
          ], 'ventas.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Registrar Venta</Button>
        </div>
      </div>

      <p className={styles.hint}>Al registrar una venta se descuenta automáticamente el stock de cada producto.</p>

      <div className={styles.filters}>
        <label className={styles.filterLabel}>
          <input type="checkbox" checked={usarPeriodo} onChange={(e) => setUsarPeriodo(e.target.checked)} />
          Filtrar por período
        </label>
        {usarPeriodo && (
          <>
            <label className={styles.filterLabel}>
              Desde:
              <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} className={styles.filterSelect} />
            </label>
            <label className={styles.filterLabel}>
              Hasta:
              <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} className={styles.filterSelect} />
            </label>
          </>
        )}
        {!usarPeriodo && (
          <>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="fecha">Ordenar por fecha</option>
            </select>
            <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
              {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
            </Button>
          </>
        )}
      </div>

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table
        columns={columns}
        data={data ?? []}
        sentinelRef={sentinelRef}
        emptyMessage="No hay ventas registradas."
      />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Registrar Venta">
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Fecha">
            <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} required />
          </FormField>

          <div className={styles.lineasContainer}>
            {lineas.map((l, i) => (
              <div key={i} className={styles.lineaRow}>
                <FormField label={i === 0 ? 'Producto Terminado' : undefined}>
                  <select value={l.productoId} onChange={(e) => updateLinea(i, 'productoId', e.target.value)} required>
                    <option value="">Seleccionar...</option>
                    {ptList?.map((pt) => (
                      <option key={pt.id} value={pt.id}>{pt.nombre} (stock: {pt.stockActual} u)</option>
                    ))}
                  </select>
                </FormField>
                <FormField label={i === 0 ? 'Cantidad' : undefined}>
                  <div className={styles.cantidadRow}>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      value={l.cantidad}
                      onChange={(e) => updateLinea(i, 'cantidad', e.target.value)}
                      required
                    />
                    {lineas.length > 1 && (
                      <button type="button" className={styles.removeBtn} onClick={() => removeLinea(i)} aria-label="Eliminar">&times;</button>
                    )}
                  </div>
                </FormField>
              </div>
            ))}
          </div>

          <Button variant="ghost" type="button" onClick={addLinea}>+ Agregar producto</Button>

          <div className={styles.formActions}>
            <Button variant="ghost" type="button" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={!!detailVenta} onClose={() => setDetailVenta(null)} title={detailVenta ? `Venta del ${detailVenta.fecha}` : ''}>
        <div className={styles.detailContent}>
          <table className={styles.detailTable}>
            <thead>
              <tr>
                <th>Producto Terminado</th>
                <th>Cant.</th>
                <th>P.U.</th>
                <th>Subtotal</th>
              </tr>
            </thead>
            <tbody>
              {detailVenta?.lineas.map((l) => (
                <tr key={l.id}>
                  <td>{l.productoNombre}</td>
                  <td>{l.cantidad} u</td>
                  <td>${Number(l.precioUnitario).toLocaleString('es-AR', { minimumFractionDigits: 2 })}</td>
                  <td>${(l.cantidad * l.precioUnitario).toLocaleString('es-AR', { minimumFractionDigits: 2 })}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className={styles.detailTotal}>Total: ${Number(detailVenta?.total ?? 0).toLocaleString('es-AR', { minimumFractionDigits: 2 })}</p>
          <div className={styles.formActions}>
            <Button variant="danger" onClick={() => {
              setDeleteTarget(detailVenta);
              setDetailVenta(null);
            }}>Eliminar venta</Button>
            <Button variant="ghost" onClick={() => setDetailVenta(null)}>Cerrar</Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Eliminar Venta"
        message={`¿Eliminar la venta del ${deleteTarget?.fecha}? Se revertirá el stock de todos los productos incluidos.`}
      />
    </div>
  );
}
