import { useState, useMemo, useCallback } from 'react';
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
import { todayStr } from '../utils/dates';
import styles from './Compras.module.css';

const emptyForm = { materiaPrimaId: '', fecha: todayStr(), cantidad: '', precio: '', lugar: '', url: '', precioMLReferencia: '' };

export default function Compras() {
  const { data: materiasPrimas } = useApi('/materias-primas');
  const [filterMpId, setFilterMpId] = useState('');
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');

  const buildUrl = useCallback((page, size) => {
    const base = filterMpId ? `/compras/materia-prima/${filterMpId}` : '/compras';
    return `${base}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
  }, [filterMpId, sortBy, sortDir]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [errores, setErrores] = useState({});
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const mpOptions = useMemo(() => {
    if (!materiasPrimas) return [];
    return [{ id: '', nombre: 'Todas' }, ...materiasPrimas];
  }, [materiasPrimas]);

  const openCreate = useCallback(() => {
    setForm(emptyForm);
    setErrores({});
    setModalOpen(true);
  }, []);

  function validar() {
    const e = {};
    if (!form.materiaPrimaId) e.materiaPrimaId = 'Requerido';
    if (!form.cantidad) e.cantidad = 'Requerido';
    if (!form.precio) e.precio = 'Requerido';
    setErrores(e);
    return Object.keys(e).length === 0;
  }

  const handleSave = useCallback(async (e) => {
    e.preventDefault();
    if (!validar()) return;
    setSaving(true);
    try {
      const body = {
        materiaPrimaId: Number(form.materiaPrimaId),
        fecha: form.fecha,
        cantidad: Number(form.cantidad),
        precio: Number(form.precio),
        lugar: form.lugar.trim() || null,
        url: form.url.trim() || null,
        precioMLReferencia: form.precioMLReferencia ? Number(form.precioMLReferencia) : null,
      };
      await api.post('/compras', body);
      setModalOpen(false);
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }, [form, refetch]);

  const handleDelete = useCallback(async () => {
    if (!deleteTarget) return;
    try {
      await api.delete(`/compras/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }, [deleteTarget, refetch]);

  const columns = useMemo(() => [
    { key: 'id', label: 'ID' },
    { key: 'materiaPrimaNombre', label: 'Materia Prima' },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'cantidad',
      label: 'Cantidad',
      render: (r) => `${r.cantidad}`,
    },
    {
      key: 'precio',
      label: 'Precio',
      render: (r) => `$${r.precio.toLocaleString('es-AR', { minimumFractionDigits: 2 })}`,
    },
    { key: 'lugar', label: 'Lugar', render: (r) => r.lugar ?? '—' },
    {
      key: 'url',
      label: 'Link',
      render: (r) => r.url ? <a href={r.url} target="_blank" rel="noopener noreferrer">🔗 Abrir</a> : '—',
    },
    {
      key: 'precioMLReferencia',
      label: 'Precio ML Ref.',
      render: (r) => r.precioMLReferencia != null ? `$${r.precioMLReferencia.toLocaleString('es-AR', { minimumFractionDigits: 2 })}` : '—',
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <ActionMenu actions={[
          { label: 'Eliminar', onClick: () => setDeleteTarget(row) },
        ]} />
      ),
    },
  ], []);

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Compras</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'id', label: 'ID' },
            { key: 'materiaPrimaNombre', label: 'Materia Prima' },
            { key: 'fecha', label: 'Fecha' },
            { key: 'cantidad', label: 'Cantidad' },
            { key: 'precio', label: 'Precio' },
            { key: 'lugar', label: 'Lugar' },
            { key: 'url', label: 'Link' },
            { key: 'precioMLReferencia', label: 'Precio ML Ref.' },
          ], 'compras.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Nueva Compra</Button>
        </div>
      </div>

      <div className={styles.filters}>
        <label className={styles.filterLabel}>
          Filtrar por materia prima:
          <select value={filterMpId} onChange={(e) => setFilterMpId(e.target.value)} className={styles.filterSelect}>
            {mpOptions.map((mp) => (
              <option key={mp.id} value={mp.id}>{mp.nombre}</option>
            ))}
          </select>
        </label>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="fecha">Ordenar por fecha</option>
          <option value="precio">Ordenar por precio</option>
          <option value="cantidad">Ordenar por cantidad</option>
        </select>
        <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
          {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
        </Button>
      </div>

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay compras registradas." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      {filterMpId && data && data.length > 0 && (
        <section className={styles.priceHistory}>
          <h2 className={styles.historyTitle}>Historial de precios</h2>
          <div className={styles.historyGrid}>
            {data.map((c) => (
              <div key={c.id} className={styles.historyItem}>
                <span className={styles.historyDate}>{c.fecha}</span>
                <span className={styles.historyPrice}>${c.precio.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</span>
                <span className={styles.historyQty}>{c.cantidad} {materiasPrimas?.find((m) => m.id === Number(filterMpId))?.unidadMedida ?? 'u'}</span>
              </div>
            ))}
          </div>
        </section>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Compra">
        <form onSubmit={handleSave} className={styles.form} noValidate>
          <FormField label="Materia Prima" error={errores.materiaPrimaId}>
            <select value={form.materiaPrimaId} onChange={(e) => { setForm({ ...form, materiaPrimaId: e.target.value }); setErrores((prev) => ({ ...prev, materiaPrimaId: undefined })); }}>
              <option value="">Seleccionar...</option>
              {materiasPrimas?.map((mp) => (
                <option key={mp.id} value={mp.id}>{mp.nombre}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Fecha">
            <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} />
          </FormField>
          <div className={styles.row}>
            <FormField label="Cantidad" error={errores.cantidad}>
              <input type="number" step="any" min="0" value={form.cantidad} onChange={(e) => { setForm({ ...form, cantidad: e.target.value }); setErrores((prev) => ({ ...prev, cantidad: undefined })); }} />
            </FormField>
            <FormField label="Precio ($)" error={errores.precio}>
              <input type="number" step="any" min="0" value={form.precio} onChange={(e) => { setForm({ ...form, precio: e.target.value }); setErrores((prev) => ({ ...prev, precio: undefined })); }} />
            </FormField>
          </div>
          <FormField label="Lugar / Proveedor (opcional)">
            <input value={form.lugar} onChange={(e) => setForm({ ...form, lugar: e.target.value })} placeholder="ej. Mercado Central" />
          </FormField>
          <FormField label="Link a la página del producto (opcional)">
            <input type="url" value={form.url} onChange={(e) => setForm({ ...form, url: e.target.value })} placeholder="https://ejemplo.com/producto" />
          </FormField>
          <FormField label="Precio ML de referencia ($) (opcional)">
            <input type="number" step="any" min="0" value={form.precioMLReferencia} onChange={(e) => setForm({ ...form, precioMLReferencia: e.target.value })} placeholder="ej. 19949" />
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
        title="Eliminar Compra"
        message={`¿Eliminar la compra de "${deleteTarget?.materiaPrimaNombre}" del ${deleteTarget?.fecha}? Se revertirá el stock.`}
      />
    </div>
  );
}
