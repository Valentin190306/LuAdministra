import { useState, useMemo, useCallback } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import { useNotify } from '../context/NotificationContext';
import { downloadCSV } from '../utils/csv';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import Loading from '../components/ui/Loading';
import ConsignacionForm from './ConsignacionForm';
import RendicionForm from './RendicionForm';
import DevolverForm from './DevolverForm';
import VerRendiciones from './VerRendiciones';
import StockConsignadoModal from './StockConsignadoModal';
import styles from './Consignaciones.module.css';

const estadoLabels = {
  PENDIENTE: 'Pendiente',
  RENDIDO_PARCIAL: 'Rendido Parcial',
  RENDIDO_TOTAL: 'Rendido Total',
};

const estadoOptions = [
  { value: '', label: 'Todos los estados' },
  { value: 'PENDIENTE', label: 'Pendiente' },
  { value: 'RENDIDO_PARCIAL', label: 'Rendido Parcial' },
  { value: 'RENDIDO_TOTAL', label: 'Rendido Total' },
];

export default function Consignaciones() {
  const { notify, notifySuccess } = useNotify();
  const { data: consignatarios } = useApi('/consignatarios');
  const { data: productos } = useApi('/productos');
  const [filterConsignatarioId, setFilterConsignatarioId] = useState('');
  const [filterEstado, setFilterEstado] = useState('');
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');

  const [consignacionFormOpen, setConsignacionFormOpen] = useState(false);
  const [rendicionTarget, setRendicionTarget] = useState(null);
  const [devolverTarget, setDevolverTarget] = useState(null);
  const [verRendicionesTarget, setVerRendicionesTarget] = useState(null);
  const [stockTarget, setStockTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const buildUrl = useCallback((page, size) => {
    const params = new URLSearchParams({ page, size, sortBy, sortDir });
    if (filterConsignatarioId) params.set('consignatarioId', filterConsignatarioId);
    if (filterEstado) params.set('estado', filterEstado);
    return `/consignaciones?${params}`;
  }, [filterConsignatarioId, filterEstado, sortBy, sortDir]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);

  const consignatarioOptions = useMemo(() => {
    if (!consignatarios) return [];
    return [{ id: '', nombre: 'Todas' }, ...consignatarios];
  }, [consignatarios]);

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await api.delete(`/consignaciones/${deleteTarget.id}`);
      setDeleteTarget(null);
      notifySuccess('Consignación eliminada correctamente');
      refetch();
    } catch (err) {
      notify(err, 'error');
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'consignatarioNombre', label: 'Consignataria' },
    {
      key: 'productos',
      label: 'Productos',
      render: (r) => `${r.productos.length} producto(s)`,
    },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'estado',
      label: 'Estado',
      render: (r) => {
        const cls = r.estado === 'RENDIDO_TOTAL' ? styles.estadoTotal
          : r.estado === 'RENDIDO_PARCIAL' ? styles.estadoParcial
          : styles.estadoPendiente;
        return <span className={cls}>{estadoLabels[r.estado] ?? r.estado}</span>;
      },
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => {
        const items = [
          { label: 'Rendiciones', onClick: () => setVerRendicionesTarget(row) },
          { label: 'Stock en consignación', onClick: () => setStockTarget(row) },
        ];
        if (row.estado !== 'RENDIDO_TOTAL') {
          items.push({ label: 'Rendir', onClick: () => setRendicionTarget(row) });
          items.push({ label: 'Devolver', onClick: () => setDevolverTarget(row) });
        }
        items.push({ label: 'Eliminar', onClick: () => setDeleteTarget(row) });
        return <ActionMenu actions={items} />;
      },
    },
  ];

  const csvColumns = [
    { key: 'id', label: 'ID' },
    { key: 'consignatarioNombre', label: 'Consignataria' },
    { key: 'fecha', label: 'Fecha' },
    { key: 'estado', label: 'Estado', value: (r) => estadoLabels[r.estado] ?? r.estado },
  ];

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Consignaciones</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, csvColumns, 'consignaciones.csv')}>Exportar CSV</Button>
          <Button onClick={() => setConsignacionFormOpen(true)}>Nueva Consignación</Button>
        </div>
      </div>

      <div className={styles.filters}>
        <label className={styles.filterLabel}>
          Consignataria:
          <select value={filterConsignatarioId} onChange={(e) => setFilterConsignatarioId(e.target.value)} className={styles.filterSelect}>
            {consignatarioOptions.map((c) => (
              <option key={c.id} value={c.id}>{c.nombre}</option>
            ))}
          </select>
        </label>
        <label className={styles.filterLabel}>
          Estado:
          <select value={filterEstado} onChange={(e) => setFilterEstado(e.target.value)} className={styles.filterSelect}>
            {estadoOptions.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </label>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="fecha">Ordenar por fecha</option>
        </select>
        <Button variant="ghost" onClick={() => setSortDir((d) => d === 'asc' ? 'desc' : 'asc')}>
          {sortDir === 'asc' ? '↑ Asc' : '↓ Desc'}
        </Button>
      </div>

      {error && <p className={styles.errorMsg}>Error: {error.message}</p>}

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay consignaciones registradas." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <ConsignacionForm
        isOpen={consignacionFormOpen}
        onClose={() => setConsignacionFormOpen(false)}
        consignatarios={consignatarios}
        productos={productos}
        onSaved={refetch}
      />

      <RendicionForm
        consignacion={rendicionTarget}
        onClose={() => setRendicionTarget(null)}
        onSaved={refetch}
      />

      <DevolverForm
        consignacion={devolverTarget}
        onClose={() => setDevolverTarget(null)}
        onSaved={refetch}
      />

      <VerRendiciones
        consignacion={verRendicionesTarget}
        onClose={() => setVerRendicionesTarget(null)}
      />

      <StockConsignadoModal
        consignacion={stockTarget}
        onClose={() => setStockTarget(null)}
      />

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Eliminar Consignación"
        message={`¿Eliminar la consignación de "${deleteTarget?.consignatarioNombre}" del ${deleteTarget?.fecha}?`}
      />
    </div>
  );
}
