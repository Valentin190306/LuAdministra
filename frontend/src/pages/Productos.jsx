import { useState, useCallback, useMemo } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import { useNotify } from '../context/NotificationContext';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Productos.module.css';

const emptyForm = { nombre: '', precioVenta: '', stockActual: '', stockMinimo: '', categoriaId: '' };

export default function Productos() {
  const { notify } = useNotify();
  const [busqueda, setBusqueda] = useState('');
  const [filtroCategoria, setFiltroCategoria] = useState('');
  const [sortBy, setSortBy] = useState('nombre');
  const [sortDir, setSortDir] = useState('asc');
  const { data: categorias } = useApi('/categorias?tipo=PRODUCTO');

  const buildUrl = useCallback((page, size) => {
    const params = new URLSearchParams();
    if (busqueda) params.set('nombre', busqueda);
    if (filtroCategoria) params.set('categoriaId', filtroCategoria);
    params.set('sortBy', sortBy);
    params.set('sortDir', sortDir);
    params.set('page', page);
    params.set('size', size);
    return `/productos?${params}`;
  }, [busqueda, filtroCategoria, sortBy, sortDir]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [errores, setErrores] = useState({});
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const [lotesModal, setLotesModal] = useState(null);
  const [lotesData, setLotesData] = useState(null);
  const [loadingLotes, setLoadingLotes] = useState(false);

  const openCreate = useCallback(() => {
    setEditing(null);
    setForm(emptyForm);
    setErrores({});
    setModalOpen(true);
  }, []);

  const openEdit = useCallback((pt) => {
    setEditing(pt);
    setForm({ nombre: pt.nombre, precioVenta: String(pt.precioVenta), stockActual: pt.stockActual ?? '', stockMinimo: pt.stockMinimo ?? '', categoriaId: pt.categoriaId ?? '' });
    setErrores({});
    setModalOpen(true);
  }, []);

  function validar() {
    const e = {};
    if (!form.nombre?.trim()) e.nombre = 'Requerido';
    if (!form.precioVenta) e.precioVenta = 'Requerido';
    else if (Number(form.precioVenta) <= 0) e.precioVenta = 'Debe ser mayor a 0';
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
        precioVenta: Number(form.precioVenta),
        stockActual: form.stockActual === '' ? null : Number(form.stockActual),
        stockMinimo: form.stockMinimo === '' ? null : Number(form.stockMinimo),
        categoriaId: form.categoriaId === '' ? null : Number(form.categoriaId),
      };
      if (editing) {
        await api.put(`/productos/${editing.id}`, body);
      } else {
        await api.post('/productos', body);
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
      await api.delete(`/productos/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }, [deleteTarget, refetch]);

  const openLotes = useCallback(async (pt) => {
    setLotesModal(pt);
    setLoadingLotes(true);
    try {
      const result = await api.get(`/lotes/por-producto?productoId=${pt.id}`);
      setLotesData(result);
    } catch (err) {
      notify(err, 'error');
    } finally {
      setLoadingLotes(false);
    }
  }, []);

  const columns = useMemo(() => [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Nombre' },
    {
      key: 'precioVenta',
      label: 'Precio de Venta',
      render: (r) => `$${r.precioVenta.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    {
      key: 'stockActual',
      label: 'En Depósito',
      render: (row) => {
        const enDeposito = row.stockActual - (row.stockConsignado ?? 0);
        return (
          <span className={row.stockMinimo != null && enDeposito < row.stockMinimo ? styles.lowStock : undefined}>
            {enDeposito}
          </span>
        );
      },
    },
    {
      key: 'stockConsignado',
      label: 'Consignado',
      render: (r) => r.stockConsignado ?? 0,
    },
    { key: 'stockMinimo', label: 'Stock Mínimo', render: (r) => r.stockMinimo ?? '—' },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <ActionMenu actions={[
          { label: 'Editar', onClick: () => openEdit(row) },
          { label: 'Ver vencimientos', onClick: () => openLotes(row) },
          { label: 'Eliminar', onClick: () => setDeleteTarget(row) },
        ]} />
      ),
    },
  ], [openEdit, openLotes]);

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  const handleExport = async () => {
    try {
      const params = new URLSearchParams();
      if (busqueda) params.set('nombre', busqueda);
      if (filtroCategoria) params.set('categoriaId', filtroCategoria);
      params.set('sortBy', sortBy);
      params.set('sortDir', sortDir);
      const full = await api.get(`/productos?${params}`);
      downloadCSV(full, [
        { key: 'nombre', label: 'Nombre' },
        { key: 'categoriaNombre', label: 'Categoría' },
        { key: 'precioVenta', label: 'Precio de Venta' },
        {
          key: 'enDeposito',
          label: 'En Depósito',
          value: (r) => r.stockActual - (r.stockConsignado ?? 0),
        },
        { key: 'stockConsignado', label: 'Consignado' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ], 'productos.csv');
    } catch (err) {
      alert(err.message);
    }
  };

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Productos</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={handleExport}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nuevo Producto</Button>
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
          <option value="precioVenta">Ordenar por precio</option>
          <option value="stockActual">Ordenar por stock</option>
        </select>
        <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
          {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
        </Button>
      </div>

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay productos. Creá uno primero." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Editar Producto' : 'Nuevo Producto'}>
        <form onSubmit={handleSave} className={styles.form} noValidate>
          <FormField label="Nombre / Variante" error={errores.nombre}>
            <input value={form.nombre} onChange={(e) => { setForm({ ...form, nombre: e.target.value }); setErrores((prev) => ({ ...prev, nombre: undefined })); }} autoFocus />
          </FormField>
          <FormField label="Precio de Venta ($)" error={errores.precioVenta}>
            <input type="number" step="any" min="0" value={form.precioVenta} onChange={(e) => { setForm({ ...form, precioVenta: e.target.value }); setErrores((prev) => ({ ...prev, precioVenta: undefined })); }} />
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

      <Modal isOpen={!!lotesModal} onClose={() => { setLotesModal(null); setLotesData(null); }} title={`Vencimientos - ${lotesModal?.nombre}`}>
        {loadingLotes ? <Loading /> : (
          lotesData && lotesData.length > 0 ? (
            <table className={styles.lotesTable}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Fecha Producción</th>
                  <th>Cantidad</th>
                  <th>Días Vigencia</th>
                  <th>Fecha Vencimiento</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {lotesData.map((l) => {
                  const vencido = l.fechaVencimiento && new Date(l.fechaVencimiento) < new Date();
                  return (
                    <tr key={l.id} className={vencido ? styles.vencidoRow : undefined}>
                      <td data-label="ID">{l.id}</td>
                      <td data-label="Fecha Producción">{l.fecha}</td>
                      <td data-label="Cantidad">{l.cantidadFabricada} u</td>
                      <td data-label="Días Vigencia">{l.diasVigencia ?? '—'}</td>
                      <td data-label="Fecha Vencimiento">{l.fechaVencimiento ?? 'Imperecedero'}</td>
                      <td data-label="Estado">{vencido ? '⚠ Vencido' : '✓ Vigente'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          ) : (
            <p>No hay lotes registrados para este producto.</p>
          )
        )}
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Eliminar Producto"
        message={`¿Eliminar "${deleteTarget?.nombre}"? Esta acción no se puede deshacer.`}
      />
    </div>
  );
}
