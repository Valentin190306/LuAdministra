import { useState, useEffect } from 'react';
import { api } from '../api/client';
import Modal from '../components/ui/Modal';
import Button from '../components/ui/Button';
import { useNotify } from '../context/NotificationContext';
import { todayStr } from '../utils/dates';
import styles from './Consignaciones.module.css';

const estadoLabels = {
  PENDIENTE: 'Pendiente',
  RENDIDO_PARCIAL: 'Rendido Parcial',
  RENDIDO_TOTAL: 'Rendido Total',
};

export default function DevolverForm({ consignacion, onClose, onSaved }) {
  const { notify, notifySuccess } = useNotify();
  const [productos, setProductos] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!consignacion) return;
    setProductos(
      consignacion.productos.map((p) => ({
        lineaConsignacionId: p.id,
        productoId: p.productoId,
        productoNombre: p.productoNombre,
        cantidadDespachada: p.cantidad,
        cantidadDevuelta: '',
      }))
    );
  }, [consignacion]);

  function updateProducto(index, value) {
    setProductos((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], cantidadDevuelta: value };
      return next;
    });
  }

  async function handleSave(e) {
    e.preventDefault();
    const valid = productos.filter((p) => Number(p.cantidadDevuelta) > 0);
    if (valid.length === 0) return;
    setSaving(true);
    try {
      await api.post('/rendiciones', {
        consignacionId: consignacion.id,
        montoEntregado: 0,
        fecha: todayStr(),
        productos: valid.map((p) => ({
          lineaConsignacionId: p.lineaConsignacionId,
          cantidadVendida: 0,
          cantidadDevuelta: Number(p.cantidadDevuelta),
        })),
      });
      onClose();
      notifySuccess('Devolución registrada correctamente');
      onSaved();
    } catch (err) {
      notify(err, 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal isOpen={!!consignacion} onClose={onClose} title={`Devolver Productos - Consignación #${consignacion?.id}`}>
      {consignacion && (
        <div>
          <p className={styles.despachoInfo}>
            <strong>{consignacion.consignatarioNombre}</strong> — {consignacion.productos.length} producto(s)
            {' | '}Estado: {estadoLabels[consignacion.estado]}
          </p>
          <form onSubmit={handleSave} className={styles.form}>
            <div className={styles.productosSection}>
              <label className={styles.sectionLabel}>Productos a devolver</label>
              {productos.map((p, i) => (
                <div key={i} className={styles.rendicionProducto}>
                  <span className={styles.rendicionProductoNombre}>{p.productoNombre}</span>
                  <span className={styles.rendicionProductoDisponible}>Disp: {p.cantidadDespachada}</span>
                  <input
                    type="number"
                    step="any"
                    min="0"
                    placeholder="Devuelto"
                    value={p.cantidadDevuelta}
                    onChange={(e) => updateProducto(i, e.target.value)}
                  />
                </div>
              ))}
            </div>
            <div className={styles.formActions}>
              <Button variant="ghost" type="button" onClick={onClose}>Cancelar</Button>
              <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Registrar Devolución'}</Button>
            </div>
          </form>
        </div>
      )}
    </Modal>
  );
}
