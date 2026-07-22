import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import styles from './Colaboradoras.module.css';

const emptyForm = { nombre: '', contacto: '' };

export default function Colaboradoras() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const result = await api.get('/colaboradoras');
      setData(result);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

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

  function openEdit(c) {
    setEditing(c);
    setForm({ nombre: c.nombre, contacto: c.contacto ?? '' });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.nombre.trim()) return;
    setSaving(true);
    try {
      const body = {
        nombre: form.nombre.trim(),
        contacto: form.contacto.trim() || null,
      };
      if (editing) {
        await api.put(`/colaboradoras/${editing.id}`, body);
      } else {
        await api.post('/colaboradoras', body);
      }
      setModalOpen(false);
      fetchData();
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await api.delete(`/colaboradoras/${deleteTarget.id}`);
      setDeleteTarget(null);
      fetchData();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Nombre' },
    { key: 'contacto', label: 'Contacto', render: (r) => r.contacto ?? '—' },
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
  if (error) return <p>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Colaboradoras</h1>
        <div className={styles.headerActions}>
          <Button onClick={openCreate}>Nueva Colaboradora</Button>
        </div>
      </div>

      <Table columns={columns} data={data} emptyMessage="No hay colaboradoras registradas" />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Colaboradora' : 'Nueva Colaboradora'}>
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Nombre">
            <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} required autoFocus />
          </FormField>
          <FormField label="Contacto (opcional)">
            <input value={form.contacto} onChange={(e) => setForm({ ...form, contacto: e.target.value })} placeholder="Teléfono, email, etc." />
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
        title="Eliminar Colaboradora"
        message={`¿Eliminar "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
      />
    </div>
  );
}
