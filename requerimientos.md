# Requerimientos del Sistema de Gestión de Inventario

## Contexto del proyecto

Sistema de gestión de inventario para un emprendimiento de fabricación y venta de productos de higiene personal libres de sustancias tóxicas (shampoo en barra, pasta dental, desodorante, entre otros).

**Usuario:** una sola persona, que fabrica y vende los productos. Opera desde su notebook y desde su celular (acceso en la misma red Wi-Fi local).

**Tipo de solución:** SPA auto-hosteada. El servidor corre en la notebook de la usuaria; el frontend es accesible desde el navegador de cualquier dispositivo en la misma red local.

> **Pendiente de confirmar con el cliente:** si se requiere acceso desde fuera de la red local (ej. ferias, locales), el esquema de despliegue cambia significativamente. Por ahora se asume acceso solo en red local.

---

## Entidades principales

### Categoría de Materia Prima
- Nombre
- Categoría padre (opcional, para subcategorías)

### Categoría de Producto Terminado
- Nombre
- Categoría padre (opcional, para subcategorías)

> Las categorías de materias primas y productos terminados son **independientes entre sí**, ya que responden a clasificaciones de negocio distintas.

### Materia Prima
- Nombre
- Unidad de medida (gramos, ml, unidades, etc.)
- Stock actual
- Umbral de alerta de stock bajo (individual por materia prima)
- Categoría (referencia a Categoría de Materia Prima)

> El umbral de alerta es un campo propio de cada materia prima, no un valor global del sistema, ya que las escalas de stock varían mucho entre insumos (ej. gramos vs. unidades).

### Compra de Materia Prima
- Materia prima asociada
- Fecha de compra
- Cantidad comprada
- Precio pagado
- Lugar de compra (opcional, sin gestión formal de proveedores)

> Permite mantener un **historial completo de precios** por materia prima, no solo el último precio pagado, para que la usuaria pueda comparar y decidir dónde reabastecerse.

### Producto Terminado
- Nombre / variante (ej. "Shampoo en barra - cabello graso")
- Precio de venta
- Stock actual
- Umbral de alerta de stock bajo (individual por producto terminado)
- Categoría (referencia a Categoría de Producto Terminado)

> Cada variante de un producto (ej. distintos tipos de cabello) se modela como un **producto terminado independiente**, ya que la receta puede diferir entre variantes.

### Receta
- Producto terminado asociado
- Lista de materias primas requeridas, cada una con su cantidad necesaria
- Notas (texto libre para observaciones, detalles o procedimientos de fabricación)

### Producción
- Producto terminado fabricado
- Cantidad fabricada
- Fecha
- Efecto: descuenta automáticamente el stock de materias primas según la receta, y suma al stock del producto terminado.

### Venta
- Producto terminado vendido
- Cantidad vendida
- Fecha
- Precio unitario (copiado del precio de venta del producto al momento de registrar la venta)
- Efecto: descuenta automáticamente el stock del producto terminado.

> El precio unitario se guarda en cada venta, y no se recalcula a partir del precio actual del producto. Sin este dato, los reportes de ventas de un período pasado (RF-023) quedarían calculados con el precio de *hoy* en vez del precio real cobrado en ese momento — el mismo problema que ya se evitó en Compra de Materia Prima guardando el precio histórico.

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
| RF-017 | Al registrar una venta, el sistema debe descontar automáticamente el stock del producto terminado correspondiente y guardar el precio unitario vigente en ese momento. | Alta |
| RF-018 | El sistema debe impedir registrar una venta si no hay stock suficiente del producto terminado. | Media |
| RF-019 | El sistema debe mostrar un listado del stock actual de todas las materias primas y productos terminados. | Alta |
| RF-020 | El sistema debe alertar o destacar visualmente las materias primas o productos terminados cuyo stock esté por debajo de su umbral individual configurado. | Media |
| RF-021 | El sistema debe permitir consultar el historial de precios de compra de una materia prima específica. | Alta |
| RF-022 | El sistema debe permitir generar un reporte de totales de producción por período de tiempo. | Baja |
| RF-023 | El sistema debe permitir generar un reporte de totales de ventas por período de tiempo, calculado con el precio unitario histórico de cada venta. | Baja |
| RF-024 | El sistema debe permitir gestionar categorías y subcategorías independientes para materias primas. | Alta |
| RF-025 | El sistema debe permitir gestionar categorías y subcategorías independientes para productos terminados. | Alta |
| RF-026 | El sistema debe permitir filtrar materias primas por categoría y por nombre, y ordenar por stock actual, fecha y precio de última compra. | Alta |
| RF-027 | El sistema debe permitir filtrar productos terminados por categoría y por nombre, y ordenar por stock actual y precio de venta. | Alta |
| RF-028 | El sistema debe permitir agregar notas de texto libre a cada receta, para registrar observaciones, detalles o procedimientos de fabricación. | Media |
| RF-029 | El sistema debe permitir ordenar el historial de compras por fecha, precio pagado y cantidad. | Alta |
| RF-030 | El sistema debe permitir ordenar el historial de producción por fecha y cantidad fabricada. | Media |
| RF-031 | El sistema debe permitir ordenar el historial de ventas por fecha y cantidad vendida. | Media |
| RF-032 | El sistema debe permitir configurar el umbral de alerta de stock bajo de forma individual para cada materia prima y cada producto terminado. | Media |

