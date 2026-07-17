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
import styles from './MateriasPrimas.module.css';

const emptyForm = { nombre: '', unidadMedida: '', stockMinimo: '' };

export default function MateriasPrimas() {
  const { data, loading, error, refetch } = useApi('/materias-primas');
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

  function openEdit(mp) {
    setEditing(mp);
    setForm({ nombre: mp.nombre, unidadMedida: mp.unidadMedida, stockMinimo: mp.stockMinimo ?? '' });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.nombre.trim() || !form.unidadMedida.trim()) return;
    setSaving(true);
    try {
      const body = {
        nombre: form.nombre.trim(),
        unidadMedida: form.unidadMedida.trim(),
        stockMinimo: form.stockMinimo === '' ? null : Number(form.stockMinimo),
      };
      if (editing) {
        await api.put(`/materias-primas/${editing.id}`, body);
      } else {
        await api.post('/materias-primas', body);
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
      await api.delete(`/materias-primas/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'nombre', label: 'Nombre' },
    { key: 'unidadMedida', label: 'Unidad de Medida' },
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
        <h1 className={styles.pageTitle}>Materias Primas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'nombre', label: 'Nombre' },
            { key: 'unidadMedida', label: 'Unidad de Medida' },
            { key: 'stockActual', label: 'Stock Actual' },
            { key: 'stockMinimo', label: 'Stock Mínimo' },
          ], 'materias-primas.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nueva Materia Prima</Button>
        </div>
      </div>

      <Table columns={columns} data={data} />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Materia Prima' : 'Nueva Materia Prima'}>
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Nombre">
            <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required autoFocus />
          </FormField>
          <FormField label="Unidad de Medida">
            <select value={form.unidadMedida} onChange={(e) => setForm({ ...form, unidadMedida: e.target.value })} required>
              <option value="">Seleccionar...</option>
              <option value="gramos">gramos</option>
              <option value="kg">kg</option>
              <option value="ml">ml</option>
              <option value="litros">litros</option>
              <option value="mg">mg</option>
              <option value="unidades">unidades</option>
              <option value="unidad">unidad</option>
              <option value="cucharadas">cucharadas</option>
              <option value="gotas">gotas</option>
            </select>
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
        title="Eliminar Materia Prima"
        message={`¿Eliminar "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
      />
    </div>
  );
}
