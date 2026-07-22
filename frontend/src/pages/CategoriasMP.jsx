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
import styles from './CategoriasMP.module.css';

const emptyForm = { nombre: '', categoriaPadreId: '' };

export default function CategoriasMP() {
  const { data, loading, error, refetch } = useApi('/categorias-materias-primas');
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

  function openEdit(cat) {
    setEditing(cat);
    setForm({ nombre: cat.nombre, categoriaPadreId: cat.categoriaPadreId ?? '' });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.nombre.trim()) return;
    setSaving(true);
    try {
      const body = {
        nombre: form.nombre.trim(),
        categoriaPadreId: form.categoriaPadreId === '' ? null : Number(form.categoriaPadreId),
      };
      if (editing) {
        await api.put(`/categorias-materias-primas/${editing.id}`, body);
      } else {
        await api.post('/categorias-materias-primas', body);
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
      await api.delete(`/categorias-materias-primas/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Nombre' },
    { key: 'categoriaPadreNombre', label: 'Categoría Padre', render: (r) => r.categoriaPadreNombre ?? '—' },
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
        <h1 className={styles.pageTitle}>Categorías de Materias Primas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'nombre', label: 'Nombre' },
            { key: 'categoriaPadreNombre', label: 'Categoría Padre' },
          ], 'categorias-materias-primas.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nueva Categoría</Button>
        </div>
      </div>

      <Table columns={columns} data={data} />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Categoría' : 'Nueva Categoría'}>
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Nombre">
            <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required autoFocus />
          </FormField>
          <FormField label="Categoría Padre (opcional)">
            <select value={form.categoriaPadreId} onChange={(e) => setForm({ ...form, categoriaPadreId: e.target.value })}>
              <option value="">Ninguna (categoría raíz)</option>
              {data?.filter((c) => c.id !== editing?.id)?.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
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
        title="Eliminar Categoría"
        message={`¿Eliminar "${deleteTarget?.nombre}"?`}
      />
    </div>
  );
}
