# NexusCoreAndroid — Agent Instructions

## NEVER COMMIT SECRETS — CRITICAL

**This has caused incidents. Read carefully before every commit.**

Files that must NEVER be committed to git:

- `google-services.json` — contains a Firebase API key; it is gitignored; do NOT commit it
- `local.properties` — may contain SDK paths or keys
- Any `.env` file, service account JSON, or file with a real API key

`google-services.json` is required to build but must NOT be in git. Get it locally via:

```bash
firebase apps:sdkconfig ANDROID 1:797114794124:android:312c60b42b3e0d9a663ba9 --project nexus-core-rms --out app/google-services.json
```

Before every `git add` or commit:

1. Run `git diff --staged` and visually confirm no `google-services.json` or key values are included
2. If staged accidentally, run `git reset HEAD app/google-services.json` before committing
3. If already committed: (a) rotate/revoke the key in Google Cloud Console, (b) BFG-purge from history, (c) force-push

**History of incidents:**

- `google-services.json` commit `249569a` — Firebase API key committed, BFG-purged in security fix commit `615504e`

## Project Overview

NexusCoreAndroid is the Android (Jetpack Compose) client for the NexusCore multi-tenant Resource Management SaaS. It is a **frontend-only** app — it has no backend of its own. It connects to either of the two existing backends via a user-selectable toggle.

**Backend repos:**

- `NexusCoreJS` at `/Users/jake/projects/NexusCore` (GitHub: `jakevb8/NexusCore`) — NestJS REST API at `https://nexus-coreapi-production.up.railway.app/api/v1/`
- `NexusCoreDotNet` at `/Users/jake/projects/NexusCoreDotNet` (GitHub: `jakevb8/NexusCoreDotNet`) — ASP.NET Core at `https://nexuscoredotnet-production.up.railway.app/api/v1/`

**Sister client apps:**

- `NexusCoreReact` at `/Users/jake/projects/NexusCoreReact` (GitHub: `jakevb8/NexusCoreReact`) — Expo React Native cross-platform client (iOS + Android)
- `NexusCoreIOS` at `/Users/jake/projects/NexusCoreIOS` (GitHub: `jakevb8/NexusCoreIOS`) — SwiftUI iOS native client

The user selects the backend on the **Login screen** (toggle between JS and .NET). The choice is persisted via DataStore Preferences and applied to the Retrofit base URL.

## Cross-Repo Feature Parity

NexusCoreAndroid mirrors the **frontend/UI feature set** of NexusCoreJS and NexusCoreDotNet. When a UI feature changes in either backend repo, the equivalent change MUST be made here. Backend-only changes (e.g. business logic, migrations) do NOT require changes here.

**Canonical UI features (all must be implemented):**

| Screen       | Features                                                                           |
| ------------ | ---------------------------------------------------------------------------------- |
| Login        | Google sign-in (Firebase UI), backend selector toggle (JS vs .NET), persist choice |
| Onboarding   | Create org form (name + org name), POST /auth/register                             |
| Pending      | Show pending approval message, sign out                                            |
| Dashboard    | Navigation cards to Assets, Team, Reports, Settings                                |
| Assets       | List with search + pagination, create/edit/delete (manager only), CSV import       |
| Asset Detail | Create/edit form: name, SKU, description, status, assignedTo                       |
| Team         | List members, invite by email, remove member, change role (manager only)           |
| Reports      | Total assets, utilization rate, assets-by-status breakdown with bar chart          |
| Settings     | Account info, backend picker (JS vs .NET), sign out                                |

## Project Structure

```
NexusCoreAndroid/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json         — GITIGNORED; download locally via firebase CLI (see NEVER COMMIT SECRETS above)
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/me/jakev/nexuscore/
│   │   │   ├── NexusCoreApp.kt          — @HiltAndroidApp
│   │   │   ├── MainActivity.kt          — @AndroidEntryPoint, setContent
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── NexusApi.kt      — Retrofit interface
│   │   │   │   │   └── BackendPreference.kt — DataStore backend selector
│   │   │   │   └── model/Models.kt      — DTOs / enums
│   │   │   ├── di/AppModule.kt          — Hilt providers
│   │   │   └── ui/
│   │   │       ├── NavHost.kt           — NavController + Screen sealed class
│   │   │       ├── theme/Theme.kt
│   │   │       ├── components/AppScaffold.kt
│   │   │       ├── login/               — LoginScreen, LoginViewModel, Onboarding*, PendingApproval*
│   │   │       ├── dashboard/           — DashboardScreen
│   │   │       ├── assets/              — AssetsScreen, AssetsViewModel, AssetDetailScreen, AssetDetailViewModel
│   │   │       ├── team/                — TeamScreen, TeamViewModel
│   │   │       ├── reports/             — ReportsScreen, ReportsViewModel
│   │   │       └── settings/            — SettingsScreen, SettingsViewModel
│   │   └── res/
│   │       └── values/{strings.xml, themes.xml}
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── AGENTS.md
```

## Key Commands

```bash
# Build debug APK (requires ANDROID_HOME or local.properties pointing at SDK)
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device/emulator
./gradlew installDebug

# Lint
./gradlew lint
```

## Architecture

- **UI**: Jetpack Compose + Material3
- **DI**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **Navigation**: `androidx.navigation:navigation-compose`
- **Network**: Retrofit + Moshi + OkHttp (Bearer token interceptor auto-attaches Firebase ID token)
- **Auth**: Firebase Auth (Google sign-in only via FirebaseUI)
- **Persistence**: DataStore Preferences (selected backend)
- **State**: `StateFlow` + `collectAsState()` in every screen

## Firebase Setup

- **Package name**: `me.jakev.nexuscore` (follows `me.jakev.<appname>` convention)
- **Firebase project**: `nexus-core-rms` (same as NexusCoreJS web app — shares Google Sign-in config)
- **Firebase App ID**: `1:797114794124:android:312c60b42b3e0d9a663ba9`
- `google-services.json` is **gitignored** — download it locally, never commit it
- Google Sign-in is already enabled in the `nexus-core-rms` project (used by the web app)
- To download `google-services.json` locally: `firebase apps:sdkconfig ANDROID 1:797114794124:android:312c60b42b3e0d9a663ba9 --project nexus-core-rms --out app/google-services.json`

## Backend Selector

The backend is selected on the **Login screen** as `FilterChip` toggles. The choice is:

1. Persisted in DataStore (`selected_backend` key) via `BackendPreference`
2. Read at app startup in `AppModule.provideRetrofit()` via `runBlocking { backendPreference.get() }`
3. Also exposed in Settings screen as radio buttons for post-login changes

**Note**: Changing backend after login requires restarting the app (or a future enhancement to recreate the Retrofit instance). For now, changes take effect on next cold start.

## Important Notes

- `google-services.json` is required to build. Without it, `apply plugin: 'com.google.gms.google-services'` will fail. Download it via the Firebase CLI command in the NEVER COMMIT SECRETS section above — do not commit it.
- The `BackendChoice.DOTNET` base URL must match the actual Railway deployment URL for NexusCoreDotNet.
- The Retrofit singleton is created once at app startup. If the user changes backend in Settings, they must restart the app for it to take effect (known limitation — document in UI).
- All API calls require a valid Firebase ID token. The OkHttp interceptor in `AppModule` fetches it synchronously via `runBlocking` — acceptable for a mobile app but should be made async if token refresh latency becomes an issue.
- After completing any task that modifies files, always commit and push to the current branch without asking for confirmation.
