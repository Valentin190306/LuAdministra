import { useState, useMemo, useCallback } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import styles from './Despachos.module.css';

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

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

const emptyForm = { colaboradoraId: '', fecha: todayStr(), productos: [{ productoTerminadoId: '', cantidad: '' }] };

function emptyRendicionProductos(productos) {
  return productos.map((p) => ({
    productoTerminadoId: p.productoTerminadoId,
    productoTerminadoNombre: p.productoTerminadoNombre,
    cantidadDespachada: p.cantidad,
    cantidadVendida: '',
    cantidadDevuelta: '',
  }));
}

export default function Despachos() {
  const { data: colaboradoras } = useApi('/colaboradoras');
  const { data: productos } = useApi('/productos-terminados');
  const [filterColId, setFilterColId] = useState('');
  const [filterEstado, setFilterEstado] = useState('');
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');

  const buildUrl = useCallback((page, size) => {
    const params = new URLSearchParams({ page, size, sortBy, sortDir });
    if (filterColId) params.set('colaboradoraId', filterColId);
    if (filterEstado) params.set('estado', filterEstado);
    return `/despachos?${params}`;
  }, [filterColId, filterEstado, sortBy, sortDir]);

  const { data, loading, hasMore, error, sentinelRef, refetch } = useInfiniteScroll(buildUrl);

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const [rendicionModal, setRendicionModal] = useState(null);
  const [rendicionProductos, setRendicionProductos] = useState([]);
  const [rendicionMonto, setRendicionMonto] = useState('');
  const [rendicionFecha, setRendicionFecha] = useState(todayStr());
  const [savingRendicion, setSavingRendicion] = useState(false);

  const [verRendiciones, setVerRendiciones] = useState(null);
  const [rendicionesData, setRendicionesData] = useState(null);
  const [loadingRendiciones, setLoadingRendiciones] = useState(false);

  const colOptions = useMemo(() => {
    if (!colaboradoras) return [];
    return [{ id: '', nombre: 'Todas' }, ...colaboradoras];
  }, [colaboradoras]);

  function addProducto() {
    setForm((prev) => ({
      ...prev,
      productos: [...prev.productos, { productoTerminadoId: '', cantidad: '' }],
    }));
  }

  function updateProducto(index, field, value) {
    setForm((prev) => {
      const next = { ...prev, productos: [...prev.productos] };
      next.productos[index] = { ...next.productos[index], [field]: value };
      return next;
    });
  }

  function removeProducto(index) {
    setForm((prev) => ({
      ...prev,
      productos: prev.productos.filter((_, i) => i !== index),
    }));
  }

  function openCreate() {
    setForm(emptyForm);
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    const valid = form.productos.filter((p) => p.productoTerminadoId && p.cantidad);
    if (!form.colaboradoraId || valid.length === 0) return;
    setSaving(true);
    try {
      await api.post('/despachos', {
        colaboradoraId: Number(form.colaboradoraId),
        fecha: form.fecha,
        productos: valid.map((p) => ({
          productoTerminadoId: Number(p.productoTerminadoId),
          cantidad: Number(p.cantidad),
        })),
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
      await api.delete(`/despachos/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  function openRendicion(d) {
    setRendicionModal(d);
    setRendicionProductos(emptyRendicionProductos(d.productos));
    setRendicionMonto('');
    setRendicionFecha(todayStr());
  }

  function updateRendicionProducto(index, field, value) {
    setRendicionProductos((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  }

  async function handleRendicionSave(e) {
    e.preventDefault();
    const valid = rendicionProductos.filter(
      (p) => Number(p.cantidadVendida) > 0 || Number(p.cantidadDevuelta) > 0
    );
    if (valid.length === 0) return;
    setSavingRendicion(true);
    try {
      await api.post('/rendiciones', {
        despachoId: rendicionModal.id,
        montoEntregado: Number(rendicionMonto) || 0,
        fecha: rendicionFecha,
        productos: valid.map((p) => ({
          productoTerminadoId: p.productoTerminadoId,
          cantidadVendida: Number(p.cantidadVendida) || 0,
          cantidadDevuelta: Number(p.cantidadDevuelta) || 0,
        })),
      });
      setRendicionModal(null);
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSavingRendicion(false);
    }
  }

  async function openVerRendiciones(d) {
    setVerRendiciones(d);
    setLoadingRendiciones(true);
    setRendicionesData(null);
    try {
      const result = await api.get(`/despachos/${d.id}/rendiciones`);
      setRendicionesData(result);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoadingRendiciones(false);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'colaboradoraNombre', label: 'Colaboradora' },
    {
      key: 'productos',
      label: 'Productos',
      render: (r) => r.productos.map((p) => p.productoTerminadoNombre).join(', '),
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
      render: (row) => (
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => openVerRendiciones(row)}>Rendiciones</Button>
          {row.estado !== 'RENDIDO_TOTAL' && (
            <Button variant="ghost" onClick={() => openRendicion(row)}>Rendir</Button>
          )}
          <Button variant="ghost" onClick={() => setDeleteTarget(row)}>Eliminar</Button>
        </div>
      ),
    },
  ];

  if (loading && !data) return <Loading />;
  if (error && !data) return <p className={styles.errorMsg}>Error al cargar: {error.message}</p>;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Despachos</h1>
        <div className={styles.headerActions}>
          <Button onClick={openCreate}>Nuevo Despacho</Button>
        </div>
      </div>

      <div className={styles.filters}>
        <label className={styles.filterLabel}>
          Colaboradora:
          <select value={filterColId} onChange={(e) => setFilterColId(e.target.value)} className={styles.filterSelect}>
            {colOptions.map((c) => (
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

      <Table columns={columns} data={data ?? []} sentinelRef={sentinelRef} emptyMessage="No hay despachos registrados." />

      {loading && hasMore && <p className={styles.loadingMore}>Cargando más...</p>}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Nuevo Despacho">
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Colaboradora">
            <select value={form.colaboradoraId} onChange={(e) => setForm({ ...form, colaboradoraId: e.target.value })} required>
              <option value="">Seleccionar...</option>
              {colaboradoras?.map((c) => (
                <option key={c.id} value={c.id}>{c.nombre}</option>
              ))}
            </select>
          </FormField>
          <FormField label="Fecha">
            <input type="date" value={form.fecha} onChange={(e) => setForm({ ...form, fecha: e.target.value })} required />
          </FormField>
          <div className={styles.productosSection}>
            <label className={styles.sectionLabel}>Productos</label>
            {form.productos.map((p, i) => (
              <div key={i} className={styles.productoRow}>
                <select
                  value={p.productoTerminadoId}
                  onChange={(e) => updateProducto(i, 'productoTerminadoId', e.target.value)}
                  required
                >
                  <option value="">Seleccionar...</option>
                  {productos?.map((pt) => (
                    <option key={pt.id} value={pt.id}>{pt.nombre}</option>
                  ))}
                </select>
                <input
                  type="number"
                  step="any"
                  min="0"
                  placeholder="Cantidad"
                  value={p.cantidad}
                  onChange={(e) => updateProducto(i, 'cantidad', e.target.value)}
                  required
                />
                {form.productos.length > 1 && (
                  <button type="button" className={styles.removeBtn} onClick={() => removeProducto(i)} aria-label="Eliminar">&times;</button>
                )}
              </div>
            ))}
            <Button variant="ghost" type="button" onClick={addProducto}>+ Agregar producto</Button>
          </div>
          <div className={styles.formActions}>
            <Button variant="ghost" type="button" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={!!rendicionModal} onClose={() => setRendicionModal(null)} title={`Rendir Despacho #${rendicionModal?.id}`}>
        {rendicionModal && (
          <div>
            <p className={styles.despachoInfo}>
              <strong>{rendicionModal.colaboradoraNombre}</strong> — {rendicionModal.productos.length} producto(s)
              {' | '}Estado: {estadoLabels[rendicionModal.estado]}
            </p>
            <form onSubmit={handleRendicionSave} className={styles.form}>
              <div className={styles.productosSection}>
                <label className={styles.sectionLabel}>Productos rendidos</label>
                {rendicionProductos.map((p, i) => (
                  <div key={i} className={styles.rendicionProducto}>
                    <span className={styles.rendicionProductoNombre}>{p.productoTerminadoNombre}</span>
                    <span className={styles.rendicionProductoDisponible}>Disp: {p.cantidadDespachada}</span>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      placeholder="Vendido"
                      value={p.cantidadVendida}
                      onChange={(e) => updateRendicionProducto(i, 'cantidadVendida', e.target.value)}
                    />
                    <input
                      type="number"
                      step="any"
                      min="0"
                      placeholder="Devuelto"
                      value={p.cantidadDevuelta}
                      onChange={(e) => updateRendicionProducto(i, 'cantidadDevuelta', e.target.value)}
                    />
                  </div>
                ))}
              </div>
              <div className={styles.row}>
                <FormField label="Monto Entregado ($)">
                  <input type="number" step="any" min="0" value={rendicionMonto} onChange={(e) => setRendicionMonto(e.target.value)} />
                </FormField>
                <FormField label="Fecha">
                  <input type="date" value={rendicionFecha} onChange={(e) => setRendicionFecha(e.target.value)} required />
                </FormField>
              </div>
              <div className={styles.formActions}>
                <Button variant="ghost" type="button" onClick={() => setRendicionModal(null)}>Cancelar</Button>
                <Button type="submit" disabled={savingRendicion}>{savingRendicion ? 'Guardando...' : 'Registrar Rendición'}</Button>
              </div>
            </form>
          </div>
        )}
      </Modal>

      <Modal isOpen={!!verRendiciones} onClose={() => { setVerRendiciones(null); setRendicionesData(null); }} title={`Rendiciones - Despacho #${verRendiciones?.id}`}>
        {loadingRendiciones ? <Loading /> : (
          rendicionesData && rendicionesData.length > 0 ? (
            <div className={styles.rendicionesList}>
              {rendicionesData.map((r) => (
                <div key={r.id} className={styles.rendicionItem}>
                  <div className={styles.rendicionHeader}>
                    <span className={styles.rendicionFecha}>{r.fecha}</span>
                    <span>Monto: <strong>${r.montoEntregado.toLocaleString('es-AR', { minimumFractionDigits: 2 })}</strong></span>
                  </div>
                  <div className={styles.rendicionDetalles}>
                    {r.productos.map((p, i) => (
                      <div key={i} className={styles.rendicionDetalle}>
                        {p.productoTerminadoNombre}: vendido <strong>{p.cantidadVendida}</strong>, devuelto <strong>{p.cantidadDevuelta}</strong>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p>No hay rendiciones registradas para este despacho.</p>
          )
        )}
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Eliminar Despacho"
        message={`¿Eliminar el despacho de "${deleteTarget?.colaboradoraNombre}" del ${deleteTarget?.fecha}?`}
      />
    </div>
  );
}
