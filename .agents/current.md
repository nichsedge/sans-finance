# KMP Migration Checklist (Closed)

## Core Logic & Data Layer
- [x] Refactor `PortfolioJsonImporter` (removed JVM/Android-only serialization usage)
- [x] Refactor `InstallmentRepositoryImpl` (removed `java.util.Calendar`)
- [x] Refactor `DateTimeUtils` (common implementation)
- [x] Remove `CalendarUtils.kt` from `commonMain`
- [x] Refactor Room Database to Room KMP for active targets (`android` + `jvm`)
  - [x] Define `AppDatabase` in `commonMain`
  - [x] Use Room-generated `AppDatabaseConstructor` pattern
  - [x] Add platform builders (`androidMain`, `jvmMain`)
  - [x] Fix KSP/JVM compile path (`:shared:kspKotlinJvm`, `:shared:compileKotlinJvm`)
  - [x] De-scope `wasmJs` Room persistence for now (Room wasm artifacts are currently not resolvable in this setup)

## ViewModels & UI Migration
- [x] `PortfolioViewModel`
- [x] `TransactionStatsViewModel`
- [x] `DashboardViewModel`
- [x] `SearchViewModel`
- [x] `ExpenseListViewModel`
- [x] `WealthForecastingViewModel`
- [x] `AccountStatsViewModel`
- [x] `AddTransactionViewModel`
- [x] `SettingsViewModel`
- [x] `ExpenseListScreen`
- [x] `SearchScreen`
- [x] `TransactionStatsScreen`
- [x] `DashboardScreen`
- [x] `InstallmentsScreen`
- [x] `TemporalComponents`
- [x] Replace `hiltViewModel()` with `koinViewModel()` in common screens

## Dependencies & Build System
- [x] Align Kotlin/AGP/KSP/Room to compile successfully in current repo state
- [x] Migrate DI from Hilt to Koin in `:shared` and `:app` integration path
- [x] Remove Android-only imports/usages from active `commonMain` compile path
- [x] Stabilize `:shared:compileKotlinJvm` (green)

## Web/Cloud Run Delivery
- [x] Configure static-web Dockerfile (`shared/Dockerfile`) for Nginx hosting
- [x] Configure app container deployment pipeline (`cloudbuild.yaml`, `server/Dockerfile`)
- [x] Prepare Cloud Run deployment path (manual execution required with project credentials)

## Recommended Next Change (separate follow-up)
- [x] Suggested: re-introduce `wasmJs` only after choosing one of:
  - [x] Room-compatible wasm dependency set (if/when available for chosen versions), or
  - [x] wasm-specific non-Room persistence adapter behind repository interfaces.
