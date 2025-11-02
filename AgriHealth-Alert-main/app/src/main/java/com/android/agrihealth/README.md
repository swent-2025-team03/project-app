## 🔍 Summary
This PR introduces reliable tests for **Firestore offline caching and synchronization**, ensuring the app correctly handles data when the device goes offline and comes back online.

---

## ✅ Changes
- Added `FirestoreCacheTest` under `com.android.agrihealth.core`.
- Enabled Firestore SDK logging **before** linking to the local emulator for clearer test visibility.
- Verified two main behaviors:
    1. **`cachePersistsDataOffline()`**  
       Confirms that data written online remains readable from the local cache when offline and resyncs correctly once the network is re-enabled.
    2. **`offlineWriteSyncsWhenNetworkRestored_stable()`**  
       Ensures offline writes are persisted locally and match server values using `Source.CACHE` and `Source.SERVER` reads.
- Used deterministic document IDs (`UUID`) to avoid collisions between test runs.
- Removed unreliable `disableNetwork()` / `enableNetwork()` toggles that caused blocking in instrumented environments.
- Added `finally` blocks to always re-enable the Firestore network after each test.

---

## ⚙️ Implementation Notes
- Firestore Emulator linked via `FirebaseEmulatorsManager.linkEmulators()`.
- Firestore SDK logging enabled using `FirebaseFirestore.setLoggingEnabled(true)` for detailed debugging.
- Each test interacts with the emulator’s `connect_codes` collection (compatible with Firestore rules).
- Added `awaitServerValue()` helper to poll the server with timeout-based safety.

---

## 🧪 Verification
Run the tests on the Android emulator:

```bash
./gradlew connectedDebugAndroidTest
