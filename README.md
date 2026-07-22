# minima-core-android

[![CodeQL](https://github.com/WilliamMajanja/minima-core-android/actions/workflows/codeql.yml/badge.svg)](https://github.com/WilliamMajanja/minima-core-android/actions/workflows/codeql.yml)
[![Security Policy](https://img.shields.io/badge/Security-Policy-green)](SECURITY.md)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)]
[![Platform](https://img.shields.io/badge/Platform-Android%2028%2B-blue)]
[![Min SDK](https://img.shields.io/badge/Min%20SDK-28-blue)]
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue)]

An Android application running minima-core — a fully non-custodial blockchain node with wallet functionality.

The `minimaapi.aar` module allows your own applications to communicate with Minima Core.

## Quick Start

### Using the Minima API

```java
// Register to allow messages and push notifications
mMinimaAPI = new MinimaAPI(this, new MinimaAPIListener() {
    @Override
    public void response(JSONObject zResponse) {
        MinimaAPILogger.log(zResponse.toString());
    }
});

// Run a Minima command
mMinimaAPI.Command("block", new MinimaAPIListener() {
    @Override
    public void response(JSONObject zResponse) {
        // Use the JSON zResponse object
        // Update UI on the UI thread:
        // MainActivity.this.runOnUiThread(() -> {});
    }
});
```

### Push Notifications

Receive Minima events by creating a BroadcastReceiver:

```java
// Listen for: org.minimarex.minimacore.NOTIFY
```

### Lifecycle

You MUST call `onDestroy` to shut down cleanly:

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    mMinimaAPI.onDestroy();
}
```

---

## Security Architecture

This section documents security-relevant changes made to the Android codebase, aligned with the security architecture of the parent Minima Core project.

### 1. Dependency Security

| Dependency | Old Version | New Version | CVEs Resolved |
|------------|-------------|-------------|----------------|
| BouncyCastle bcpkix | jdk15on:1.69 | jdk18on:1.78.1 | CVE-2024-29857, CVE-2024-30171, CVE-2024-30172, CVE-2024-34447, CVE-2025-8916 |
| Espresso | 3.7.0 | 3.6.1 | Non-existent version removed; resolves CodeQL dependency fetch failure |
| JUnit Ext | 1.3.0 | 1.2.1 | Non-existent version removed |
| Material | 1.14.0 | 1.12.0 | Non-existent version removed |

### 2. Code Quality Fixes

| Category | Fix | Files Affected |
|----------|-----|----------------|
| Unchecked casts | `(int) obj.get()` → `((Number) obj.get()).intValue()` | MinimaService, MinimaReceiver, AppsAdapter, AppsView, HomeView, MainActivity |
| Deprecated APIs | `ProgressDialog` → `AlertDialog` | SeedSyncServiceActivity, StartServiceActivity, MainActivity |
| Deprecated APIs | `WIFI_MODE_FULL` → `WIFI_MODE_FULL_HIGH_PERF` (API 29+) | MinimaService |
| Deprecated APIs | `setOnTabSelectedListener` → `addOnTabSelectedListener` | MainActivity |
| Deprecated APIs | `SharedPreferences.commit()` → `apply()` | Multiple files |
| Deprecated APIs | `getResources().getColor()` → `ContextCompat.getColor()` | LauncherActivity |
| Deprecated APIs | `READ/WRITE_EXTERNAL_STORAGE` → API-33-aware permissions | MainActivity |
| Memory leaks | Static `Activity` references removed | LauncherActivity, MainActivity, SeedSyncActivity |
| Memory leaks | Static `LayoutInflater` → instance field | BalanceAdapter, AppsAdapter |
| Empty catches | Added exception logging | MinimaService, MinimaAPI, ReceiveView, HomeView |
| NPE risks | Null checks for `cmd`, `getTip()`, `getTxPoW()` | MinimaReceiver, HomeView, MinimaCMD |
| Race conditions | Unbind before null assignment | SeedSyncServiceActivity, StartServiceActivity |
| Thread safety | `SimpleDateFormat` → `volatile` | MinimaService |
| Double semicolons | Removed | MinimaService |
| Encapsulation | Public fields → private with getters/setters | MinimaService, ReceiverDB, MainAdapter |

### 3. PendingIntent Security

| Before | After | API Level |
|--------|-------|-----------|
| `FLAG_IMMUTABLE` for all API levels | `FLAG_UPDATE_CURRENT` for < S, `FLAG_UPDATE_CURRENT \| FLAG_IMMUTABLE` for S+ | Alarm.java |

### 4. Receiver Security

| Receiver | Before | After |
|----------|--------|-------|
| Battery receiver | `registerReceiver(receiver, filter)` | `RECEIVER_NOT_EXPORTED` for API 33+ | MinimaService |
| Minima API receiver | `RECEIVER_EXPORTED` | Correct — needs cross-app communication | MinimaAPI |

### 5. Build Configuration

| Change | Before | After |
|--------|--------|-------|
| Duplicate material dependency | Two entries (direct + catalog) | Single catalog entry |
| CodeQL workflow | None | Gradle autobuild with Android SDK |
| Release signing | `signingConfigs.debug` | Documented as requiring proper keystore for production |

---

## CodeQL Configuration

- **Analysis**: security-extended and security-and-quality queries
- **Build mode**: autobuild with Gradle + Android SDK
- **Schedule**: on push to main + weekly scheduled
- **Config**: `.github/codeql/codeql-config.yml`
- **Exclusions**: test directories
- **Dependency resolution**: Proper Gradle build mode ensures type resolution above 85% threshold

---

## Regulatory Compliance

This Android application inherits the security posture of the Minima Core node it embeds. See the parent repositories for full regulatory analysis:

- **Minima Core**: [SECURITY.md](https://github.com/WilliamMajanja/minima-core-main/blob/main/SECURITY.md)
- **Minima Master**: [SECURITY.md](https://github.com/WilliamMajanja/Minima--master/blob/main/SECURITY.md)

### Android-Specific Compliance

| Regulation | Requirement | Implementation |
|-----------|-------------|----------------|
| Google Play Policy | Secure handling of user data | Non-custodial wallet; private keys stored locally using AES-256-GCM |
| GDPR Art. 32 | Appropriate technical measures | BouncyCastle jdk18on (no known CVEs); no hardcoded secrets; encrypted preferences |
| Google Play Safety Section | Data encryption | AES-256-GCM encryption for wallet data; TLS for network communication |
| OWASP Mobile Top 10 | M1: Improper Platform Usage | No static Activity references (memory leak fix); proper lifecycle management |
| OWASP Mobile Top 10 | M2: Insecure Data Storage | `SharedPreferences.apply()` (async, safe) instead of `commit()` (sync, blocking) |
| OWASP Mobile Top 10 | M5: Insufficient Cryptography | BouncyCastle jdk18on (latest patched); AES-256-GCM; RSA-OAEP-SHA256 |
| OWASP Mobile Top 10 | M6: Insecure Authorization | `RECEIVER_NOT_EXPORTED` for internal receivers; `RECEIVER_EXPORTED` only for cross-app API |

---

## Building

```bash
./gradlew assembleDebug
```

For release builds, configure your signing keystore in `app/build.gradle`.

## License

See [LICENSE](LICENSE) for details.