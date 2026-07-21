import { useState, useMemo } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Compras.module.css';

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

const emptyForm = { materiaPrimaId: '', fecha: todayStr(), cantidad: '', precio: '', lugar: '' };

export default function Compras() {
  const { data: materiasPrimas } = useApi('/materias-primas');
  const [filterMpId, setFilterMpId] = useState('');
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');
  const sortParams = { sortBy, sortDir };
  const filterPath = filterMpId ? `/compras/materia-prima/${filterMpId}` : '/compras';
  const { data, loading, error, refetch } = useApi(filterPath, sortParams);

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const mpOptions = useMemo(() => {
    if (!materiasPrimas) return [];
    return [{ id: '', nombre: 'Todas' }, ...materiasPrimas];
  }, [materiasPrimas]);

  function openCreate() {
    setForm(emptyForm);
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    if (!form.materiaPrimaId || !form.cantidad || !form.precio) return;
    setSaving(true);
    try {
      const body = {
        materiaPrimaId: Number(form.materiaPrimaId),
        fecha: form.fecha,
        cantidad: Number(form.cantidad),
        precio: Number(form.precio),
        lugar: form.lugar.trim() || null,
      };
      await api.post('/compras', body);
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
      await api.delete(`/compras/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
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
      key: 'acciones',
      label: '',
      render: (row) => (
        <Button variant="ghost" onClick={() => setDeleteTarget(row)}>Eliminar</Button>
      ),
    },
  ];

  if (loading && !data) return <Loading />;
  if (error) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Compras</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => downloadCSV(data, [
            { key: 'materiaPrimaNombre', label: 'Materia Prima' },
            { key: 'fecha', label: 'Fecha' },
            { key: 'cantidad', label: 'Cantidad' },
            { key: 'precio', label: 'Precio' },
            { key: 'lugar', label: 'Lugar' },
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

      <Table columns={columns} data={data} emptyMessage="No hay compras registradas." />

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
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Materia Prima">
            <select value={form.materiaPrimaId} onChange={(e) => setForm({ ...form, materiaPrimaId: e.target.value })} required>
              <option value="">Seleccionar...</option>
              {materiasPrimas?.map((mp) => (
                <option key={mp.id} value={mp.id}>{mp.nombre}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Fecha">
            <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
          </FormField>
          <div className={styles.row}>
            <FormField label="Cantidad">
              <input type="number" step="any" min="0" value={form.cantidad} onChange={(e) => setForm({ ...form, cantidad: e.target.value })} required />
            </FormField>
            <FormField label="Precio ($)">
              <input type="number" step="any" min="0" value={form.precio} onChange={(e) => setForm({ ...form, precio: e.target.value })} required />
            </FormField>
          </div>
          <FormField label="Lugar / Proveedor (opcional)">
            <input value={form.lugar} onChange={(e) => setForm({ ...form, lugar: e.target.value })} placeholder="ej. Mercado Central" />
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
