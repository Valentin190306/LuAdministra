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
import styles from './ProductosTerminados.module.css';

const emptyForm = { nombre: '', precioVenta: '', stockMinimo: '' };

export default function ProductosTerminados() {
  const { data, loading, error, refetch } = useApi('/productos-terminados');
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  function openCreate() {
    setEditing(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(pt) {
    setEditing(pt);
    setForm({ nombre: pt.nombre, precioVenta: String(pt.precioVenta), stockMinimo: pt.stockMinimo ?? '' });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.nombre.trim() || !form.precioVenta) return;
    setSaving(true);
    try {
      const body = {
        nombre: form.nombre.trim(),
        precioVenta: Number(form.precioVenta),
        stockMinimo: form.stockMinimo === '' ? null : Number(form.stockMinimo),
      };
      if (editing) {
        await api.put(`/productos-terminados/${editing.id}`, body);
      } else {
        await api.post('/productos-terminados', body);
      }
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
      await api.delete(`/productos-terminados/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'nombre', label: 'Nombre' },
    {
      key: 'precioVenta',
      label: 'Precio de Venta',
      render: (r) => `$${r.precioVenta.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    {
      key: 'stockActual',
      label: 'Stock Actual',
      render: (row) => (
        <span className={row.stockMinimo != null && row.stockActual < row.stockMinimo ? styles.lowStock : undefined}>
          {row.stockActual}
        </span>
      ),
    },
    { key: 'stockMinimo', label: 'Stock Mínimo', render: (r) => r.stockMinimo ?? '—' },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => openEdit(row)}>Editar</Button>
          <Button variant="ghost" onClick={() => setDeleteTarget(row)}>Eliminar</Button>
        </div>
      ),
    },
  ];

  if (loading) return <Loading />;
  if (error) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Productos Terminados</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'nombre', label: 'Nombre' },
            { key: 'precioVenta', label: 'Precio de Venta' },
            { key: 'stockActual', label: 'Stock Actual' },
            { key: 'stockMinimo', label: 'Stock Mínimo' },
          ], 'productos-terminados.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nuevo Producto</Button>
        </div>
      </div>

      <Table columns={columns} data={data} />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Producto Terminado' : 'Nuevo Producto Terminado'}>
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Nombre / Variante">
            <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required autoFocus />
          </FormField>
          <FormField label="Precio de Venta ($)">
            <input type="number" step="any" min="0" value={form.precioVenta} onChange={(e) => setForm({ ...form, precioVenta: e.target.value })} required />
          </FormField>
          <FormField label="Stock Mínimo (opcional)">
            <input type="number" step="any" min="0" value={form.stockMinimo} onChange={(e) => setForm({ ...form, stockMinimo: e.target.value })} />
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
        title="Eliminar Producto Terminado"
        message={`¿Eliminar "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
      />
    </div>
  );
}
