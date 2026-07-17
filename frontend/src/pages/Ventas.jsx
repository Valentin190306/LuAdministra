import { useState } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import styles from './Ventas.module.css';

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

const emptyForm = { productoTerminadoId: '', fecha: todayStr(), cantidad: '' };

export default function Ventas() {
  const { data, loading, error, refetch } = useApi('/ventas');
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
        <Button onClick={openCreate}>Registrar Venta</Button>
      </div>

      <p className={styles.hint}>Al registrar una venta se descuenta automáticamente el stock del producto terminado.</p>

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
