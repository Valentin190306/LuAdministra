# Requerimientos del Sistema de Gestión de Inventario

## Contexto del proyecto

Sistema de gestión de inventario para un emprendimiento de fabricación y venta de productos de higiene personal libres de sustancias tóxicas (shampoo en barra, pasta dental, desodorante, entre otros).

**Usuario:** una sola persona, que fabrica y vende los productos, opera desde su propia notebook.

**Tipo de solución:** SPA auto-hosteada (servidor corriendo localmente en la notebook, acceso vía navegador).

---

## Entidades principales

### Materia Prima
- Nombre
- Unidad de medida (gramos, ml, unidades, etc.)
- Stock actual

### Compra de Materia Prima
- Materia prima asociada
- Fecha de compra
- Cantidad comprada
- Precio pagado
- Lugar/proveedor de compra (opcional, sin gestión formal de proveedores)

> Permite mantener un **historial completo de precios** por materia prima, no solo el último precio pagado, para que la usuaria pueda comparar y decidir dónde reabastecerse.

### Producto Terminado
- Nombre / variante (ej. "Shampoo en barra - cabello graso")
- Precio de venta
- Stock actual

> Cada variante de un producto (ej. distintos tipos de cabello) se modela como un **producto terminado independiente**, ya que la receta puede diferir entre variantes.

### Receta
- Producto terminado asociado
- Lista de materias primas requeridas, cada una con su cantidad necesaria

### Producción
- Producto terminado fabricado
- Cantidad fabricada
- Fecha
- Efecto: descuenta automáticamente el stock de materias primas según la receta, y suma al stock del producto terminado.

### Venta
- Producto terminado vendido
- Cantidad vendida
- Fecha
- Efecto: descuenta automáticamente el stock del producto terminado.

---

## Requisitos Funcionales

| ID | Descripción | Prioridad |
|---|---|---|
| RF-001 | El sistema debe permitir registrar una nueva materia prima, indicando nombre y unidad de medida. | Alta |
| RF-002 | El sistema debe permitir modificar y eliminar materias primas existentes. | Alta |
| RF-003 | El sistema debe permitir consultar el stock actual de cada materia prima. | Alta |
| RF-004 | El sistema debe permitir registrar una compra de materia prima, indicando cantidad, precio pagado, fecha y lugar de compra (opcional). | Alta |
| RF-005 | El sistema debe actualizar automáticamente el stock de la materia prima al registrar una compra. | Alta |
| RF-006 | El sistema debe mantener un historial completo de compras por materia prima, incluyendo precios históricos. | Alta |
| RF-007 | El sistema debe permitir registrar un nuevo producto terminado, indicando nombre/variante y precio de venta. | Alta |
| RF-008 | El sistema debe permitir modificar y eliminar productos terminados existentes. | Alta |
| RF-009 | El sistema debe permitir consultar el stock actual de cada producto terminado. | Alta |
| RF-010 | El sistema debe permitir definir una receta para cada producto terminado, asociando una o más materias primas con su cantidad necesaria. | Alta |
| RF-011 | El sistema debe permitir modificar la receta de un producto terminado existente. | Media |
| RF-012 | El sistema debe permitir registrar un evento de producción, indicando producto terminado y cantidad fabricada. | Alta |
| RF-013 | Al registrar una producción, el sistema debe descontar automáticamente el stock de cada materia prima involucrada, según la receta correspondiente. | Alta |
| RF-014 | Al registrar una producción, el sistema debe incrementar automáticamente el stock del producto terminado fabricado. | Alta |
| RF-015 | El sistema debe impedir registrar una producción si no hay stock suficiente de alguna materia prima requerida. | Media |
| RF-016 | El sistema debe permitir registrar una venta, indicando producto terminado y cantidad vendida. | Alta |
| RF-017 | Al registrar una venta, el sistema debe descontar automáticamente el stock del producto terminado correspondiente. | Alta |
| RF-018 | El sistema debe impedir registrar una venta si no hay stock suficiente del producto terminado. | Media |
| RF-019 | El sistema debe mostrar un listado del stock actual de todas las materias primas y productos terminados. | Alta |
| RF-020 | El sistema debe alertar o destacar visualmente las materias primas o productos terminados con stock por debajo de un umbral definido. | Media |
| RF-021 | El sistema debe permitir consultar el historial de precios de compra de una materia prima específica. | Alta |
| RF-022 | El sistema debe permitir generar un reporte de totales de producción por período de tiempo. | Baja |
| RF-023 | El sistema debe permitir generar un reporte de totales de ventas por período de tiempo. | Baja |

> **Nota:** RF-022 y RF-023 quedan pendientes de mayor especificación (granularidad temporal, filtros, formato de salida) antes de pasar a diseño.

## Requisitos No Funcionales

| ID | Descripción |
|---|---|
| RNF-001 | El sistema debe ser accesible mediante navegador web (arquitectura SPA). |
| RNF-002 | El sistema debe poder ejecutarse íntegramente en modo local (servidor + base de datos en la misma notebook), sin depender de conexión a internet para su funcionamiento. |
| RNF-003 | El sistema debe estar pensado para un único usuario concurrente. |
| RNF-004 | El servidor debe poder iniciarse de forma simple, idealmente automática, sin intervención técnica del usuario final. |

---

## Fuera de alcance (por ahora)

- Gestión formal de proveedores (no se requiere, la usuaria reabastece de manera informal).
- Multiusuario o roles de acceso (un solo usuario en notebook propia).
- Facturación o integración con sistemas fiscales.

---

## Notas de diseño

- El modelo de datos requiere una base de datos relacional, dadas las relaciones entre materia prima, receta, producto terminado, compra, producción y venta.

---

## Stack tecnológico

| Capa | Tecnología | Justificación |
|---|---|---|
| **Frontend** | React + Vite | Ecosistema amplio, transferible al mercado laboral, compatible con cualquier backend via HTTP. |
| **Backend** | Java + Spring Boot | Lenguaje ya conocido por el desarrollador; Spring Boot es el estándar del ecosistema Java para APIs REST. |
| **Base de datos** | SQLite | Un solo usuario concurrente; no requiere servidor de base de datos separado; backup trivial (un archivo). |
| **ORM** | Spring Data JPA + Hibernate | Integración nativa con Spring Boot; evita SQL manual para operaciones CRUD estándar. |

**Arquitectura:** SPA auto-hosteada. El backend expone una API REST en `localhost:8080`; el frontend corre en `localhost:3000` y consume esa API vía HTTP. Todo ejecuta localmente en la notebook de la usuaria, sin dependencia de internet.