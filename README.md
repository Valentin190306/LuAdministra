# LuAdministra

Sistema de gestión de inventario para un emprendimiento de fabricación y venta de productos de higiene personal. SPA auto-hosteada: un solo proceso sirve el frontend y la API REST, accesible desde cualquier dispositivo de la red local.

## Características

- **Gestión de stock** de materias primas y productos terminados con umbrales de alerta individuales.
- **Compras** de materias primas con historial completo de precios y precio de referencia de Mercado Libre.
- **Recetas** que vinculan cada producto con sus materias primas y cantidades.
- **Producción (lotes)** que descuenta materias primas según la receta, suma stock del producto y calcula fecha de vencimiento.
- **Ventas** con múltiples líneas y precios históricos por línea.
- **Consignaciones** a consignatarias con rendiciones y devoluciones, y stock desglosado entre "en depósito" y "consignado".
- **Dashboard** con resumen de stock, alertas de stock mínimo y alertas de lotes vencidos.
- **Exportación CSV** en todas las tablas.
- **Paginación infinita** en las tablas con volumen creciente (compras, lotes, ventas, consignaciones).
- **Descubrimiento en red local** mediante mDNS (`http://LuAdministra.local:8080`).

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | React 19 + Vite |
| Backend | Java 25 + Spring Boot 3.4.4 |
| Base de datos | SQLite (archivo único) |
| ORM | Spring Data JPA + Hibernate |
| Documentación API | springdoc-openapi (Swagger UI) |
| Descubrimiento de red | jmDNS |

## Estructura del proyecto

```
LuAdministra/
├── backend/                 # Spring Boot (API REST + estáticos + SQLite)
│   └── src/main/java/com/luadministra/
│       ├── categoria/       # Categorías (MP y Producto)
│       ├── compra/          # Compras de materia prima
│       ├── consignacion/    # Consignaciones y líneas
│       ├── consignatario/   # Consignatarias
│       ├── config/          # DataSeeder, CORS, mDNS, SPA routing
│       ├── dashboard/       # Resumen de stock y alertas
│       ├── dto/             # PaginatedResponse
│       ├── exception/       # Manejo global de errores
│       ├── lote/            # Producción / lotes
│       ├── materiaprima/    # Materias primas
│       ├── producto/        # Productos terminados
│       ├── receta/          # Recetas
│       ├── rendicion/       # Rendiciones
│       └── venta/           # Ventas y líneas
├── frontend/                # React + Vite
│   └── src/
│       ├── api/             # Cliente HTTP
│       ├── components/      # UI (Layout, modales, toast, etc.)
│       ├── context/         # NotificationContext
│       ├── hooks/           # useApi, useInfiniteScroll
│       ├── pages/           # Una página por módulo
│       ├── styles/          # Design system (variables CSS)
│       └── utils/           # csv, fechas
├── requerimientos.md        # Especificación funcional y de diseño
└── README.md
```

## Requisitos previos

- JDK 25
- Maven 3.9+
- Node.js 20+ y npm 10+

## Desarrollo

### Backend

```bash
cd backend
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8080` y sirve también el frontend compilado.

### Frontend (con hot reload)

```bash
cd frontend
npm install
npm run dev
```

Corre en `http://localhost:5173`. El proxy de Vite reenvía `/api` al backend (ver `vite.config.js`). Al hacer build, el frontend se copia a `backend/src/main/resources/static`.

### Seeder de datos de ejemplo

Al arrancar con el perfil `default` (el predeterminado), `DataSeeder` puebla la base de datos con datos de ejemplo, incluidos lotes vencidos para probar las alertas del dashboard.

## Build de producción

El frontend se sirve desde Spring Boot como un único proceso:

```bash
cd frontend && npm run build     # copia el bundle a backend/src/main/resources/static
cd ../backend && mvn package     # genera backend/target/luadministra-backend-0.0.1-SNAPSHOT.jar
```

### Ejecutar

```bash
java -jar backend/target/luadministra-backend-0.0.1-SNAPSHOT.jar
```

### Sin datos de ejemplo

Para arrancar sin ejecutar el seeder (base existente):

```bash
java -jar backend/target/luadministra-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Configuración

`backend/src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `server.port` | `8080` | Puerto HTTP |
| `spring.datasource.url` | `jdbc:sqlite:${app.basedir:backend}/data/luadministra.db` | Archivo SQLite |
| `spring.jpa.hibernate.ddl-auto` | `update` | Esquema automático |
| `springdoc.swagger-ui.path` | `/docs` | Interfaz Swagger UI |

### Servir desde `0.0.0.0`

Por defecto Spring Boot escucha en todas las interfaces, por lo que la app es accesible desde cualquier dispositivo de la red local por `http://IP-de-la-notebook:8080`.

## Acceso desde la red local (mDNS)

El backend registra automáticamente el servicio `LuAdministra._http._tcp.local.` mediante jmDNS al iniciarse. Esto permite acceder desde cualquier dispositivo de la misma red (y sin importar cuál de las redes Wi-Fi esté activa) por:

```
http://LuAdministra.local:8080
```

Para verificar la IP vigente de la notebook si mDNS no estuviera disponible, usar `ipconfig` (Windows) o `ip addr` (Linux/macOS).

### Nota sobre firewall

Al iniciar el servidor por primera vez, el sistema operativo (Windows/macOS) puede pedir permiso para conexiones entrantes en el puerto 8080. Hay que permitirlo para que el celular y otras notebooks puedan acceder.

## Documentación de la API

Con el servidor corriendo:

- Swagger UI: `http://localhost:8080/docs`
- JSON OpenAPI: `http://localhost:8080/docs/api-docs`

## Tests

```bash
cd backend
mvn test
```

72 tests de integración cubren todos los controladores y la lógica de negocio (CRUD, stock, rendiciones, validaciones).

## Guía de prueba manual

Ver la [lista de funciones a probar manualmente](#lista-de-funciones-a-probar-manualmente) que cubre los 12 módulos: Dashboard, Categorías, Materias Primas, Compras, Productos, Recetas, Lotes, Ventas, Consignatarios, Consignaciones, Rendiciones/Devoluciones y casos generales (mDNS, responsive, toasts, consistencia de stock).

## Despliegue en la notebook de la usuaria (Windows 11)

1. Instalar JDK 25.
2. Copiar el JAR a un directorio fijo (ej. `C:\LuAdministra\`).
3. Crear un acceso directo al JAR o un script `.bat`:

   ```bat
   @echo off
   java -jar "C:\LuAdministra\luadministra-backend-0.0.1-SNAPSHOT.jar"
   ```

4. Asegurarse de que la notebook **no suspenda** mientras el servidor esté en uso (Panel de control → Opciones de energía → Nunca suspender).
5. Permitir el acceso entrante al puerto 8080 en el firewall cuando el sistema lo solicite.
6. Acceder desde cualquier dispositivo por `http://LuAdministra.local:8080`.

## Backup

Todos los datos viven en un único archivo SQLite (`backend/data/luadministra.db`). Para respaldar, copiar ese archivo a un destino externo (pendrive, Google Drive, otro disco). Se recomienda hacerlo periódicamente.

## Licencia

Uso interno. Sin licencia pública definida.
