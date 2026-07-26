import { useState, useEffect } from 'react';
import { api } from '../api/client';
import Modal from '../components/ui/Modal';
import Button from '../components/ui/Button';
import FormField from '../components/ui/FormField';
import { useNotify } from '../context/NotificationContext';
import { todayStr } from '../utils/dates';
import styles from './Consignaciones.module.css';

const estadoLabels = {
  PENDIENTE: 'Pendiente',
  RENDIDO_PARCIAL: 'Rendido Parcial',
  RENDIDO_TOTAL: 'Rendido Total',
};

function emptyRendicionProductos(productos) {
  return productos.map((p) => ({
    lineaConsignacionId: p.id,
    productoId: p.productoId,
    productoNombre: p.productoNombre,
    cantidadDespachada: p.cantidad,
    cantidadVendida: '',
  }));
}

export default function RendicionForm({ consignacion, onClose, onSaved }) {
  const { notify, notifySuccess } = useNotify();
  const [productos, setProductos] = useState([]);
  const [monto, setMonto] = useState('');
  const [fecha, setFecha] = useState(todayStr());
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!consignacion) return;
    let cancelled = false;
    async function load() {
      try {
        const rendiciones = await api.get(`/rendiciones/consignacion/${consignacion.id}`);
        const yaRendido = {};
        rendiciones.forEach((r) => {
          r.productos.forEach((p) => {
            yaRendido[p.productoId] = (yaRendido[p.productoId] || 0)
              + p.cantidadVendida + p.cantidadDevuelta;
          });
        });
        if (!cancelled) {
          setProductos(
            consignacion.productos.map((p) => ({
              lineaConsignacionId: p.id,
              productoId: p.productoId,
              productoNombre: p.productoNombre,
              cantidadDespachada: p.cantidad - (yaRendido[p.productoId] || 0),
              cantidadVendida: '',
            }))
          );
        }
      } catch {
        if (!cancelled) {
          setProductos(emptyRendicionProductos(consignacion.productos));
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [consignacion]);

  function updateProducto(index, value) {
    setProductos((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], cantidadVendida: value };
      return next;
    });
  }

  async function handleSave(e) {
    e.preventDefault();
    const valid = productos.filter((p) => Number(p.cantidadVendida) > 0);
    if (valid.length === 0) return;
    setSaving(true);
    try {
      await api.post('/rendiciones', {
        consignacionId: consignacion.id,
        montoEntregado: Number(monto) || 0,
        fecha,
        productos: valid.map((p) => ({
          lineaConsignacionId: p.lineaConsignacionId,
          cantidadVendida: Number(p.cantidadVendida) || 0,
        })),
      });
      onClose();
      notifySuccess('Rendición registrada correctamente');
      onSaved();
    } catch (err) {
      notify(err, 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal isOpen={!!consignacion} onClose={onClose} title={`Rendición de Consignación #${consignacion?.id}`}>
      {consignacion && (
        <div>
          <p className={styles.despachoInfo}>
            <strong>{consignacion.consignatarioNombre}</strong> — {consignacion.productos.length} producto(s)
            {' | '}Estado: {estadoLabels[consignacion.estado]}
          </p>
          <form onSubmit={handleSave} className={styles.form}>
            <div className={styles.productosSection}>
              <label className={styles.sectionLabel}>Productos rendidos</label>
              {loading ? <p>Cargando productos...</p> : (
                productos.map((p, i) => (
                  <div key={i} className={styles.rendicionProducto}>
                    <span className={styles.rendicionProductoNombre}>{p.productoNombre}</span>
                    <span className={styles.rendicionProductoDisponible}>Disp: {p.cantidadDespachada}</span>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      placeholder="Vendido"
                      value={p.cantidadVendida}
                      onChange={(e) => updateProducto(i, e.target.value)}
                    />
                  </div>
                ))
              )}
            </div>
            <div className={styles.row}>
              <FormField label="Monto Entregado ($)">
                <input type="number" step="any" min="0" value={monto} onChange={(e) => setMonto(e.target.value)} />
              </FormField>
              <FormField label="Fecha">
                <input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} required />
              </FormField>
            </div>
            <div className={styles.formActions}>
              <Button variant="ghost" type="button" onClick={onClose}>Cancelar</Button>
              <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Registrar Rendición'}</Button>
            </div>
          </form>
        </div>
      )}
    </Modal>
  );
}
