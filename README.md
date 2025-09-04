
# 📸 Smart Photo Finder

Find your best photo in minutes — without scrolling endlessly through your gallery.

With **Smart Photo Finder**, you can select a photo, detect a face, and instantly discover similar photos in your library.
So the next time someone asks for your best or most relevant photo, you’ll have it ready in seconds.

---
## 🚀 Features

* **Media Indexing** – Scans your local camera images for quick retrieval.
* **Face Detection** – Powered by **Google ML Kit**.
* **Face Embedding** – Extracts embeddings using **TensorFlow Lite (MobileFaceNet)** for similarity search.
* **Thumbnail Generation** – Creates optimized **200×200 JPEG** thumbnails for smooth browsing.
* **Face-Based Search** – Pick one photo and instantly find similar ones.

> ✅ **Currently supports local camera images.**
> The app is built to be **maintainable and extensible**, so new sources like **WhatsApp media** or **cloud storage (Dropbox, Google Drive, etc.)** can be integrated easily.


## 🏗️ Architecture & Components

* **Architecture**: MVVM (lifecycle awareness) + MVI (state management) + Clean Architecture (layered, testable, extensible)
* **UI**: Jetpack Compose
* **Navigation**: Jetpack Navigation
* **Image Loading**: Coil
* **Face Detection**: Google ML Kit
* **Embeddings**: TensorFlow Lite
* **Persistence**: Room Database
* **Background Processing**: WorkManager
* **Paging**: Paging 3 (for lazy loading + memory efficiency)
* **Dependency Injection**: Koin

---
## ⚙️ Performance & Optimization

* **Batch Processing** – Camera images are processed in batches for face detection & thumbnails.
* **Bitmap Pooling** – Reuses buffers to prevent out-of-memory (OOM) crashes.
* **Lazy Loading** – Combines Room + Paging 3 to load only what’s visible.
* **Optimized Full Images** – Original images are downscaled to 1280px (longest edge). Face detection is performed on this optimized version, striking the right balance between accuracy and performance.
* **Offline Support** – Face embeddings & tags stored locally with Room DB.
* **Modern UI** – Jetpack Compose provides performance and maintainability.
* **System-Aware Background Work** – WorkManager schedules jobs without draining battery.


## 📦 Roadmap & Extensibility

Extensibility is a **core design goal** of this project.
The clean, modular architecture allows new data sources, ML models, or features to be added without disrupting existing functionality.
---
## 🛠️ Tech Stack

* **Language**: Kotlin
* **UI**: Jetpack Compose
* **Architecture**: MVVM + MVI + Clean Architecture
* **Image Loading**: Coil
* **Database**: Room
* **Async**: Coroutines + Flow
* **Dependency Injection**: Koin
* **Background Work**: WorkManager
* **Face Detection**: Google ML Kit
* **Embeddings**: TensorFlow Lite



---

