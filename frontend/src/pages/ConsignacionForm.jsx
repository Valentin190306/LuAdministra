import { useState } from 'react';
import { api } from '../api/client';
import Modal from '../components/ui/Modal';
import Button from '../components/ui/Button';
import FormField from '../components/ui/FormField';
import { useNotify } from '../context/NotificationContext';
import { todayStr } from '../utils/dates';
import styles from './Consignaciones.module.css';

const emptyForm = { consignatarioId: '', fecha: todayStr(), productos: [{ productoId: '', cantidad: '' }] };

export default function ConsignacionForm({ isOpen, onClose, consignatarios, productos, onSaved }) {
  const { notify, notifySuccess } = useNotify();
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

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
      setForm(emptyForm);
      onClose();
      notifySuccess('Consignación creada correctamente');
      onSaved();
    } catch (err) {
      notify(err, 'error');
    } finally {
      setSaving(false);
    }
  }

  function handleClose() {
    setForm(emptyForm);
    onClose();
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Nueva Consignación">
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
          <Button variant="ghost" type="button" onClick={handleClose}>Cancelar</Button>
          <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</Button>
        </div>
      </form>
    </Modal>
  );
}
