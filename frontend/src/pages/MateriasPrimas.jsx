import { useState, useCallback, useMemo } from 'react';
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
import styles from './MateriasPrimas.module.css';

const emptyForm = { nombre: '', unidadMedida: '', stockActual: '', stockMinimo: '', categoriaId: '' };

export default function MateriasPrimas() {
  const [busqueda, setBusqueda] = useState('');
  const [filtroCategoria, setFiltroCategoria] = useState('');
  const [sortBy, setSortBy] = useState('nombre');
  const [sortDir, setSortDir] = useState('asc');
  const { data: categorias } = useApi('/categorias?tipo=MATERIA_PRIMA');

  const buildUrl = useCallback((page, size) => {
    const params = new URLSearchParams();
    if (busqueda) params.set('nombre', busqueda);
    if (filtroCategoria) params.set('categoriaId', filtroCategoria);
    params.set('sortBy', sortBy);
    params.set('sortDir', sortDir);
    params.set('page', page);
    params.set('size', size);
    return `/materias-primas?${params}`;
  }, [busqueda, filtroCategoria, sortBy, sortDir]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [errores, setErrores] = useState({});
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const openCreate = useCallback(() => {
    setEditing(null);
    setForm(emptyForm);
    setErrores({});
    setModalOpen(true);
  }, []);

  const openEdit = useCallback((mp) => {
    setEditing(mp);
    setForm({ nombre: mp.nombre, unidadMedida: mp.unidadMedida, stockActual: mp.stockActual ?? '', stockMinimo: mp.stockMinimo ?? '', categoriaId: mp.categoriaId ?? '' });
    setErrores({});
    setModalOpen(true);
  }, []);

  function validar() {
    const e = {};
    if (!form.nombre?.trim()) e.nombre = 'Requerido';
    if (!form.unidadMedida?.trim()) e.unidadMedida = 'Requerido';
    if (form.stockActual !== '' && Number(form.stockActual) < 0) e.stockActual = 'No puede ser negativo';
    if (form.stockMinimo !== '' && Number(form.stockMinimo) < 0) e.stockMinimo = 'No puede ser negativo';
    setErrores(e);
    return Object.keys(e).length === 0;
  }

  const handleSave = useCallback(async (e) => {
    e.preventDefault();
    if (!validar()) return;
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
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }, [form, editing, refetch]);

  const handleDelete = useCallback(async () => {
    if (!deleteTarget) return;
    try {
      await api.delete(`/materias-primas/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }, [deleteTarget, refetch]);

  const columns = useMemo(() => [
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
  ], [openEdit]);

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  const handleExport = async () => {
    try {
      const params = new URLSearchParams();
      if (busqueda) params.set('nombre', busqueda);
      if (filtroCategoria) params.set('categoriaId', filtroCategoria);
      params.set('sortBy', sortBy);
      params.set('sortDir', sortDir);
      const full = await api.get(`/materias-primas?${params}`);
      downloadCSV(full, [
        { key: 'nombre', label: 'Nombre' },
        { key: 'categoriaNombre', label: 'Categoría' },
        { key: 'unidadMedida', label: 'Unidad de Medida' },
        { key: 'stockActual', label: 'Stock Actual' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ], 'materias-primas.csv');
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Materias Primas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={handleExport}>Exportar CSV</Button>
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

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay materias primas. Creá una primero." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Materia Prima' : 'Nueva Materia Prima'}>
        <form onSubmit={handleSave} className={styles.form} noValidate>
          <FormField label="Nombre" error={errores.nombre}>
            <input value={form.nombre} onChange={(e) => { setForm({ ...form, nombre: e.target.value }); setErrores((prev) => ({ ...prev, nombre: undefined })); }} autoFocus />
          </FormField>
          <FormField label="Unidad de Medida" error={errores.unidadMedida}>
            <select value={form.unidadMedida} onChange={(e) => { setForm({ ...form, unidadMedida: e.target.value }); setErrores((prev) => ({ ...prev, unidadMedida: undefined })); }}>
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
          <FormField label={editing ? 'Stock Actual (override manual)' : 'Stock Inicial'} error={errores.stockActual}>
            <input type="number" step="any" min="0" value={form.stockActual} onChange={(e) => { setForm({ ...form, stockActual: e.target.value }); setErrores((prev) => ({ ...prev, stockActual: undefined })); }} placeholder={editing ? 'Dejar vacío para mantener el actual' : 'Dejar vacío para iniciar en 0'} />
          </FormField>
          <FormField label="Stock Mínimo (opcional)" error={errores.stockMinimo}>
            <input type="number" step="any" min="0" value={form.stockMinimo} onChange={(e) => { setForm({ ...form, stockMinimo: e.target.value }); setErrores((prev) => ({ ...prev, stockMinimo: undefined })); }} />
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
