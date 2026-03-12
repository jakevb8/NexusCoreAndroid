# NexusCoreAndroid

Android client for [NexusCore](https://github.com/jakevb8/NexusCore) — a multi-tenant Resource Management SaaS. Built with Jetpack Compose and connects to either the Node.js or .NET backend via a user-selectable toggle.

## Features

- Google Sign-In via Firebase Authentication
- Backend selector: switch between the NexusCoreJS (Node) and NexusCoreDotNet (.NET) APIs
- Asset management: list, search, create, edit, delete, CSV import, sample CSV download
- Team management: invite members by email, copy-link fallback, remove members, change roles
- Reports: utilization rate and assets-by-status breakdown
- Settings: account info, backend picker, sign out
- Full RBAC support (`SUPERADMIN > ORG_MANAGER > ASSET_MANAGER > VIEWER`)

## Tech Stack

| Layer       | Library                                     |
| ----------- | ------------------------------------------- |
| UI          | Jetpack Compose + Material3                 |
| DI          | Hilt                                        |
| Navigation  | `navigation-compose`                        |
| Network     | Retrofit + Moshi + OkHttp                   |
| Auth        | Firebase Auth (Google sign-in) + FirebaseUI |
| Persistence | DataStore Preferences                       |
| State       | `StateFlow` + `collectAsState()`            |

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Firebase CLI (`npm install -g firebase-tools` + `firebase login`)

### Setup

1. **Clone the repo**

   ```bash
   git clone https://github.com/jakevb8/NexusCoreAndroid.git
   cd NexusCoreAndroid
   ```

2. **Restore `google-services.json`** (gitignored — never commit this file)

   ```bash
   firebase apps:sdkconfig ANDROID 1:797114794124:android:312c60b42b3e0d9a663ba9 \
     --project nexus-core-rms \
     --out app/google-services.json
   ```

3. **Open in Android Studio** and let Gradle sync.

4. **Run on a device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

## Building

```bash
# Debug APK
./gradlew assembleDebug

# Release AAB (requires signing env vars — see CI/CD below)
./gradlew bundleRelease

# Unit tests
./gradlew test

# Lint
./gradlew lint
```

## CI/CD & Google Play Publishing

Releases are published to Google Play Internal Testing automatically when a `v*` tag is pushed to `main`.

### GitHub Actions secrets required

| Secret                             | Description                                   |
| ---------------------------------- | --------------------------------------------- |
| `RELEASE_KEYSTORE_B64`             | Base64-encoded `.jks` release keystore        |
| `RELEASE_STORE_PASSWORD`           | Keystore password                             |
| `RELEASE_KEY_ALIAS`                | Key alias inside the keystore                 |
| `RELEASE_KEY_PASSWORD`             | Key password                                  |
| `GOOGLE_SERVICES_B64`              | Base64-encoded `google-services.json` for CI  |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Google Play service account JSON (plain text) |

### Release process

```bash
# Tag a release — CI will build, sign, and upload to Google Play Internal Testing
git tag v1.0.0
git push origin v1.0.0
```

## Package / Firebase Details

- **Package name:** `me.jakev.nexuscore`
- **Firebase project:** `nexus-core-rms`
- **Firebase App ID:** `1:797114794124:android:312c60b42b3e0d9a663ba9`

## Related Repos

| Repo                                                          | Description                             |
| ------------------------------------------------------------- | --------------------------------------- |
| [NexusCore](https://github.com/jakevb8/NexusCore)             | Next.js 15 frontend + NestJS REST API   |
| [NexusCoreDotNet](https://github.com/jakevb8/NexusCoreDotNet) | ASP.NET Core 8 Razor Pages backend      |
| [NexusCoreReact](https://github.com/jakevb8/NexusCoreReact)   | Expo React Native cross-platform client |
| [NexusCoreIOS](https://github.com/jakevb8/NexusCoreIOS)       | SwiftUI iOS native client               |
