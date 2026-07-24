import { useState, useCallback } from 'react';
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
import styles from './Produccion.module.css';

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

const emptyForm = { productoTerminadoId: '', fecha: todayStr(), cantidadFabricada: '' };

function monthAgo() {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  return d.toISOString().slice(0, 10);
}

export default function Produccion() {
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');
  const [desde, setDesde] = useState(monthAgo());
  const [hasta, setHasta] = useState(todayStr());
  const [usarPeriodo, setUsarPeriodo] = useState(false);
  const { data: ptList } = useApi('/productos-terminados');

  const buildUrl = useCallback((page, size) => {
    if (usarPeriodo) {
      return `/producciones/periodo?desde=${desde}&hasta=${hasta}&page=${page}&size=${size}`;
    }
    return `/producciones?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
  }, [sortBy, sortDir, usarPeriodo, desde, hasta]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);

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
    if (!form.productoTerminadoId || !form.cantidadFabricada) return;
    setSaving(true);
    try {
      await api.post('/producciones', {
        productoTerminadoId: Number(form.productoTerminadoId),
        fecha: form.fecha,
        cantidadFabricada: Number(form.cantidadFabricada),
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
      await api.delete(`/producciones/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'productoTerminadoNombre', label: 'Producto Terminado' },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'cantidadFabricada',
      label: 'Cantidad',
      render: (r) => `${r.cantidadFabricada} u`,
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
  ];

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Producción</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'id', label: 'ID' },
            { key: 'productoTerminadoNombre', label: 'Producto Terminado' },
            { key: 'fecha', label: 'Fecha' },
            { key: 'cantidadFabricada', label: 'Cantidad Fabricada' },
          ], 'produccion.csv')}>Exportar CSV</Button>
          <Button onClick={openCreate}>Registrar Producción</Button>
        </div>
      </div>

      <p className={styles.hint}>Al registrar una producción se descuenta automáticamente el stock de materias primas según la receta y se incrementa el stock del producto terminado.</p>

      <div className={styles.filters}>
        <label className={styles.filterLabel}>
          <input type="checkbox" checked={usarPeriodo} onChange={(e) => setUsarPeriodo(e.target.checked)} />
          Filtrar por período
        </label>
        {usarPeriodo && (
          <>
            <label className={styles.filterLabel}>
              Desde:
              <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} className={styles.filterSelect} />
            </label>
            <label className={styles.filterLabel}>
              Hasta:
              <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} className={styles.filterSelect} />
            </label>
          </>
        )}
        {!usarPeriodo && (
          <>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="fecha">Ordenar por fecha</option>
              <option value="cantidadFabricada">Ordenar por cantidad</option>
            </select>
            <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
              {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
            </Button>
          </>
        )}
      </div>

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay producciones registradas." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Registrar Producción">
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Producto Terminado">
            <select value={form.productoTerminadoId} onChange={(e) => setForm({ ...form, productoTerminadoId: e.target.value })} required>
              <option value="">Seleccionar...</option>
              {ptList?.map((pt) => (
                <option key={pt.id} value={pt.id}>{pt.nombre}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Fecha">
            <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
          </FormField>
          <FormField label="Cantidad Fabricada">
            <input type="number" step="any" min="0" value={form.cantidadFabricada} onChange={(e) => setForm({ ...form, cantidadFabricada: e.target.value })} required />
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
        title="Eliminar Producción"
        message={`¿Eliminar la producción de "${deleteTarget?.productoTerminadoNombre}" del ${deleteTarget?.fecha}? Se revertirá el stock de materias primas y productos terminados.`}
      />
    </div>
  );
}
