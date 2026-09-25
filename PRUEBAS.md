# Pruebas manuales — LuAdministra

Checklist de funciones para probar la app a mano. Cada caso indica qué hacer y qué se espera ver. Marcar con `[x]` cuando pase.

## 1. Dashboard (`/dashboard`)

- [ ] Se ven las tarjetas (Materias Primas, Productos, Alertas) y las tablas de stock.
- [ ] Un ítem con `stockActual < stockMinimo` aparece en "Alertas de stock mínimo".
- [ ] Un lote vencido con stock > 0 aparece en "Alertas de vencimiento" (nombre, fecha, lote).
- [ ] "Exportar MP" descarga un CSV con materias primas.
- [ ] "Exportar PT" descarga un CSV con productos.

## 2. Categorías (`/categorias`)

- [ ] Crear categoría de Materia Prima → aparece en la tab correcta.
- [ ] Crear categoría de Producto → aparece en la tab correcta.
- [ ] Crear categoría con categoría padre → se ve la jerarquía.
- [ ] Guardar sin nombre → error de validación.
- [ ] Editar nombre → se refleja.
- [ ] **Eliminar categoría asociada a una MP/producto** → se borra y la MP/producto queda sin categoría (no debe dar error 500).
- [ ] **Eliminar categoría con subcategorías** → se borra y las subcategorías pasan a ser raíz.
- [ ] Eliminar categoría sin referencias → se borra normalmente.
- [ ] Cambiar de tab (MP/Producto) filtra la lista.
- [ ] Exportar CSV.

## 3. Materias Primas (`/materias-primas`)

- [ ] Crear con nombre, unidad, stock, mínimo y categoría.
- [ ] Crear solo con nombre + unidad → stock queda en 0.
- [ ] Guardar sin nombre / sin unidad → error.
- [ ] Buscar por nombre filtra en tiempo real.
- [ ] Filtrar por categoría.
- [ ] Ordenar por columnas.
- [ ] Editar stock/mínimo/categoría → se actualiza.
- [ ] Stock negativo → error.
- [ ] Eliminar sin referencias → ok.
- [ ] Eliminar MP usada en una receta → error de integridad (o comportamiento definido).
- [ ] Exportar CSV.

## 4. Compras (`/compras`)

- [ ] Crear compra (MP + fecha + cantidad + precio) → stock de la MP aumenta.
- [ ] Crear con URL y precio ML → se guardan.
- [ ] Crear con lugar → se muestra.
- [ ] Cantidad negativa / sin MP → error.
- [ ] Filtrar por MP → muestra historial de precios.
- [ ] Más de 50 registros → scroll infinito carga más.
- [ ] Ordenar por fecha/precio.
- [ ] Eliminar compra → stock de la MP se revierte.
- [ ] Exportar CSV.

## 5. Productos (`/productos`)

- [ ] Crear con nombre, precio, categoría → stock 0.
- [ ] Crear con stock inicial y mínimo.
- [ ] Sin precio / precio negativo → error.
- [ ] Buscar y filtrar por categoría.
- [ ] Editar precio/stock.
- [ ] Eliminar sin dependencias → ok.
- [ ] Eliminar con receta asociada → error.
- [ ] "Ver vencimientos" muestra los lotes del producto con sus fechas.
- [ ] Exportar CSV.

## 6. Recetas (`/recetas`)

- [ ] Crear receta con 1 ingrediente.
- [ ] Crear con 3+ ingredientes.
- [ ] Sin ingredientes → error.
- [ ] Editar (agregar/quitar/modificar).
- [ ] Producto que ya tiene receta → se reemplaza o bloquea.
- [ ] Eliminar receta → el producto queda sin receta.
- [ ] Exportar CSV.

## 7. Lotes (`/lotes`)

- [ ] Crear lote → stock del producto aumenta.
- [ ] Con `diasVigencia` → se calcula fecha de vencimiento.
- [ ] Filtrar por período de fechas.
- [ ] Cantidad 0/negativa, o sin producto → error.
- [ ] Eliminar lote → stock se revierte.
- [ ] Scroll infinito con +50 registros.
- [ ] Exportar CSV.

## 8. Ventas (`/ventas`)

- [ ] Venta de 1 producto → stock se descuenta.
- [ ] Venta con 3+ productos → todos los stocks bajan.
- [ ] Precio unitario personalizado.
- [ ] Sin líneas / stock insuficiente / cantidad negativa → error.
- [ ] Filtrar por período.
- [ ] Ver detalle en modal.
- [ ] Eliminar venta → stock se restaura.
- [ ] Scroll infinito.
- [ ] Exportar CSV.

## 9. Consignatarios (`/consignatarios`)

- [ ] Crear con nombre y contacto.
- [ ] Crear solo con nombre.
- [ ] Sin nombre → error.
- [ ] Editar.
- [ ] Eliminar sin consignaciones → ok.
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
