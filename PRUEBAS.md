# Pruebas manuales — LuAdministra

Checklist de funciones para probar la app a mano. Cada caso indica qué hacer y qué se espera ver. Marcar con `[x]` cuando pase.

## 1. Dashboard (`/dashboard`)

- [x] Se ven las tarjetas (Materias Primas, Productos, Alertas) y las tablas de stock.
- [x] Un ítem con `stockActual < stockMinimo` aparece en "Alertas de stock mínimo".
- [x] Un lote vencido con stock > 0 aparece en "Alertas de vencimiento" (nombre, fecha, lote).
- [x] "Exportar MP" descarga un CSV con materias primas.
- [x] "Exportar PT" descarga un CSV con productos.

## 2. Categorías (`/categorias`)

- [x] Crear categoría de Materia Prima → aparece en la tab correcta.
- [x] Crear categoría de Producto → aparece en la tab correcta.
- [x] Crear categoría con categoría padre → se ve la jerarquía.
- [x] Guardar sin nombre → error de validación.
- [x] Editar nombre → se refleja.
- [x] **Eliminar categoría asociada a una MP/producto** → se borra y la MP/producto queda sin categoría (no debe dar error 500).
- [X] **Eliminar categoría con subcategorías** → se borra y las subcategorías pasan a ser raíz.
- [X] Eliminar categoría sin referencias → se borra normalmente.
- [X] Cambiar de tab (MP/Producto) filtra la lista.
- [X] Exportar CSV.

## 3. Materias Primas (`/materias-primas`)

- [x] Crear con nombre, unidad, stock, mínimo y categoría.
- [x] Crear solo con nombre + unidad → stock queda en 0.
- [x] Guardar sin nombre / sin unidad → error.
- [x] Buscar por nombre filtra en tiempo real.
- [x] Filtrar por categoría.
- [x] Ordenar por columnas.
- [x] Editar stock/mínimo/categoría → se actualiza.
- [x] Stock negativo → error.
- [x] Eliminar sin referencias → ok.
- [x] Eliminar MP usada en una receta → error de integridad (o comportamiento definido).
- [x] Exportar CSV.

## 4. Compras (`/compras`)

- [x] Crear compra (MP + fecha + cantidad + precio) → stock de la MP aumenta.
- [x] Crear con URL y precio ML → se guardan.
- [x] Crear con lugar → se muestra.
- [x] Cantidad negativa / sin MP → error.
- [x] Filtrar por MP → muestra historial de precios.
- [x] Más de 50 registros → scroll infinito carga más.
- [x] Ordenar por fecha/precio.
- [x] Eliminar compra → stock de la MP se revierte.
- [x] Exportar CSV.

## 5. Productos (`/productos`)

- [x] Crear con nombre, precio, categoría → stock 0.
- [x] Crear con stock inicial y mínimo.
- [x] Sin precio / precio negativo → error.
- [x] Buscar y filtrar por categoría.
- [x] Editar precio/stock.
- [x] Eliminar sin dependencias → ok.
- [x] Eliminar con receta asociada → error.
- [x] "Ver vencimientos" muestra los lotes del producto con sus fechas.
- [x] Exportar CSV.

## 6. Recetas (`/recetas`)

- [x] Crear receta con 1 ingrediente.
- [x] Crear con 3+ ingredientes.
- [x] Sin ingredientes → error.
- [x] Editar (agregar/quitar/modificar).
- [x] Producto que ya tiene receta → se reemplaza o bloquea.
- [x] Eliminar receta → el producto queda sin receta.
- [x] Exportar CSV.

## 7. Lotes (`/lotes`)

- [x] Crear lote → stock del producto aumenta.
- [x] Con `diasVigencia` → se calcula fecha de vencimiento.
- [x] Filtrar por período de fechas.
- [x] Cantidad 0/negativa, o sin producto → error.
- [x] Eliminar lote → stock se revierte.
- [x] Scroll infinito con +50 registros.
- [x] Exportar CSV.

## 8. Ventas (`/ventas`)

- [x] Venta de 1 producto → stock se descuenta.
- [x] Venta con 3+ productos → todos los stocks bajan.
- [ ] Precio unitario personalizado.
- [x] Sin líneas / stock insuficiente / cantidad negativa → error.
- [x] Filtrar por período.
- [x] Ver detalle en modal.
- [x] Eliminar venta → stock se restaura.
- [x] Scroll infinito.
- [x] Exportar CSV.

## 9. Consignatarios (`/consignatarios`)

- [x] Crear con nombre y contacto.
- [x] Crear solo con nombre.
- [x] Sin nombre → error.
- [x] Editar.
- [x] Eliminar sin consignaciones → ok.
- [ ] Eliminar con consignaciones activas → error.
- [ ] Exportar CSV.

## 10. Consignaciones (`/consignaciones`)

- [ ] Crear consignación → stock consignado del producto aumenta.
- [ ] Con múltiples productos.
- [ ] Sin productos / sin stock suficiente → error.
- [ ] Filtrar por consignatario.
- [ ] Filtrar por estado (PENDIENTE / RENDIDO_PARCIAL / RENDIDO_TOTAL).
- [ ] Eliminar PENDIENTE → stock se restaura.
- [ ] Eliminar RENDIDO_PARCIAL → bloqueado o restauración consistente.
- [ ] Modal "Rendiciones" lista las rendiciones de la consignación.
- [ ] Modal "Stock en consignación" muestra Consignado / Rendido / Pendiente.
- [ ] Exportar CSV.

## 11. Rendiciones / Devoluciones

- [ ] Rendir parcial (6 de 10) → estado RENDIDO_PARCIAL + montoEntregado.
- [ ] Rendir total (10 de 10) → RENDIDO_TOTAL.
- [ ] Devolver (4 unidades) → stock restaurado.
- [ ] Mixto: 6 vendidas + 2 devueltas de 10.
- [ ] Rendir más de lo consignado → error.
- [ ] Sin líneas → error.
- [ ] Rendir una consignación RENDIDO_TOTAL → bloqueado.

## 12. General

- [ ] Recargar (F5) en `/lotes`, `/ventas`, etc. → la página carga sin 404.
- [ ] Acceso desde otro dispositivo por `http://LuAdministra.local:8080`.
- [ ] Modo mobile en DevTools → layout se adapta.
- [ ] Toasts de éxito/error aparecen y desaparecen.
- [ ] Con `--spring.profiles.active=prod` el seeder NO corre.
- [ ] Eliminar compra/lote/venta/consignación y verificar que el stock queda consistente.
