# LotoControl

LotoControl es una aplicación Android diseñada para gestionar de forma sencilla las entregas y pagos de lotería semanal a distintos clientes. La aplicación permite un control eficiente de décimos entregados, devoluciones, pagos y saldos pendientes.

## Características Principales

- **Pantalla de Inicio**
  - Visualización de la fecha del sorteo actual
  - Precio del décimo
  - Lista de clientes con acceso directo a sus liquidaciones
  - Estado de pagos con código de colores

- **Gestión de Clientes**
  - Control de décimos entregados y devueltos
  - Registro de pagos
  - Cálculo automático de saldos
  - Seguimiento de deudas anteriores

- **Resumen General**
  - Vista general de todos los saldos
  - Identificación visual de clientes con deuda (rojo)
  - Identificación de clientes al día (azul)

- **Importación de Datos**
  - Soporte para archivos Excel
  - Formato de importación estandarizado

## Requisitos Técnicos

- Android Studio Arctic Fox o superior
- Android SDK 24 o superior
- Kotlin 1.9.0 o superior
- Gradle 8.1.2 o superior

## Configuración del Proyecto

1. Clone el repositorio:
   ```bash
   git clone [url-del-repositorio]
   ```

2. Abra el proyecto en Android Studio

3. Sincronice el proyecto con Gradle

4. Ejecute la aplicación en un emulador o dispositivo físico

## Formato de Archivo Excel para Importación

El archivo Excel debe tener las siguientes columnas:

| Cliente | Fecha Sorteo | Precio Décimo | Décimos Entregados | Deuda Anterior |
|---------|--------------|---------------|-------------------|----------------|
| Nombre  | DD/MM/YYYY   | 0.00         | 0                 | 0.00          |

## Tecnologías Utilizadas

- **UI/UX**
  - Jetpack Compose
  - Material Design 3
  - Temas y estilos personalizados

- **Arquitectura**
  - MVVM (Model-View-ViewModel)
  - Clean Architecture
  - Jetpack Navigation

- **Persistencia de Datos**
  - Room Database
  - Apache POI (para Excel)

- **Inyección de Dependencias**
  - Hilt

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - vea el archivo LICENSE para más detalles.
