import { useState, useEffect, useCallback } from 'react';
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
import styles from './MateriasPrimas.module.css';

const emptyForm = { nombre: '', unidadMedida: '', stockActual: '', stockMinimo: '', categoriaId: '' };

export default function MateriasPrimas() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [busqueda, setBusqueda] = useState('');
  const [filtroCategoria, setFiltroCategoria] = useState('');
  const [sortBy, setSortBy] = useState('nombre');
  const [sortDir, setSortDir] = useState('asc');
  const { data: categorias } = useApi('/categorias-materias-primas');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (busqueda) params.set('nombre', busqueda);
      if (filtroCategoria) params.set('categoriaId', filtroCategoria);
      params.set('sortBy', sortBy);
      params.set('sortDir', sortDir);
      const result = await api.get(`/materias-primas?${params}`);
      setData(result);
      setError(null);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [busqueda, filtroCategoria, sortBy, sortDir]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);
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
    setForm({ nombre: mp.nombre, unidadMedida: mp.unidadMedida, stockActual: mp.stockActual ?? '', stockMinimo: mp.stockMinimo ?? '', categoriaId: mp.categoriaId ?? '' });
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
        stockActual: form.stockActual === '' ? null : Number(form.stockActual),
        stockMinimo: form.stockMinimo === '' ? null : Number(form.stockMinimo),
        categoriaId: form.categoriaId === '' ? null : Number(form.categoriaId),
      };
      if (editing) {
        await api.put(`/materias-primas/${editing.id}`, body);
      } else {
        await api.post('/materias-primas', body);
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
      await api.delete(`/materias-primas/${deleteTarget.id}`);
      setDeleteTarget(null);
      fetchData();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
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
        <ActionMenu actions={[
          { label: 'Editar', onClick: () => openEdit(row) },
          { label: 'Eliminar', onClick: () => setDeleteTarget(row) },
        ]} />
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
            { key: 'categoriaNombre', label: 'Categoría' },
            { key: 'unidadMedida', label: 'Unidad de Medida' },
            { key: 'stockActual', label: 'Stock Actual' },
            { key: 'stockMinimo', label: 'Stock Mínimo' },
          ], 'materias-primas.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nueva Materia Prima</Button>
        </div>
      </div>

      <div className={styles.filters}>
        <input
          type="text"
          placeholder="Buscar por nombre..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          className={styles.searchInput}
        />
        <select value={filtroCategoria} onChange={(e) => setFiltroCategoria(e.target.value)}>
          <option value="">Todas las categorías</option>
          {categorias?.map((c) => (
            <option key={c.id} value={c.id}>{c.nombre}</option>
          ))}
        </select>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="nombre">Ordenar por nombre</option>
          <option value="stockActual">Ordenar por stock</option>
          <option value="unidadMedida">Ordenar por unidad</option>
          <option value="ultimaCompraFecha">Ordenar por última compra (fecha)</option>
          <option value="ultimaCompraPrecio">Ordenar por última compra (precio)</option>
        </select>
        <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
          {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
        </Button>
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
          <FormField label="Categoría (opcional)">
            <select value={form.categoriaId} onChange={(e) => setForm({ ...form, categoriaId: e.target.value })}>
              <option value="">Sin categoría</option>
              {categorias?.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
          </FormField>
          {editing && (
            <FormField label="Stock Actual (override manual)">
              <input type="number" step="any" min="0" value={form.stockActual} onChange={(e) => setForm({ ...form, stockActual: e.target.value })} placeholder="Dejar vacío para mantener el actual" />
            </FormField>
          )}
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