> **Nota:** RF-022 y RF-023 quedan pendientes de mayor especificación (granularidad temporal, filtros, formato de salida) antes de pasar a diseño.

---

## Requisitos No Funcionales

| ID | Descripción |
|---|---|
| RNF-001 | El sistema debe ser accesible mediante navegador web (arquitectura SPA). |
| RNF-002 | El sistema debe poder ejecutarse íntegramente en modo local (servidor + base de datos en la misma notebook), sin depender de conexión a internet para su funcionamiento. |
| RNF-003 | El sistema debe estar pensado para un único usuario concurrente. |
| RNF-004 | El servidor debe poder iniciarse de forma simple, idealmente automática, sin intervención técnica del usuario final. |
| RNF-005 | El frontend debe ser responsive y operable desde dispositivos móviles (celular) en la misma red local. |
| RNF-006 | La notebook no debe entrar en modo de suspensión mientras el servidor esté en uso; debe configurarse explícitamente en las opciones de energía del sistema operativo. |

---

## Fuera de alcance (por ahora)

- Gestión formal de proveedores (no se requiere, la usuaria reabastece de manera informal).
- Multiusuario o roles de acceso (un solo usuario en notebook propia).
- Facturación o integración con sistemas fiscales.
- Acceso desde fuera de la red local (pendiente de confirmación con el cliente).
- Backup automático del archivo de base de datos (recomendado incorporar más adelante; ver Notas de diseño).

---

## Notas de diseño

- El modelo de datos requiere una base de datos relacional, dadas las relaciones entre materia prima, receta, producto terminado, compra, producción y venta.
- Las categorías de materias primas y productos terminados soportan jerarquía (subcategorías mediante referencia a categoría padre), lo que implica consultas recursivas o manejo de árbol en el backend. Spring Data JPA no tiene soporte nativo para árboles de profundidad arbitraria; evaluar entre una query nativa `WITH RECURSIVE` (soportada por SQLite) o resolver el árbol en memoria en la capa de servicio. **Pendiente de decidir** antes de implementar las entidades de categoría.
- **Proceso único de despliegue:** el build de producción de React (Vite) se sirve directamente desde Spring Boot (`src/main/resources/static`), en vez de correr frontend y backend como dos procesos separados. Esto simplifica el arranque automático (RNF-004), reduce a un solo puerto el acceso desde la red local, y evita tener que configurar CORS.
- **Identificación de la notebook en la red local:** la IP asignada por DHCP puede cambiar entre reconexiones de Wi-Fi, rompiendo el acceso desde el celular sin aviso previo. **Pendiente de definir con la usuaria** una de estas opciones: IP reservada en el router, hostname vía mDNS (`nombre-notebook.local`), o mostrar la IP vigente en pantalla al iniciar el servidor.
- **Firewall del sistema operativo:** al iniciar el servidor por primera vez, Windows/macOS van a solicitar confirmación para permitir conexiones entrantes en el puerto usado. Si la usuaria rechaza el permiso sin saber qué es, el acceso desde el celular falla sin error visible en la aplicación. Debe documentarse este paso en el manual de usuario final.
- **Backup:** todos los datos viven en un único archivo SQLite, en un único disco. Se recomienda incorporar como requisito de baja prioridad una copia automática periódica del archivo `.db` a un destino externo (Google Drive, pendrive, u otro disco), para evitar pérdida total de información ante falla de la notebook.

---

## Stack tecnológico

| Capa | Tecnología | Justificación |
|---|---|---|
| **Frontend** | React + Vite | Ecosistema amplio, transferible al mercado laboral, compatible con cualquier backend via HTTP. Debe implementarse con diseño responsive. |
| **Backend** | Java + Spring Boot | Lenguaje ya conocido por el desarrollador; Spring Boot es el estándar del ecosistema Java para APIs REST. |
| **Base de datos** | SQLite | Un solo usuario concurrente; no requiere servidor de base de datos separado; backup trivial (un archivo). |
| **ORM** | Spring Data JPA + Hibernate | Integración nativa con Spring Boot; evita SQL manual para operaciones CRUD estándar. |

**Arquitectura:** SPA auto-hosteada, servida como un único proceso. El backend Spring Boot expone la API REST y sirve el build estático de React desde `0.0.0.0:8080`, accesible en toda la red local. Todo ejecuta localmente en la notebook de la usuaria, sin dependencia de internet.