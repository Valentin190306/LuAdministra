import { useState, useMemo, useCallback } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import { useInfiniteScroll } from '../hooks/useInfiniteScroll';
import { downloadCSV } from '../utils/csv';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import styles from './Consignaciones.module.css';

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

const emptyForm = { consignatarioId: '', fecha: todayStr(), productos: [{ productoId: '', cantidad: '' }] };

function emptyRendicionProductos(productos) {
  return productos.map((p) => ({
    productoId: p.productoId,
    productoNombre: p.productoNombre,
    cantidadDespachada: p.cantidad,
    cantidadVendida: '',
  }));
}

export default function Consignaciones() {
  const { data: consignatarios } = useApi('/consignatarios');
  const { data: productos } = useApi('/productos');
  const [filterConsignatarioId, setFilterConsignatarioId] = useState('');
  const [filterEstado, setFilterEstado] = useState('');
  const [sortBy, setSortBy] = useState('fecha');
  const [sortDir, setSortDir] = useState('desc');

  const buildUrl = useCallback((page, size) => {
    const params = new URLSearchParams({ page, size, sortBy, sortDir });
    if (filterConsignatarioId) params.set('consignatarioId', filterConsignatarioId);
    if (filterEstado) params.set('estado', filterEstado);
    return `/consignaciones?${params}`;
  }, [filterConsignatarioId, filterEstado, sortBy, sortDir]);

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

  const [stockModalConsignatario, setStockModalConsignatario] = useState(null);
  const [stockData, setStockData] = useState(null);
  const [loadingStock, setLoadingStock] = useState(false);

  const [devolverModal, setDevolverModal] = useState(null);
  const [devolverProductos, setDevolverProductos] = useState([]);
  const [savingDevolver, setSavingDevolver] = useState(false);

  const consignatarioOptions = useMemo(() => {
    if (!consignatarios) return [];
    return [{ id: '', nombre: 'Todas' }, ...consignatarios];
  }, [consignatarios]);

  function addProducto() {
    setForm((prev) => ({
      ...prev,
      productos: [...prev.productos, { productoId: '', cantidad: '' }],
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
    const valid = form.productos.filter((p) => p.productoId && p.cantidad);
    if (!form.consignatarioId || valid.length === 0) return;
    setSaving(true);
    try {
      await api.post('/consignaciones', {
        consignatarioId: Number(form.consignatarioId),
        fecha: form.fecha,
        productos: valid.map((p) => ({
          productoId: Number(p.productoId),
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
      await api.delete(`/consignaciones/${deleteTarget.id}`);
      setDeleteTarget(null);
      refetch();
    } catch (err) {
      alert(err.message);
    }
  }

  async function openRendicion(d) {
    setRendicionModal(d);
    setRendicionMonto('');
    setRendicionFecha(todayStr());
    try {
      const rendiciones = await api.get(`/rendiciones/consignacion/${d.id}`);
      const yaRendido = {};
      rendiciones.forEach((r) => {
        r.productos.forEach((p) => {
          yaRendido[p.productoId] = (yaRendido[p.productoId] || 0)
            + p.cantidadVendida + p.cantidadDevuelta;
        });
      });
      setRendicionProductos(
        d.productos.map((p) => ({
          productoId: p.productoId,
          productoNombre: p.productoNombre,
          cantidadDespachada: p.cantidad - (yaRendido[p.productoId] || 0),
          cantidadVendida: '',
        }))
      );
    } catch {
      setRendicionProductos(emptyRendicionProductos(d.productos));
    }
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
      (p) => Number(p.cantidadVendida) > 0
    );
    if (valid.length === 0) return;
    setSavingRendicion(true);
    try {
      await api.post('/rendiciones', {
        consignacionId: rendicionModal.id,
        montoEntregado: Number(rendicionMonto) || 0,
        fecha: rendicionFecha,
        productos: valid.map((p) => ({
          productoId: p.productoId,
          cantidadVendida: Number(p.cantidadVendida) || 0,
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
      const result = await api.get(`/rendiciones/consignacion/${d.id}`);
      setRendicionesData(result);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoadingRendiciones(false);
    }
  }

  async function openStockConsignado(d) {
    setStockModalConsignatario(d);
    setStockData(null);
    setLoadingStock(true);
    try {
      const result = await api.get(`/consignaciones/stock-consignado?consignatarioId=${d.consignatarioId}`);
      setStockData(result);
    } catch (err) {
      alert(err.message);
    } finally {
      setLoadingStock(false);
    }
  }

  function openDevolver(d) {
    setDevolverModal(d);
    setDevolverProductos(
      d.productos.map((p) => ({
        productoId: p.productoId,
        productoNombre: p.productoNombre,
        cantidadDespachada: p.cantidad,
        cantidadDevuelta: '',
      }))
    );
  }

  function updateDevolverProducto(index, value) {
    setDevolverProductos((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], cantidadDevuelta: value };
      return next;
    });
  }

  async function handleDevolverSave(e) {
    e.preventDefault();
    const valid = devolverProductos.filter((p) => Number(p.cantidadDevuelta) > 0);
    if (valid.length === 0) return;
    setSavingDevolver(true);
    try {
      await api.post('/rendiciones', {
        consignacionId: devolverModal.id,
        montoEntregado: 0,
        fecha: todayStr(),
        productos: valid.map((p) => ({
          productoId: p.productoId,
          cantidadVendida: 0,
          cantidadDevuelta: Number(p.cantidadDevuelta),
        })),
      });
      setDevolverModal(null);
      refetch();
    } catch (err) {
      alert(err.message);
    } finally {
      setSavingDevolver(false);
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
          { label: 'Rendiciones', onClick: () => openVerRendiciones(row) },
          { label: 'Stock en consignación', onClick: () => openStockConsignado(row) },
        ];
        if (row.estado !== 'RENDIDO_TOTAL') {
          items.push({ label: 'Rendir', onClick: () => openRendicion(row) });
          items.push({ label: 'Devolver', onClick: () => openDevolver(row) });
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
          <Button onClick={openCreate}>Nueva Consignación</Button>
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

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Nueva Consignación">
        <form onSubmit={handleSave} className={styles.form}>
          <FormField label="Consignataria">
            <select value={form.consignatarioId} onChange={(e) => setForm({ ...form, consignatarioId: e.target.value })} required>
              <option value="">Seleccionar...</option>
              {consignatarios?.map((c) => (
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
                  value={p.productoId}
                  onChange={(e) => updateProducto(i, 'productoId', e.target.value)}
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

      <Modal isOpen={!!rendicionModal} onClose={() => setRendicionModal(null)} title={`Rendición de Consignación #${rendicionModal?.id}`}>
        {rendicionModal && (
          <div>
            <p className={styles.despachoInfo}>
              <strong>{rendicionModal.consignatarioNombre}</strong> — {rendicionModal.productos.length} producto(s)
              {' | '}Estado: {estadoLabels[rendicionModal.estado]}
            </p>
            <form onSubmit={handleRendicionSave} className={styles.form}>
              <div className={styles.productosSection}>
                <label className={styles.sectionLabel}>Productos rendidos</label>
                {rendicionProductos.map((p, i) => (
                  <div key={i} className={styles.rendicionProducto}>
                    <span className={styles.rendicionProductoNombre}>{p.productoNombre}</span>
                    <span className={styles.rendicionProductoDisponible}>Disp: {p.cantidadDespachada}</span>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      placeholder="Vendido"
                      value={p.cantidadVendida}
                      onChange={(e) => updateRendicionProducto(i, 'cantidadVendida', e.target.value)}
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

      <Modal isOpen={!!verRendiciones} onClose={() => { setVerRendiciones(null); setRendicionesData(null); }} title={`Rendiciones - Consignación #${verRendiciones?.id}`}>
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
                        {p.productoNombre}: vendido <strong>{p.cantidadVendida}</strong>, devuelto <strong>{p.cantidadDevuelta}</strong>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p>No hay rendiciones registradas para esta consignación.</p>
          )
        )}
      </Modal>

      <Modal isOpen={!!stockModalConsignatario} onClose={() => { setStockModalConsignatario(null); setStockData(null); }} title="Stock en consignación">
        {loadingStock ? <Loading /> : (
          stockData && stockData.length > 0 ? (
            <table className={styles.stockTable}>
              <thead>
                <tr>
                  <th>Producto</th>
                  <th>Despachado</th>
                  <th>Rendido</th>
                  <th>Pendiente</th>
                </tr>
              </thead>
              <tbody>
                {stockData.map((s, i) => (
                  <tr key={i}>
                    <td>{s.productoNombre}</td>
                    <td>{s.cantidadDespachada}</td>
                    <td>{s.cantidadRendida}</td>
                    <td className={styles.stockPendiente}>{s.cantidadPendiente}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p>No hay productos en consignación con esta consignataria.</p>
          )
        )}
      </Modal>

      <Modal isOpen={!!devolverModal} onClose={() => setDevolverModal(null)} title={`Devolver Productos - Consignación #${devolverModal?.id}`}>
        {devolverModal && (
          <div>
            <p className={styles.despachoInfo}>
              <strong>{devolverModal.consignatarioNombre}</strong> — {devolverModal.productos.length} producto(s)
              {' | '}Estado: {estadoLabels[devolverModal.estado]}
            </p>
            <form onSubmit={handleDevolverSave} className={styles.form}>
              <div className={styles.productosSection}>
                <label className={styles.sectionLabel}>Productos a devolver</label>
                {devolverProductos.map((p, i) => (
                  <div key={i} className={styles.rendicionProducto}>
                    <span className={styles.rendicionProductoNombre}>{p.productoNombre}</span>
                    <span className={styles.rendicionProductoDisponible}>Disp: {p.cantidadDespachada}</span>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      placeholder="Devuelto"
                      value={p.cantidadDevuelta}
                      onChange={(e) => updateDevolverProducto(i, e.target.value)}
                    />
                  </div>
                ))}
              </div>
              <div className={styles.formActions}>
                <Button variant="ghost" type="button" onClick={() => setDevolverModal(null)}>Cancelar</Button>
                <Button type="submit" disabled={savingDevolver}>{savingDevolver ? 'Guardando...' : 'Registrar Devolución'}</Button>
              </div>
            </form>
          </div>
        )}
      </Modal>

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
