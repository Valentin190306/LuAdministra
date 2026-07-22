import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';
import { useApi } from '../hooks/useApi';
import Table from '../components/ui/Table';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import FormField from '../components/ui/FormField';
import Loading from '../components/ui/Loading';
import { downloadCSV } from '../utils/csv';
import styles from './Recetas.module.css';

export default function Recetas() {
  const { data: ptList, loading: ptLoading } = useApi('/productos-terminados');
  const { data: mpList } = useApi('/materias-primas');

  const [recipeMap, setRecipeMap] = useState({});
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedPt, setSelectedPt] = useState(null);
  const [creatingNew, setCreatingNew] = useState(false);
  const [newPtId, setNewPtId] = useState('');
  const [detalles, setDetalles] = useState([]);
  const [notas, setNotas] = useState('');
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(null);

  const loadRecipe = useCallback(async (ptId) => {
    try {
      const recipe = await api.get(`/recetas/producto/${ptId}`);
      setRecipeMap((prev) => ({ ...prev, [ptId]: recipe }));
      return recipe;
    } catch {
      return null;
    }
  }, []);

  function openModal(pt) {
    setCreatingNew(false);
    setNewPtId('');
    setSelectedPt(pt);
    loadRecipe(pt.id).then((recipe) => {
      if (recipe) {
        setDetalles(recipe.detalles.map((d) => ({ materiaPrimaId: d.materiaPrimaId, cantidad: d.cantidad })));
        setNotas(recipe.notas ?? '');
      } else {
        setDetalles([{ materiaPrimaId: '', cantidad: '' }]);
        setNotas('');
      }
      setModalOpen(true);
    });
  }

  function openNewRecipe() {
    setCreatingNew(true);
    setSelectedPt(null);
    setNewPtId('');
    setDetalles([{ materiaPrimaId: '', cantidad: '' }]);
    setNotas('');
    setModalOpen(true);
  }

  function addDetalle() {
    setDetalles((prev) => [...prev, { materiaPrimaId: '', cantidad: '' }]);
  }

  function updateDetalle(index, field, value) {
    setDetalles((prev) => {
      const next = [...prev];
      next[index] = { ...next[index], [field]: value };
      return next;
    });
  }

  function removeDetalle(index) {
    setDetalles((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSave(e) {
    e.preventDefault();
    const valid = detalles.filter((d) => d.materiaPrimaId && d.cantidad);
    if (valid.length === 0) return;

    setSaving(true);
    try {
      const ptId = creatingNew ? Number(newPtId) : selectedPt.id;
      const body = {
        productoTerminadoId: ptId,
        detalles: valid.map((d) => ({
          materiaPrimaId: Number(d.materiaPrimaId),
          cantidad: Number(d.cantidad),
        })),
        notas: notas.trim() || null,
      };

      const existing = !creatingNew && recipeMap[selectedPt.id];
      if (existing) {
        await api.put(`/recetas/${existing.id}`, body);
      } else {
        await api.post('/recetas', body);
      }

      setModalOpen(false);
      if (creatingNew) {
        await loadRecipe(ptId);
      } else {
        await loadRecipe(selectedPt.id);
      }
    } catch (err) {
      alert(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    try {
      const recipe = recipeMap[deleting.id];
      if (recipe) {
        await api.delete(`/recetas/${recipe.id}`);
        setRecipeMap((prev) => {
          const next = { ...prev };
          delete next[deleting.id];
          return next;
        });
      }
      setDeleting(null);
    } catch (err) {
      alert(err.message);
    }
  }

  const columns = [
    { key: 'id', label: 'ID' },
    { key: 'nombre', label: 'Producto Terminado' },
    {
      key: 'receta',
      label: 'Receta',
      render: (row) => {
        const recipe = recipeMap[row.id];
        if (!recipe) return <span className={styles.noRecipe}>Sin receta</span>;
        return <span className={styles.hasRecipe}>{recipe.detalles.length} ingredientes</span>;
      },
    },
    {
      key: 'acciones',
      label: '',
      render: (row) => (
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => openModal(row)}>
            {recipeMap[row.id] ? 'Editar Receta' : 'Configurar'}
          </Button>
          {recipeMap[row.id] && (
            <Button variant="ghost" onClick={() => setDeleting(row)}>Eliminar</Button>
          )}
        </div>
      ),
    },
  ];

  if (ptLoading) return <Loading />;

  return (
    <div>
      <div className={styles.header}>
        <h1 className={styles.pageTitle}>Recetas</h1>
        <div className={styles.headerActions}>
          <Button variant="ghost" onClick={() => {
          const flat = (ptList ?? []).map((pt) => {
            const r = recipeMap[pt.id];
            return {
              producto: pt.nombre,
              ingredientes: r ? r.detalles.map((d) => `${d.materiaPrimaNombre} (${d.cantidad})`).join('; ') : 'Sin receta',
            };
          });
          downloadCSV(flat, [
            { key: 'producto', label: 'Producto Terminado' },
            { key: 'ingredientes', label: 'Ingredientes' },
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
        <form onSubmit={handleSave} className={styles.form}>
          {creatingNew && (
            <FormField label="Producto Terminado">
              <select value={newPtId} onChange={(e) => setNewPtId(e.target.value)} required>
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
                  <select value={d.materiaPrimaId} onChange={(e) => updateDetalle(i, 'materiaPrimaId', e.target.value)} required>
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
                      required
                    />
                    {detalles.length > 1 && (
                      <button type="button" className={styles.removeBtn} onClick={() => removeDetalle(i)} aria-label="Eliminar">&times;</button>
                    )}
                  </div>
                </FormField>
              </div>
            ))}
          </div>

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
