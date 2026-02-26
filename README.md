<p align="center">
  <img src="doc/assets/logo3.png" alt="Vendoo Logo" width="200"/>
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

### Generación de Docs con Dokka (Android)
Para generar la documentación técnica de la aplicación Android en formatos HTML o Javadoc, utiliza los siguientes comandos desde la carpeta `android_app`:

```bash
# Generar documentación en HTML
./gradlew :app:dokkaHtml

# Generar documentación en formato Javadoc
./gradlew :app:dokkaJavadoc
```
Los archivos generados se encontrarán en `android_app/app/build/dokka/`.

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
