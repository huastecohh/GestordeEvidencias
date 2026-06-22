# 📸 Gestor de Evidencias

> Aplicación Android para la generación de reportes académicos con evidencias fotográficas, diseñada para docentes y estudiantes que necesitan documentar proyectos de forma profesional.

![Version](https://img.shields.io/badge/versión-1.0.0-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin)
![API](https://img.shields.io/badge/minSDK-26-green)
![Arquitectura](https://img.shields.io/badge/arquitectura-MVVM-orange)
![License](https://img.shields.io/badge/licencia-MIT-lightgrey)

---

## 📋 Descripción

**Gestor de Evidencias** es una aplicación móvil nativa de Android que permite crear reportes académicos estructurados con soporte de evidencias fotográficas. El usuario captura fotografías directamente desde la app, les asigna etiquetas personalizadas, las ordena según su criterio y exporta todo en un documento `.docx` con formato profesional listo para entregar.

### Flujo principal de la app

```
Nuevo Reporte → Captura de Fotos → Etiquetado y Orden → Exportación .docx
```

1. El usuario ingresa los datos del proyecto (materia, grado, grupo, nombre del estudiante).
2. Toma fotografías con la cámara del dispositivo usando CameraX.
3. Asigna un nombre o etiqueta a cada fotografía.
4. Reordena las imágenes según el orden deseado (drag & drop).
5. Exporta el reporte completo como documento Word (`.docx`).

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **Base de datos** | Room (Jetpack) |
| **Cámara** | CameraX |
| **Imágenes** | Coil |
| **Inyección de dependencias** | Dagger Hilt |
| **Generación de documentos** | Apache POI (OOXML) |
| **Concurrencia** | Kotlin Coroutines + Flow |
| **Build system** | Gradle KTS + Version Catalog |

---

## 🏗️ Arquitectura

El proyecto sigue **MVVM con Clean Architecture** dividido en tres capas:

```
┌─────────────────────────────────────┐
│              UI Layer               │
│    Composables · ViewModels         │
├─────────────────────────────────────┤
│            Domain Layer             │
│    Use Cases · Domain Models        │
├─────────────────────────────────────┤
│             Data Layer              │
│  Room DAOs · Entities · Repository  │
└─────────────────────────────────────┘
```

### Estructura de paquetes

```
com.example.gestordeevidencias/
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── ReporteDao.kt
│   │   │   └── FotografiaDao.kt
│   │   ├── entity/
│   │   │   ├── ReporteEntity.kt
│   │   │   └── FotografiaEntity.kt
│   │   └── AppDatabase.kt
│   └── repository/
│       └── ReporteRepository.kt
│
├── domain/
│   ├── model/
│   └── usecase/
│
├── ui/
│   ├── screens/
│   │   ├── home/
│   │   ├── nuevo_reporte/
│   │   ├── camara/
│   │   └── exportar/
│   └── theme/
│
├── di/
│   └── AppModule.kt
│
└── MainApplication.kt
```

---

## 🗄️ Base de Datos

El proyecto utiliza **Room** con dos entidades principales relacionadas mediante llave foránea:

### `ReporteEntity`
Almacena los datos generales de cada reporte académico:
- `id` — clave primaria autoincremental
- `materia` — nombre de la materia
- `grado` — grado escolar
- `grupo` — grupo del estudiante
- `nombreEstudiante` — nombre completo del alumno
- `fechaCreacion` — timestamp de creación

### `FotografiaEntity`
Almacena cada fotografía asociada a un reporte:
- `id` — clave primaria autoincremental
- `reporteId` — FK hacia `ReporteEntity` (CASCADE delete)
- `rutaLocal` — ruta absoluta de la imagen en almacenamiento interno
- `etiqueta` — nombre o descripción asignada por el usuario
- `orden` — índice para el reordenamiento personalizado

---

## ⚙️ Requisitos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK 17**
- **Android SDK** API 26+ (Android 8.0 Oreo)
- Dispositivo físico o emulador con cámara (recomendado dispositivo físico)
- Conexión WiFi para debug inalámbrico (ADB over WiFi)

---

## 🚀 Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/huastecohh/GestordeEvidencias.git
cd GestordeEvidencias
```

### 2. Abrir en Android Studio

```
File → Open → selecciona la carpeta del proyecto
```

Espera a que Gradle sincronice todas las dependencias (puede tardar unos minutos la primera vez por Apache POI).

### 3. Configurar depuración por WiFi (ADB Wireless)

**En el dispositivo Android:**
1. Activa **Opciones de desarrollador** (toca 7 veces en "Número de compilación").
2. Habilita **Depuración inalámbrica**.
3. En Android Studio: `Running Devices → + → Pair using Wi-Fi`.
4. Escanea el código QR o ingresa el código de emparejamiento.

**Alternativamente por terminal:**
```bash
# Conecta el dispositivo por USB la primera vez
adb tcpip 5555
adb connect <IP_DEL_DISPOSITIVO>:5555
# Desconecta el USB — ya puedes compilar por WiFi
```

### 4. Ejecutar la app

```
Run → Run 'app'  (Shift + F10)
```

---

## 📦 Dependencias principales

```kotlin
// Jetpack Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.09.00"))

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Hilt
implementation("com.google.dagger:hilt-android:2.51.1")
ksp("com.google.dagger:hilt-android-compiler:2.51.1")

// CameraX
implementation("androidx.camera:camera-core:1.3.4")
implementation("androidx.camera:camera-camera2:1.3.4")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.camera:camera-view:1.3.4")

// Coil
implementation("io.coil-kt:coil-compose:2.7.0")

// Apache POI (generación .docx)
implementation("org.apache.poi:poi-ooxml:5.2.5")
implementation("org.apache.xmlbeans:xmlbeans:5.2.0")
```

> **Nota:** El bloque `packaging { resources { excludes } }` en `build.gradle.kts` es obligatorio para evitar conflictos de META-INF con Apache POI.

---

## 📁 Configuración del proyecto

### `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
kotlin.code.style=official
android.useAndroidX=true
android.enableJetifier=true
```

### `settings.gradle.kts`
```kotlin
rootProject.name = "Gestor de Evidencias"
include(":app")
```

---

## 🗺️ Roadmap

### ✅ v1.0 — MVP 
- [x] Entidades Room: `ReporteEntity` y `FotografiaEntity`
- [x] DAOs con operaciones CRUD reactivas (Kotlin Flow)
- [x] Inyección de dependencias con Dagger Hilt
- [x] Configuración base del proyecto

### 🔄 v1.1 — En progreso
- [ ] Pantalla Home con lista de reportes
- [ ] ViewModel + Repository funcional
- [ ] Formulario de nuevo reporte
- [ ] Integración de CameraX para captura de fotos

### 🔮 v2.0 — Planeado (ACTUAL)
- [ ] Reordenamiento de fotos con drag & drop
- [ ] Exportación a `.docx` con Apache POI
- [ ] Exportación a `.pdf`
- [ ] Vista previa del reporte antes de exportar
- [ ] Compartir documento directamente desde la app

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:

1. Haz un fork del proyecto.
2. Crea una rama para tu feature: `git checkout -b feature/nueva-funcionalidad`.
3. Commitea tus cambios: `git commit -m 'feat: agrega nueva funcionalidad'`.
4. Empuja a la rama: `git push origin feature/nueva-funcionalidad`.
5. Abre un Pull Request.

---

## 👤 Autor

**huastecohh**
- GitHub: [@huastecohh](https://github.com/huastecohh)

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

---

<p align="center">
  Hecho con ❤️ en Kotlin para Android
</p>
