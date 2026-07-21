import { useState } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
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

const emptyForm = { productoTerminadoId: '', fecha: todayStr(), cantidad: '' };

export default function Ventas() {
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');
  const [desde, setDesde] = useState(monthAgo());
  const [hasta, setHasta] = useState(todayStr());
  const [usarPeriodo, setUsarPeriodo] = useState(false);
  const apiPath = usarPeriodo
    ? `/ventas/periodo?desde=${desde}&hasta=${hasta}`
    : `/ventas?sortBy=${sortBy}&sortDir=${sortDir}`;
  const { data, loading, error, refetch } = useApi(apiPath);
  const { data: ptList } = useApi('/productos-terminados');

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  function openCreate() {
    setForm(emptyForm);
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.productoTerminadoId || !form.cantidad) return;
    setSaving(true);
    try {
      await api.post('/ventas', {
        productoTerminadoId: Number(form.productoTerminadoId),
        fecha: form.fecha,
        cantidad: Number(form.cantidad),
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
    { key: 'productoTerminadoNombre', label: 'Producto Terminado' },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'cantidad',
      label: 'Cantidad',
      render: (r) => `${r.cantidad} u`,
    },
    {
      key: 'precioUnitario',
      label: 'Precio Unit.',
      render: (r) => `$${r.precioUnitario.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    {
      key: 'total',
      label: 'Total',
      render: (r) => `$${(r.cantidad * r.precioUnitario).toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <Button variant="ghost" onClick={() => setDeleteTarget(row)}>Eliminar</Button>
      ),
    },
  ];

  if (loading && !data) return <Loading />;
  if (error) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Ventas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'productoTerminadoNombre', label: 'Producto Terminado' },
            { key: 'fecha', label: 'Fecha' },
            { key: 'cantidad', label: 'Cantidad Vendida' },
            { key: 'precioUnitario', label: 'Precio Unitario' },
          ], 'ventas.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Registrar Venta</Button>
        </div>
      </div>

      <p className={styles.hint}>Al registrar una venta se descuenta automáticamente el stock del producto terminado.</p>

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
              <option value="cantidad">Ordenar por cantidad</option>
            </select>
            <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
              {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
            </Button>
          </>
        )}
      </div>

      <Table columns={columns} data={data} emptyMessage="No hay ventas registradas." />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Registrar Venta">
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Producto Terminado">
            <select value={form.productoTerminadoId} onChange={(e) => setForm({ ...form, productoTerminadoId: e.target.value })} required>
              <option value="">Seleccionar...</option>
              {ptList?.map((pt) => (
                <option key={pt.id} value={pt.id}>{pt.nombre} (stock: {pt.stockActual} u)</option>
              ))}
            </select>
          </FormField>
          <FormField label="Fecha">
            <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
          </FormField>
          <FormField label="Cantidad Vendida">
            <input type="number" step="any" min="0" value={form.cantidad} onChange={(e) => setForm({ ...form, cantidad: e.target.value })} required />
          </FormField>
          <div className={styles.formActions}>
            <Button variant="ghost" type="button" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Eliminar Venta"
        message={`¿Eliminar la venta de "${deleteTarget?.productoTerminadoNombre}" del ${deleteTarget?.fecha}? Se revertirá el stock.`}
      />
    </div>
  );
}
