import { api } from '../api/client';
import { exportExcel } from './xlsx';

const estadoLabels = {
  PENDIENTE: 'Pendiente',
  RENDIDO_PARCIAL: 'Rendido Parcial',
  RENDIDO_TOTAL: 'Finalizada',
};

export async function exportLibroCompleto() {
  const [
    materiasPrimas,
    productos,
    categorias,
    consignatarios,
    rendiciones,
    recetas,
    consignacionesPage,
    ventasPage,
    comprasPage,
    lotesPage,
  ] = await Promise.all([
    api.get('/materias-primas'),
    api.get('/productos'),
    api.get('/categorias'),
    api.get('/consignatarios'),
    api.get('/rendiciones'),
    api.get('/recetas'),
    api.get('/consignaciones?page=0&size=100000'),
    api.get('/ventas?page=0&size=100000'),
    api.get('/compras?page=0&size=100000'),
    api.get('/lotes?page=0&size=100000'),
  ]);

  const consignaciones = consignacionesPage.content;
  const ventas = ventasPage.content;
  const compras = comprasPage.content;
  const lotes = lotesPage.content;

  const consignacionLineas = consignaciones.flatMap((c) =>
    (c.productos ?? []).map((l) => ({
      consignacionId: c.id,
      consignatarioNombre: c.consignatarioNombre,
      fecha: c.fecha,
      producto: l.productoNombre,
      cantidad: l.cantidad,
      precioUnitario: l.precioUnitario,
      subtotal: (l.cantidad ?? 0) * (l.precioUnitario ?? 0),
    }))
  );

  const rendicionLineas = rendiciones.flatMap((r) =>
    (r.productos ?? []).map((l) => ({
      rendicionId: r.id,
      consignacionId: r.consignacionId,
      consignatarioNombre: r.consignatarioNombre,
      producto: l.productoNombre,
      cantidadVendida: l.cantidadVendida,
      cantidadDevuelta: l.cantidadDevuelta,
    }))
  );

  const ventaLineas = ventas.flatMap((v) =>
    (v.lineas ?? []).map((l) => ({
      ventaId: v.id,
      fecha: v.fecha,
      producto: l.productoNombre,
      cantidad: l.cantidad,
      precioUnitario: l.precioUnitario,
      subtotal: (l.cantidad ?? 0) * (l.precioUnitario ?? 0),
    }))
  );

  const recetaDetalles = recetas.flatMap((r) =>
    (r.detalles ?? []).map((d) => ({
      recetaId: r.id,
      productoNombre: r.productoNombre,
      materiaPrimaNombre: d.materiaPrimaNombre,
      cantidad: d.cantidad,
    }))
  );

  const dateStr = new Date().toISOString().slice(0, 10);

  exportExcel({
    'Materias Primas': {
      columns: [
        { key: 'nombre', label: 'Nombre' },
        { key: 'categoriaNombre', label: 'Categoría' },
        { key: 'unidadMedida', label: 'Unidad de Medida' },
        { key: 'stockActual', label: 'Stock Actual' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ],
      data: materiasPrimas,
    },
    'Productos': {
      columns: [
        { key: 'nombre', label: 'Nombre' },
        { key: 'categoriaNombre', label: 'Categoría' },
        { key: 'precioVenta', label: 'Precio de Venta' },
        {
          key: 'enDeposito',
          label: 'En Depósito',
          value: (p) => p.stockActual - (p.stockConsignado ?? 0),
        },
        { key: 'stockConsignado', label: 'Consignado' },
        { key: 'stockMinimo', label: 'Stock Mínimo' },
      ],
      data: productos,
    },
    'Categorías': {
      columns: [
        { key: 'nombre', label: 'Nombre' },
        { key: 'tipo', label: 'Tipo' },
        { key: 'categoriaPadreNombre', label: 'Categoría Padre' },
      ],
      data: categorias,
    },
    'Consignatarias': {
      columns: [
        { key: 'nombre', label: 'Nombre' },
        { key: 'contacto', label: 'Contacto' },
      ],
      data: consignatarios,
    },
    'Consignaciones': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'consignatarioNombre', label: 'Consignataria' },
        { key: 'fecha', label: 'Fecha' },
        {
          key: 'estado',
          label: 'Estado',
          value: (c) => estadoLabels[c.estado] ?? c.estado,
        },
        {
          key: 'cantProductos',
          label: 'Cant. Productos',
          value: (c) => (c.productos ?? []).length,
        },
      ],
      data: consignaciones,
    },
    'Consignaciones Productos': {
      columns: [
        { key: 'consignacionId', label: 'N° Consignación' },
        { key: 'consignatarioNombre', label: 'Consignataria' },
        { key: 'fecha', label: 'Fecha' },
        { key: 'producto', label: 'Producto' },
        { key: 'cantidad', label: 'Cantidad' },
        { key: 'precioUnitario', label: 'Precio Unitario' },
        { key: 'subtotal', label: 'Subtotal' },
      ],
      data: consignacionLineas,
    },
    'Rendiciones': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'consignacionId', label: 'N° Consignación' },
        { key: 'consignatarioNombre', label: 'Consignataria' },
        { key: 'fecha', label: 'Fecha' },
        { key: 'montoEntregado', label: 'Monto Entregado' },
      ],
      data: rendiciones,
    },
    'Rendiciones Productos': {
      columns: [
        { key: 'rendicionId', label: 'N° Rendición' },
        { key: 'consignacionId', label: 'N° Consignación' },
        { key: 'consignatarioNombre', label: 'Consignataria' },
        { key: 'producto', label: 'Producto' },
        { key: 'cantidadVendida', label: 'Vendido' },
        { key: 'cantidadDevuelta', label: 'Devuelto' },
      ],
      data: rendicionLineas,
    },
    'Ventas': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'fecha', label: 'Fecha' },
        {
          key: 'cantProductos',
          label: 'Cant. Productos',
          value: (v) => (v.lineas ?? []).length,
        },
        { key: 'total', label: 'Total' },
      ],
      data: ventas,
    },
    'Ventas Productos': {
      columns: [
        { key: 'ventaId', label: 'N° Venta' },
        { key: 'fecha', label: 'Fecha' },
        { key: 'producto', label: 'Producto' },
        { key: 'cantidad', label: 'Cantidad' },
        { key: 'precioUnitario', label: 'Precio Unitario' },
        { key: 'subtotal', label: 'Subtotal' },
      ],
      data: ventaLineas,
    },
    'Compras': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'materiaPrimaNombre', label: 'Materia Prima' },
        { key: 'fecha', label: 'Fecha' },
        { key: 'cantidad', label: 'Cantidad' },
        { key: 'precio', label: 'Precio' },
        { key: 'total', label: 'Total', value: (c) => (c.cantidad ?? 0) * (c.precio ?? 0) },
        { key: 'lugar', label: 'Lugar' },
        { key: 'url', label: 'URL' },
        { key: 'precioMLReferencia', label: 'Precio Ref. ML' },
      ],
      data: compras,
    },
    'Lotes': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'productoNombre', label: 'Producto' },
        { key: 'fecha', label: 'Fecha' },
        { key: 'cantidadFabricada', label: 'Cantidad Fabricada' },
        { key: 'diasVigencia', label: 'Días de Vigencia' },
        { key: 'fechaVencimiento', label: 'Fecha de Vencimiento' },
      ],
      data: lotes,
    },
    'Recetas': {
      columns: [
        { key: 'id', label: 'N°' },
        { key: 'productoNombre', label: 'Producto' },
        { key: 'notas', label: 'Notas' },
      ],
      data: recetas,
    },
    'Recetas Detalles': {
      columns: [
        { key: 'recetaId', label: 'N° Receta' },
        { key: 'productoNombre', label: 'Producto' },
        { key: 'materiaPrimaNombre', label: 'Materia Prima' },
        { key: 'cantidad', label: 'Cantidad' },
      ],
      data: recetaDetalles,
    },
  }, `luadministra-${dateStr}.xlsx`);
}
