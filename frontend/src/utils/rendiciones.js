import { api } from '../api/client';

export async function rendidoPorLinea(consignacionId) {
  const rendiciones = await api.get(`/rendiciones/consignacion/${consignacionId}`);
  const rendido = new Map();
  rendiciones.forEach((r) => {
    r.productos.forEach((p) => {
      const previo = rendido.get(p.lineaConsignacionId) ?? 0;
      rendido.set(p.lineaConsignacionId, previo + p.cantidadVendida + p.cantidadDevuelta);
    });
  });
  return rendido;
}