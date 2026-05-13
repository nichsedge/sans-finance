# Kotlin Multiplatform (KMP) Migration Plan for Sans Finance

This document outlines the transition of the Sans Finance Android application to a multiplatform project, enabling support for Web (WasmJs) and future platforms (iOS, Desktop).

## Completed Milestones

- [x] **Project Restructuring**: Configured the root project and `:shared` module for KMP.
- [x] **Web Support**: Enabled `wasmJs` target and created the necessary entry points (`main.kt`, `index.html`).
- [x] **NPM Compatibility**: Renamed the root project to `sans-finance` to satisfy NPM naming requirements for the Web build.
- [x] **Shared UI Infrastructure**: Moved core UI components (`CategoryIcon`, `GlassCard`, `Sparkline`, etc.) to `:shared/commonMain`.
- [x] **Server Integration**: Updated the Ktor `:server` to serve the compiled Web app as static files.
- [x] **Cloud-Ready**: Updated `server/Dockerfile` to build both the frontend and backend for a single-container deployment to Cloud Run.

## Project Structure

```text
.
├── app/                  # Android-specific application module
├── shared/               # KMP module (Common logic + Compose UI)
│   ├── src/commonMain    # Shared code (Models, ViewModels, UI)
│   ├── src/wasmJsMain    # Web-specific entry point
│   └── src/androidMain   # Android-specific implementations
└── server/               # Ktor backend (Serves Web app + API)
```

## Build Configuration Notes

### AGP 9.2.1 Compatibility
The current project uses a very new version of the Android Gradle Plugin (9.2.1). This requires using the `com.android.kotlin.multiplatform.library` plugin instead of the traditional `com.android.library` for the shared module.

> [!IMPORTANT]
> If you encounter `onVariant` errors, it usually indicates a version mismatch between AGP and the Kotlin/Compose plugins. We have opted for a "Web-first" configuration in `shared/build.gradle.kts` to ensure the Web build can progress independently of Android library complexities.

### Dependency Management
- **Compose Multiplatform**: Version `1.8.0-alpha03`
- **Kotlin**: Version `2.1.0`
- **Koin**: Used for DI across all platforms (replacing Hilt in `:shared`).
- **Lifecycle KMP**: `androidx-lifecycle-viewmodel` for shared ViewModels.

## Next Steps for UI Migration

### 1. Dependency Injection (Koin)
Move logic from Hilt to Koin in the `:shared` module.
- Define a `commonModule` in `shared/src/commonMain/kotlin/com/sans/finance/di/`.
- Initialize Koin in `main.kt` (Web) and `App.kt` (Android).

### 2. ViewModel Refactoring
Migrate `DashboardViewModel` and others to `commonMain`.
- Remove `@HiltViewModel` and `@Inject`.
- Replace `LocaleManager` with a multiplatform alternative (e.g., using `multiplatform-settings`).

### 3. Data Layer (Room KMP)
Room 2.7.0+ supports KMP, but `wasmJs` support is still evolving.
- For the Web target, consider implementing a `KtorExpenseRepository` that talks to the `:server` module, or use a mock repository for initial UI development.

### 4. Resource Management
Convert Android resources (strings, icons) to Compose Multiplatform Resources.
- Use `Res.string.name` instead of `R.string.name`.
- Place shared resources in `shared/src/commonMain/composeResources/`.

## Deployment to Cloud Run

To deploy the full stack (Web App + Ktor API), use the updated `server/Dockerfile`:

```bash
# Build and push to GCR
gcloud builds submit --config cloudbuild.yaml .
```

The server is configured to serve the Web app at the root `/` and API endpoints under `/api/*`.
