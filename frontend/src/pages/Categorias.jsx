import { useState, useMemo, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Categorias.module.css';

const TIPOS = {
  MATERIA_PRIMA: { label: 'Materia Prima', subtitle: 'de Materias Primas', csvName: 'categorias-materias-primas.csv' },
  PRODUCTO: { label: 'Producto', subtitle: 'de Productos', csvName: 'categorias-productos.csv' },
};

const emptyForm = { nombre: '', categoriaPadreId: '' };

export default function Categorias() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tipo = searchParams.get('tipo') || 'MATERIA_PRIMA';
  const tipoInfo = TIPOS[tipo] || TIPOS.MATERIA_PRIMA;

  const apiPath = `/categorias?tipo=${tipo}`;
  const { data, loading, error, refetch } = useApi(apiPath);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [errores, setErrores] = useState({});
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const setTipo = useCallback((newTipo) => {
    setSearchParams({ tipo: newTipo });
  }, []);

  const openCreate = useCallback(() => {
    setEditing(null);
    setForm(emptyForm);
    setErrores({});
    setModalOpen(true);
  }, []);

  const openEdit = useCallback((cat) => {
    setEditing(cat);
    setForm({ nombre: cat.nombre, categoriaPadreId: cat.categoriaPadreId ?? '' });
    setErrores({});
    setModalOpen(true);
  }, []);

  function validar() {
    const e = {};
    if (!form.nombre?.trim()) e.nombre = 'Requerido';
    setErrores(e);
    return Object.keys(e).length === 0;
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!validar()) return;
    setSaving(true);
    try {
      const body = {
        nombre: form.nombre.trim(),
        categoriaPadreId: form.categoriaPadreId === '' ? null : Number(form.categoriaPadreId),
        tipo,
      };
      if (editing) {
        await api.put(`/categorias/${editing.id}`, { ...body, tipo });
      } else {
        await api.post('/categorias', body);
      }
      setModalOpen(false);
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }

  const handleDelete = useCallback(async () => {
    if (!deleteTarget) return;
    try {
      await api.delete(`/categorias/${deleteTarget.id}?tipo=${tipo}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }, [deleteTarget, tipo, refetch]);

  const columns = useMemo(() => [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Nombre' },
    { key: 'categoriaPadreNombre', label: 'Categoría Padre', render: (r) => r.categoriaPadreNombre ?? '—' },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <ActionMenu actions={[
          { label: 'Editar', onClick: () => openEdit(row) },
          { label: 'Eliminar', onClick: () => setDeleteTarget(row) },
        ]} />
      ),
    },
  ], [openEdit]);

  if (loading) return <Loading />;
  if (error) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.tabs}>
        {Object.entries(TIPOS).map(([key, info]) => (
          <button
            key={key}
            className={`${styles.tab} ${tipo === key ? styles.active : ''}`}
            onClick={() => setTipo(key)}
          >
            {info.label}
          </button>
        ))}
      </div>

      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Categorías {tipoInfo.subtitle}</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'nombre', label: 'Nombre' },
            { key: 'categoriaPadreNombre', label: 'Categoría Padre' },
          ], tipoInfo.csvName)}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nueva Categoría</Button>
        </div>
      </div>

      <Table columns={columns} data={data} />

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Categoría' : 'Nueva Categoría'}>
        <form onSubmit={handleSave} className={styles.form} noValidate>
          <FormField label="Nombre" error={errores.nombre}>
            <input value={form.nombre} onChange={(e) => { setForm({ ...form, nombre: e.target.value }); setErrores((prev) => ({ ...prev, nombre: undefined })); }} autoFocus />
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
