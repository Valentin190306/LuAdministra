import { useState, useCallback, useMemo } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import ActionMenu from '../components/ui/ActionMenu';
import Modal from '../components/ui/Modal';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Recetas.module.css';

export default function Recetas() {
  const { data: ptList, loading: ptLoading } = useApi('/productos');
  const { data: mpList } = useApi('/materias-primas');
  const { data: allRecipes } = useApi('/recetas');

  const recipeLookup = useMemo(() => Object.fromEntries((allRecipes ?? []).map((r) => [r.productoId, r])), [allRecipes]);
  const mpLookup = useMemo(() => Object.fromEntries((mpList ?? []).map((mp) => [mp.id, mp.unidadMedida])), [mpList]);

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedPt, setSelectedPt] = useState(null);
  const [creatingNew, setCreatingNew] = useState(false);
  const [newPtId, setNewPtId] = useState('');
  const [detalles, setDetalles] = useState([]);
  const [notas, setNotas] = useState('');
  const [errores, setErrores] = useState({});
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(null);

  const loadRecipe = useCallback(async (ptId) => {
    try {
      return await api.get(`/recetas/producto/${ptId}`);
    } catch {
      return null;
    }
  }, []);

  const openModal = useCallback((pt) => {
    setCreatingNew(false);
    setNewPtId('');
    setSelectedPt(pt);
    setErrores({});
    loadRecipe(pt.id).then((recipe) => {
      if (recipe) {
        setDetalles(recipe.detalles.map((d) => ({ materiaPrimaId: d.materiaPrimaId, cantidad: d.cantidad })));
        setNotas(recipe.notas ?? '');
      } else {
        setDetalles([{ materiaPrimaId: '', cantidad: '' }]);
        setNotas('');
      }
      setModalOpen(true);
    }).catch(() => {
      setDetalles([{ materiaPrimaId: '', cantidad: '' }]);
      setNotas('');
      setModalOpen(true);
    });
  }, [loadRecipe]);

  const openNewRecipe = useCallback(() => {
    setCreatingNew(true);
    setSelectedPt(null);
    setNewPtId('');
    setDetalles([{ materiaPrimaId: '', cantidad: '' }]);
    setNotas('');
    setErrores({});
    setModalOpen(true);
  }, []);

  const addDetalle = useCallback(() => {
    setDetalles((prev) => [...prev, { materiaPrimaId: '', cantidad: '' }]);
    setErrores((prev) => ({ ...prev, detalles: undefined }));
  }, []);

  const updateDetalle = useCallback((index, field, value) => {
    setDetalles((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
    setErrores((prev) => ({ ...prev, detalles: undefined }));
  }, []);

  const removeDetalle = useCallback((index) => {
    setDetalles((prev) => prev.filter((_, i) => i !== index));
  }, []);

  function validar() {
    const e = {};
    if (creatingNew && !newPtId) e.newPtId = 'Requerido';
    const valid = detalles.filter((d) => d.materiaPrimaId && d.cantidad);
    if (valid.length === 0) e.detalles = 'Agregue al menos un ingrediente con cantidad';
    setErrores(e);
    return Object.keys(e).length === 0;
  }

  const handleSave = useCallback(async (e) => {
    e.preventDefault();
    if (!validar()) return;

    setSaving(true);
    try {
      const ptId = creatingNew ? Number(newPtId) : selectedPt.id;
      const valid = detalles.filter((d) => d.materiaPrimaId && d.cantidad);
      const body = {
        productoId: ptId,
        detalles: valid.map((d) => ({
          materiaPrimaId: Number(d.materiaPrimaId),
          cantidad: Number(d.cantidad),
        })),
        notas: notas.trim() || null,
      };

      const existing = !creatingNew && recipeLookup[selectedPt.id];
      if (existing) {
        await api.put(`/recetas/${existing.id}`, body);
      } else {
        await api.post('/recetas', body);
      }

      setModalOpen(false);
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }, [creatingNew, newPtId, selectedPt, detalles, notas, recipeLookup]);

  const handleDelete = useCallback(async () => {
    if (!deleting) return;
    try {
      const recipe = recipeLookup[deleting.id];
      if (recipe) {
        await api.delete(`/recetas/${recipe.id}`);
      }
      setDeleting(null);
    } catch (err) {
      alert(err.message);
    }
  }, [deleting, recipeLookup]);

  const columns = useMemo(() => [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Producto Terminado' },
    {
      key: 'receta',
      label: 'Receta',
      render: (row) => {
        const recipe = recipeLookup[row.id];
        if (!recipe) return <span className={styles.noRecipe}>Sin receta</span>;
        return <span className={styles.hasRecipe}>{recipe.detalles.length} ingredientes</span>;
      },
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => {
        const items = [];
          items.push({ label: recipeLookup[row.id] ? 'Editar Receta' : 'Configurar', onClick: () => openModal(row) });
        if (recipeLookup[row.id]) items.push({ label: 'Eliminar', onClick: () => setDeleting(row) });
        return <ActionMenu actions={items} />;
      },
    },
  ], [recipeLookup, openModal]);

  if (ptLoading) return <Loading />;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Recetas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => {
          const flat = (ptList ?? []).map((pt) => {
            const r = recipeLookup[pt.id];
            return {
              producto: pt.nombre,
              ingredientes: r ? r.detalles.map((d) => `${d.materiaPrimaNombre} (${d.cantidad} ${mpLookup[d.materiaPrimaId] ?? ''})`).join('; ') : 'Sin receta',
              notas: r?.notas ?? '',
            };
          });
          downloadCSV(flat, [
            { key: 'producto', label: 'Producto Terminado' },
            { key: 'ingredientes', label: 'Ingredientes' },
            { key: 'notas', label: 'Notas' },
          ], 'recetas.csv');
          }}>Exportar CSV</Button>
          <Button onClick={openNewRecipe}>Nueva Receta</Button>
        </div>
      </div>

      <p className={styles.hint}>Seleccioná un producto terminado para definir su receta (materias primas y cantidades necesarias).</p>

      <Table
        columns={columns}
        data={ptList}
        emptyMessage="No hay productos terminados. Creá uno primero."
      />

      <Modal isOpen={modalOpen} onClose={() => { setModalOpen(false); setCreatingNew(false); }} title={creatingNew ? 'Nueva Receta' : (selectedPt?.nombre ?? 'Receta')}>
        <form onSubmit={handleSave} className={styles.form} noValidate>
          {creatingNew && (
            <FormField label="Producto Terminado" error={errores.newPtId}>
              <select value={newPtId} onChange={(e) => { setNewPtId(e.target.value); setErrores((prev) => ({ ...prev, newPtId: undefined })); }}>
                <option value="">Seleccionar...</option>
                {ptList?.map((pt) => (
                  <option key={pt.id} value={pt.id}>{pt.nombre}</option>
                ))}
              </select>
            </FormField>
          )}
          <div className={styles.detalles}>
            {detalles.map((d, i) => (
              <div key={i} className={styles.detalleRow}>
                <FormField label={i === 0 ? 'Materia Prima' : undefined}>
                  <select value={d.materiaPrimaId} onChange={(e) => updateDetalle(i, 'materiaPrimaId', e.target.value)}>
                    <option value="">Seleccionar...</option>
                    {mpList?.map((mp) => (
                      <option key={mp.id} value={mp.id}>{mp.nombre} ({mp.unidadMedida})</option>
                    ))}
                  </select>
                </FormField>
                <FormField label={i === 0 ? 'Cantidad' : undefined}>
                  <div className={styles.cantidadRow}>
                    <input
                      type="number"
                      step="any"
                      min="0"
                      value={d.cantidad}
                      onChange={(e) => updateDetalle(i, 'cantidad', e.target.value)}
                    />
                    {detalles.length > 1 && (
                      <button type="button" className={styles.removeBtn} onClick={() => removeDetalle(i)} aria-label="Eliminar">&times;</button>
                    )}
                  </div>
                </FormField>
              </div>
            ))}
          </div>

          {errores.detalles && <p className={styles.errorMsg}>{errores.detalles}</p>}

          <Button variant="ghost" type="button" onClick={addDetalle}>+ Agregar ingrediente</Button>

          <FormField label="Notas (opcional)">
            <textarea
              className={styles.notasInput}
              value={notas}
              onChange={(e) => setNotas(e.target.value)}
              rows={3}
              placeholder="Observaciones, procedimiento de fabricación, etc."
            />
          </FormField>

          <div className={styles.formActions}>
            <Button variant="ghost" type="button" onClick={() => setModalOpen(false)}>Cancelar</Button>
            <Button type="submit" disabled={saving}>{saving ? 'Guardando...' : 'Guardar Receta'}</Button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={!!deleting} onClose={() => setDeleting(null)} title="Eliminar Receta">
        <p className={styles.confirmText}>¿Eliminar la receta de "{deleting?.nombre}"?</p>
        <div className={styles.formActions}>
          <Button variant="ghost" onClick={() => setDeleting(null)}>Cancelar</Button>
          <Button variant="danger" onClick={handleDelete}>Eliminar</Button>
        </div>
      </Modal>
    </div>
  );
}
