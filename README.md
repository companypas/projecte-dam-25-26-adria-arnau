<p align="center">
  <img src="assets/logo3.png" alt="Vendoo Logo" width="200"/>
</p>

# Vendoo - Marketplace App 🛒 📚

## 📝 Descripción
**Vendoo** es una plataforma de marketplace moderna desarrollada como Proyecto Intermodular para el ciclo de **Desarrollo de Aplicaciones Multiplataforma (DAM)**. La aplicación permite a los usuarios comprar y vender productos de segunda mano, gestionar sus perfiles y comunicarse con otros vendedores.

El sistema se compone de una aplicación nativa Android y un potente backend basado en **Odoo**, todo orquestado mediante contenedores Docker para facilitar su despliegue.

---

## � Tecnologías Utilizadas

### Frontend (Android App)
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Inyección de Dependencias:** Hilt
- **Persistencia Local:** SQLDelight
- **Networking:** Retrofit + OkHttp
- **Documentación:** Dokka (HTML y Javadoc)

### Backend (Odoo)
- **Plataforma:** Odoo ERP
- **Base de Datos:** PostgreSQL
- **Módulos:** Custom addons desarrollados específicamente para el marketplace.
- **Despliegue:** Docker & Docker Compose

---

## 📂 Estructura del Proyecto

- `android_app/`: Código fuente de la aplicación nativa Android.
- `custom_addons/`: Módulos personalizados de Odoo para la lógica de negocio del marketplace.
- `docker-compose.yml`: Configuración para levantar el entorno de Odoo y la base de datos.
- `diagramas/`: Documentación visual y esquemas de la arquitectura.
- `odoo/`: Ficheros de configuración del servidor backend.

---

## 📖 Documentación

### 📱 Documentación Android — Dokka
Para generar la documentación técnica de la aplicación Android en formatos HTML o Javadoc, utiliza los siguientes comandos desde la carpeta `android_app`:

```bash
# Generar documentación en HTML
./gradlew :app:dokkaHtml

# Generar documentación en formato Javadoc
./gradlew :app:dokkaJavadoc
```
Los archivos generados se encontrarán en `android_app/app/build/dokka/`.

---

### 🐍 Documentación Backend (Odoo + API) — Sphinx

La documentación del backend (módulos Odoo y API REST) se genera con **Sphinx** y se encuentra en `docs/modulos_odoo_y_api/`.

#### 1. Instalar las dependencias

Desde la raíz del proyecto, ejecuta:

```bash
pip install -r docs/modulos_odoo_y_api/requirements-docs.txt
```

Esto instalará:
- `sphinx` – motor principal de documentación
- `sphinx-rtd-theme` – tema visual Read the Docs
- `myst-parser` – soporte para ficheros Markdown (`.md`)
- `sphinx-autodoc-typehints` – extracción de tipos desde docstrings
- `sphinx-autobuild` – recarga en vivo *(opcional)*

#### 2. Generar la documentación HTML

Accede a la carpeta de docs y ejecuta `make`:

```bash
cd docs/modulos_odoo_y_api

# En Linux / macOS / Git Bash
make html

# En Windows (CMD / PowerShell)
.\make.bat html
```

La documentación generada estará disponible en:
```
docs/modulos_odoo_y_api/_build/html/index.html
```
Abre ese fichero en tu navegador para consultarla.

#### 3. Modo recarga en vivo *(opcional)*

Si quieres que la documentación se recargue automáticamente al editar los ficheros `.rst` o `.md`:

```bash
# Linux / macOS / Git Bash
make livehtml

# Windows
.\make.bat livehtml
```

Se abrirá un servidor local en `http://127.0.0.1:8000` con recarga automática.

#### 4. Limpiar la documentación generada

```bash
# Linux / macOS / Git Bash
make clean

# Windows
.\make.bat clean
```

---

## 🛠️ Instalación y Ejecución

### 1. Levantar el Backend
Asegúrate de tener Docker instalado y ejecuta:
```bash
docker-compose up -d
```

### 2. Ejecutar la App Android
1. Abre la carpeta `android_app` en Android Studio (Koala o superior).
2. Asegúrate de que el SDK de Java esté configurado en la versión 17 o superior.
3. Ejecuta la app en un emulador o dispositivo físico.

---

## 👥 Participantes
Este proyecto ha sido desarrollado por:
- **Company Pastor, Adrià**
- **Alemany Espert, Arnau**

### 🏫 Centro Educativo
**IES Eduardo Primo Marqués**

---

## 📧 Contacto
**Tutor del proyecto:** [montesinos_iva61@ieseduardoprimo.es](mailto:montesinos_iva61@ieseduardoprimo.es)

---

## 📄 Licencia
Este proyecto está desarrollado con fines educativos como parte del módulo intermodular del ciclo formativo de DAM.

<p align="center">
  <img src="https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=1200" alt="Programming" width="100%"/>
</p>
