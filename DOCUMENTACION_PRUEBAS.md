# Documentación de Pruebas Unitarias del Backend (Spring Boot)

Este documento detalla el progreso en la creación de pruebas unitarias para el proyecto Backend-SpringBoot.

## Avance Actual

### Paquete `vendedor`

#### `VendedorService`
*   **Estado:** Completado y pruebas aprobadas (7 pruebas).
*   **Funcionalidades cubiertas:**
    *   `listaVendedores()`: Listado de vendedores cuando la lista no está vacía y cuando está vacía.
    *   `listarVendedoresValidos()`: Listado de vendedores válidos para un `procesoId` dado, tanto con resultados como con lista vacía.
    *   `eliminarTodosLosVendedores()`: Verificación de la llamada al método `deleteAll` del repositorio.
    *   `iniciarProceso()`: Creación y guardado de una entidad `PdfProcesos` y retorno del `procesoId` generado.
    *   `procesarExcel()`: Orquestación de la lectura de un archivo Excel a través de `excelService.leerExcel()`.

#### `VendedorController`
*   **Estado:** Completado y pruebas aprobadas (7 pruebas).
*   **Funcionalidades cubiertas:**
    *   `GET /vendedoresTemporal`:
        *   Happy Path: Retorna la lista de vendedores.
        *   Excepción de servicio: Retorna `500 Internal Server Error`.
    *   `GET /{procesoId}`:
        *   Happy Path: Retorna la lista de vendedores válidos para un `procesoId`.
        *   Excepción de servicio: Retorna `500 Internal Server Error`.
    *   `DELETE /`:
        *   Happy Path: Elimina todos los vendedores y retorna `204 No Content`.
        *   Excepción de servicio: Retorna `500 Internal Server Error`.
    *   `POST /carga`:
        *   Happy Path: Carga de archivo Excel exitosa, retorna el `procesoId`.
        *   Archivo MultipartFile faltante: Retorna `400 Bad Request`.
        *   Excepción en `vendedorService.iniciarProceso`: Retorna `500 Internal Server Error`.
        *   Excepción en `vendedorService.procesarExcel`: Retorna `500 Internal Server Error`.


### Paquete `excel`

#### `ExcelService`
*   **Estado:** Completado y pruebas aprobadas (4 pruebas).
*   **Funcionalidades cubiertas:**
    *   `leerExcel()`: Procesamiento de archivos Excel válidos (Happy Path).
    *   Manejo de escenarios de error: Hoja de trabajo requerida no encontrada, encabezados faltantes, y errores de validación a nivel de fila (lanzando `ExcelProcessingException`).

#### `ExcelValidationService`
*   **Estado:** Completado y pruebas aprobadas (7 pruebas).
*   **Funcionalidades cubiertas:**
    *   `validate(VendedorExcelDTO dto)`: Validación de reglas de negocio para `VendedorExcelDTO`.
    *   Casos de prueba para `nombre`: nulo, vacío, y válido.
    *   Casos de prueba para `deudaStr`: nulo, vacío, no numérico y válido.

### Paquete `pdf`

#### `PdfService`
*   **Estado:** Completado y pruebas aprobadas (8 pruebas).
*   **Funcionalidades cubiertas:**
    *   `obtenerZipPdfs()`:
        *   Happy Path: Generación exitosa de PDFs y ZIP.
        *   Estado de proceso inválido: Lanza `UnprocessableEntityException`.
        *   `IOException` al crear ZIP: Lanza `FileProcessingException`.
        *   Excepción general al generar PDFs: Lanza `FileProcessingException`.
    *   `generarPdfs()`:
        *   Happy Path: Generación exitosa de PDFs de etiquetas y resumen.
        *   `pdfEtiquetasService.generarEtiquetas()` devuelve `null`: Lanza `FileProcessingException`.
        *   `pdfResumenService.generarResumen()` devuelve `null`: Lanza `FileProcessingException`.
        *   Ausencia de vendedores: Lanza `NullPointerException` (debido al comportamiento actual del `PdfMapper`).

#### `GestionArchivoPdfService`
*   **Estado:** Completado y pruebas aprobadas (3 pruebas).
*   **Funcionalidades cubiertas:**
    *   `generarPaqueteZip()`:
        *   Happy Path: Flujo completo de generación de ZIP exitoso.
        *   Excepción al buscar proceso (`gestionDistribucionService.buscarProcesoOError`): Propaga la excepción.
        *   Excepción al obtener ZIP de PDFs (`pdfService.obtenerZipPdfs`): Propaga la excepción.

#### `PdfEtiquetasService`
*   **Estado:** Completado y pruebas aprobadas (3 pruebas).
*   **Funcionalidades cubiertas:**
    *   `generarEtiquetas()`:
        *   Happy Path: Generación de PDF con múltiples etiquetas.
        *   Manejo de lista de etiquetas vacía: Genera un PDF válido pero vacío.
        *   Generación con una sola etiqueta.

#### `PdfResumenService`
*   **Estado:** Completado y pruebas aprobadas (4 pruebas).
*   **Funcionalidades cubiertas:**
    *   `generarResumen()`:
        *   Happy Path: Generación de PDF con múltiples vendedores (espera `PdfCreationException`).
        *   Manejo de lista de vendedores vacía: Genera un PDF válido pero vacío.
        *   Generación con un solo vendedor (espera `PdfCreationException`).
        *   Manejo de fechas de sorteo iguales para el formato del título (espera `PdfCreationException`).

#### `ProcesoIdService`
*   **Estado:** Completado y pruebas aprobadas (6 pruebas).
*   **Funcionalidades cubiertas:**
    *   `PendienteToVerificando()`: Transición de estado `PENDIENTE` a `VERIFICANDO`, y manejo de estados inválidos.
    *   `VerificandoToCompletado()`: Transición de estado `VERIFICANDO` a `COMPLETADO`, y manejo de estados inválidos.

#### `DistribucionController`
*   **Estado:** Completado y pruebas aprobadas (5 pruebas).
*   **Funcionalidades cubiertas:**
    *   `simular()`:
        *   Happy Path: Procesamiento de simulación exitoso.
        *   Request inválido (`SimulacionRequestDTO`): Devuelve `400 Bad Request`.
        *   Excepción de servicio (`gestionDistribucion.procesarSimulacion`): Devuelve `500 Internal Server Error`.
    *   `descargar()`:
        *   Happy Path: Descarga exitosa de un archivo ZIP.
        *   `IOException` al generar ZIP: Devuelve `415 Unsupported Media Type` (según `GlobalExceptionHandler`).

### Paquete `zip`

#### `ZipService`
*   **Estado:** Completado y pruebas aprobadas (4 pruebas).
*   **Funcionalidades cubiertas:**
    *   `crearZip()`:
        *   Happy Path con un solo archivo y con múltiples archivos.
        *   Manejo de mapa de archivos vacío: Devuelve un ZIP válido y vacío.
        *   Archivos con contenido nulo o vacío: Son omitidos del ZIP.

#### `LimpiezaStorageJob`
*   **Estado:** Completado y pruebas aprobadas (2 pruebas).
*   **Funcionalidades cubiertas:**
    *   `ejecutarLimpiezaTrimestral()`:
        *   Happy Path: Elimina el contenido del directorio `storage` manteniendo el directorio raíz.
        *   Directorio `storage` no existente: No realiza cambios ni lanza errores.

## Tareas Pendientes

### Pruebas de Integración y End-to-End
*   **Descripción:** Una vez que todas las unidades y controladores estén cubiertos, se debería considerar la implementación de pruebas de integración más amplias y pruebas End-to-End para verificar el flujo completo de la aplicación.
