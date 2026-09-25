# TODO

Plantilla para registrar pendientes: cosas por implementar, revisar o decisiones abiertas.

## Cómo usar

- Copiá los bloques que necesites y completalos.
- Marcá `[ ]` → `[x]` cuando esté hecho.
- Usá una sección por área (Backend, Frontend, Tests, Diseño/Decisiones, Deuda técnica, Documentación).

---

## Backend

- [ ] **`_Titulo_`**
  - Área: `_paquete/servicio/endpoint_`
  - Descripción: _qué falta o qué revisar_
  - Prioridad: `alta | media | baja`
  - Estado: `pendiente | en progreso | bloqueado`
  - Cómo verificar: _comando / test / paso manual_

## Frontend

- [ ] **`_Titulo_`**
  - Página/componente: `_ruta_`
  - Descripción: _qué falta o qué revisar_
  - Prioridad: `alta | media | baja`
  - Estado: `pendiente | en progreso | bloqueado`
  - Cómo verificar: _build / paso manual_

- [ ] **`Facilitar la entrada de registros inmutables`**
  - Página/componente: ``
  - Descripción: _Mejorar modales para la modificación de registros y operaciones adjacentes para evitar o reducir las probabilidades de borrado/modificación por entrada errónea_
  - Prioridad: `alta`
  - Estado: `pendiente`
  - Cómo verificar: _test manual_

## Tests

- [ ] **`_Titulo_`**
  - Alcance: `_LoteServiceTest | VentaControllerTest | ..._`
  - Descripción: _test a agregar o corregir_
  - Prioridad: `alta | media | baja`
  - Estado: `pendiente | en progreso | bloqueado`

## Diseño / Decisiones abiertas

- [ ] **`_Titulo_`**
  - Contexto: _qué se intenta resolver_
  - Opciones consideradas: _lista breve_
  - Decisión tomada: _opción X | pendiente_
  - Impacto: _archivos/entidades afectadas_

## Deuda técnica / Refactor

- [ ] **`_Titulo_`**
  - Dónde: `_archivo:linea o ruta_`
  - Problema: _qué está mal o incompleto_
  - Prioridad: `alta | media | baja`
  - Estado: `pendiente | en progreso | bloqueado`

## Documentación

- [ ] **`_Titulo_`**
  - Qué falta documentar: _proceso, regla de negocio, comando, etc._
  - Dónde: `_README | comentario | wiki_`
  - Prioridad: `alta | media | baja`

---

## Plantilla compacta (tarea rápida)

- [ ] `[prioridad] _descripción corta_ `_`(área)`_

---

## Pendientes actuales (ejemplos)

- [ ] `[alta] (diseño) Definir mecanismo para corregir rendiciones erróneas` _(no existe forma de revertir/editar una rendición; registrado como gap fuera de alcance)_
- [ ] `[media] (backend) Snapshot de MP consumidas en Lote` _(eliminar un lote no devuelve MP, pero el revert futuro dependería de la receta vigente)_