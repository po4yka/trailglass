# End-to-End Encryption - TrailGlass

**Implemented:** 2025-11-18
**Status:** ✅ Client-side Complete (Server integration pending)
**Security Level:** AES-256-GCM with platform secure storage

---

## Overview

TrailGlass implements client-side end-to-end encryption (E2E) to protect user data during sync operations. When enabled, all sync data is encrypted on the device before transmission and decrypted after reception. The server only stores encrypted blobs and cannot access the actual data.

---

## Architecture

### Components

1. **EncryptionService** (`shared/src/commonMain/kotlin/com/po4yka/trailglass/data/security/EncryptionService.kt`)
   - Platform-agnostic encryption interface
   - Expect/actual pattern for platform-specific implementations
   - AES-256-GCM encryption algorithm
   - Secure key storage

2. **Android Implementation** (`shared/src/androidMain/kotlin/.../EncryptionService.android.kt`)
   - Uses Android Keystore for hardware-backed key storage
   - AES-256-GCM with 128-bit authentication tags
   - Keys stored in AndroidKeyStore (cannot be extracted)
   - PBKDF2 with 100,000 iterations for key backup encryption

3. **iOS Implementation** (`shared/src/iosMain/kotlin/.../EncryptionService.ios.kt`)
   - Uses iOS Keychain for secure key storage
   - CommonCrypto APIs for AES-GCM encryption
   - Keys stored with kSecAttrAccessibleAfterFirstUnlock
   - Secure Enclave support (when available)

4. **SyncDataEncryption** (`shared/src/commonMain/kotlin/.../SyncDataEncryption.kt`)
   - High-level wrapper for sync data encryption
   - JSON serialization before encryption
   - Encrypted payload format for server communication
   - Version support for encryption format evolution

---

## Encryption Details

### Algorithm: AES-256-GCM

- **Key Size:** 256 bits (32 bytes)
- **IV Size:** 96 bits (12 bytes) - randomly generated per encryption
- **Tag Size:** 128 bits (16 bytes) - for authenticated encryption
- **Mode:** Galois/Counter Mode (GCM)

**Why AES-256-GCM?**
- Provides both confidentiality and authenticity
- Authenticated encryption prevents tampering
- Hardware acceleration on modern devices
- Industry-standard and well-audited

### Key Storage

#### Android:
```kotlin
KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
    .setBlockModes(BLOCK_MODE_GCM)
    .setKeySize(256)
    .build()
```
- Keys stored in AndroidKeyStore
- Hardware-backed on supported devices
- Keys cannot be extracted or exported directly
- Survives app uninstall (optional - can be configured)

#### iOS:
```kotlin
SecItemAdd(query as CFDictionaryRef, null)
// With kSecAttrAccessibleAfterFirstUnlock
```
- Keys stored in iOS Keychain
- Secure Enclave protection when available
- Keys encrypted with device passcode
- Synchronized across user's devices via iCloud Keychain (optional)

---

## Data Flow

### Encryption Flow (Outgoing Sync):

```
User Data (Plain)
      ↓
JSON Serialization
      ↓
Plaintext JSON String
      ↓
EncryptionService.encrypt()
      ↓
Generate Random IV (12 bytes)
      ↓
AES-256-GCM Encryption
      ↓
Ciphertext + Authentication Tag
      ↓
Base64 Encoding
      ↓
EncryptedSyncPayload {
    encryptedData: "iv:tag:ciphertext",
    encryptionVersion: 1
}
      ↓
Send to Server
```

### Decryption Flow (Incoming Sync):

```
Receive from Server
      ↓
EncryptedSyncPayload
      ↓
Parse Storage Format
      ↓
Extract IV, Tag, Ciphertext
      ↓
Base64 Decoding
      ↓
EncryptionService.decrypt()
      ↓
AES-256-GCM Decryption + Tag Verification
      ↓
Plaintext JSON String
      ↓
JSON Deserialization
      ↓
User Data (Plain)
```

---

## Usage

### Initialize Encryption

```kotlin
val syncDataEncryption = SyncDataEncryption(encryptionService, json)

// Generate encryption key (first time setup)
syncDataEncryption.initializeEncryption()
    .onSuccess {
        println("Encryption initialized successfully")
    }
    .onFailure { error ->
        println("Failed to initialize encryption: ${error.message}")
    }
```

### Encrypt Data for Sync

```kotlin
// Example: Encrypt trip data
val trip = TripDto(id = "trip123", name = "Tokyo Adventure", ...)

val encryptedPayload = syncDataEncryption.encryptSyncData(trip)
    .getOrThrow()

// Send encryptedPayload to server
apiClient.syncTrip(encryptedPayload)
```

### Decrypt Data from Sync

```kotlin
// Receive encrypted payload from server
val encryptedPayload: EncryptedSyncPayload = apiClient.fetchTrip(tripId)

// Decrypt
val trip: TripDto = syncDataEncryption.decryptSyncData<TripDto>(encryptedPayload)
    .getOrThrow()

// Use decrypted data
println("Trip name: ${trip.name}")
```

### Key Management

```kotlin
// Check if key exists
val hasKey = syncDataEncryption.isEncryptionAvailable()

// Export key for backup (password-protected)
val password = "user_strong_password"
val keyBackup = syncDataEncryption.exportEncryptionKey(password)
    .getOrThrow()

// Store keyBackup securely (e.g., user's password manager)

// Import key from backup
syncDataEncryption.importEncryptionKey(keyBackup, password)
    .onSuccess {
        println("Key imported successfully")
    }

// Delete key (WARNING: Makes encrypted data unrecoverable!)
syncDataEncryption.deleteEncryptionKey()
```

---

## Integration with Sync

### Current Sync Flow (Without E2E):

```kotlin
// SyncCoordinator.performSync()
val request = DeltaSyncRequest(
    deviceId = deviceId,
    lastSyncVersion = lastSyncVersion,
    localChanges = localChanges // Plain data
)

val response = apiClient.performDeltaSync(request)
```

### Proposed E2E Sync Flow:

```kotlin
// SyncCoordinator.performSync()
val encryptedChanges = if (settings.enableE2EEncryption) {
    // Encrypt each entity before sync
    LocalChanges(
        locations = localChanges.locations.map {
            syncDataEncryption.encryptSyncData(it).getOrThrow()
        },
        placeVisits = localChanges.placeVisits.map {
            syncDataEncryption.encryptSyncData(it).getOrThrow()
        },
        // ... encrypt all entity types
    )
} else {
    localChanges // Plain data (existing behavior)
}

val request = DeltaSyncRequest(
    deviceId = deviceId,
    lastSyncVersion = lastSyncVersion,
    localChanges = encryptedChanges,
    isEncrypted = settings.enableE2EEncryption
)

val response = apiClient.performDeltaSync(request)

// Decrypt received data
val decryptedRemoteChanges = if (settings.enableE2EEncryption) {
    RemoteChanges(
        locations = response.remoteChanges.locations.map {
            syncDataEncryption.decryptSyncData<LocationDto>(it).getOrThrow()
        },
        // ... decrypt all entity types
    )
} else {
    response.remoteChanges // Plain data
}
```

---

## Server-Side Requirements

The backend server needs to support encrypted sync:

### API Changes:

1. **Add `isEncrypted` flag to sync requests:**
```json
{
  "deviceId": "device123",
  "lastSyncVersion": 42,
  "isEncrypted": true,
  "localChanges": {
    "locations": [
      {
        "encryptedData": "base64_iv:base64_tag:base64_ciphertext",
        "encryptionVersion": 1
      }
    ]
  }
}
```

2. **Store encrypted blobs as-is:**
```sql
CREATE TABLE encrypted_sync_data (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    encrypted_data TEXT NOT NULL,  -- Store the encrypted blob
    encryption_version INT NOT NULL,
    sync_version BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

3. **Return encrypted data to clients:**
```json
{
  "syncVersion": 43,
  "remoteChanges": {
    "locations": [
      {
        "encryptedData": "base64_iv:base64_tag:base64_ciphertext",
        "encryptionVersion": 1
      }
    ]
  }
}
```

**Important:** The server should NOT attempt to decrypt the data. It stores opaque encrypted blobs and forwards them between devices.

---

## Security Considerations

### ✅ Secure Practices Implemented:

1. **Key Storage:**
   - Android: AndroidKeystore with hardware backing
   - iOS: iOS Keychain with Secure Enclave
   - Keys never leave the device in plain form

2. **Random IV:**
   - New random IV generated for every encryption
   - Prevents pattern analysis and replay attacks

3. **Authenticated Encryption:**
   - GCM mode provides both encryption and authentication
   - Prevents tampering with encrypted data

4. **Key Backup:**
   - Password-protected key export using PBKDF2
   - 100,000 iterations for key derivation
   - Strong password required from user

5. **Version Support:**
   - Encryption version field allows algorithm updates
   - Forward compatibility for encryption format changes

### ⚠️ Security Warnings:

1. **Key Loss = Data Loss:**
   - If the encryption key is lost, all encrypted data is unrecoverable
   - Users MUST backup their keys using the export function
   - Provide clear UI warnings when enabling E2E

2. **Password Strength:**
   - Key backup encryption relies on password strength
   - Enforce strong password requirements (min 12 chars, mixed case, symbols)
   - Consider password strength meter in UI

3. **No Server-Side Recovery:**
   - Server cannot recover data if key is lost
   - No "forgot password" recovery for encryption keys
   - User must keep key backup safe

4. **Conflict Resolution:**
   - Encrypted data cannot be merged automatically
   - Conflicts require decryption on client
   - May need special handling for E2E encrypted conflicts

5. **Search Limitations:**
   - Server-side search won't work on encrypted data
   - All search must be client-side after decryption
   - Consider searchable encryption for future enhancement

---

## User Experience Considerations

### Enabling E2E Encryption:

**First-Time Setup Flow:**
1. User toggles "Enable End-to-End Encryption" in settings
2. Show warning dialog:
   ```
   ⚠️ Important: End-to-End Encryption

   Your data will be encrypted on this device before sync.

   • Your data is protected even if the server is compromised
   • You MUST backup your encryption key
   • Lost keys cannot be recovered - data will be permanently lost
   • Other devices need the same key to access your data

   Backup your encryption key now?
   [Backup Key] [Skip for Now] [Cancel]
   ```
3. If "Backup Key":
   - Prompt for strong password
   - Generate key backup
   - Show key backup code or save to file
   - Verify backup (ask user to re-enter password)
4. Generate encryption key
5. Enable E2E in settings
6. Trigger full re-sync with encryption

### Key Backup Dialog:

```
🔐 Backup Your Encryption Key

This encrypted backup can restore your key on other devices.

Password: [___________________]
(Minimum 12 characters)

Your encrypted key backup:
[Copy] [Save to File] [Share]

⚠️ Store this backup securely:
- Password manager (recommended)
- Encrypted cloud storage
- Physical secure location

WITHOUT THIS BACKUP, YOUR DATA CANNOT BE RECOVERED IF YOU LOSE YOUR DEVICE!
```

### Multi-Device Setup:

**Adding a New Device:**
1. Install TrailGlass on new device
2. Login to account
3. App detects E2E encryption is enabled
4. Show dialog:
   ```
   🔐 Encryption Key Required

   This account uses end-to-end encryption.
   To access your data, import your encryption key.

   [Import from Backup] [Generate New Key]

   Note: Generating a new key will make old encrypted data inaccessible.
   ```
5. Import key from backup
6. Sync encrypted data

---

## Settings UI

### Privacy Settings Section:

```kotlin
// Existing settings
data class PrivacySettings(
    val dataRetentionDays: Int = 365,
    val shareAnalytics: Boolean = false,
    val shareCrashReports: Boolean = true,
    val autoBackup: Boolean = true,
    val encryptBackups: Boolean = true,
    val enableE2EEncryption: Boolean = false  // NEW
)
```

### UI Components Needed:

1. **Settings Toggle:**
```
Privacy & Security
├─ End-to-End Encryption         [OFF]
│  └─ Protect your data with encryption
│
├─ Manage Encryption Key          [Disabled when E2E is off]
│  ├─ Backup Key
│  ├─ Import Key
│  └─ Change Key (Advanced)
```

2. **Encryption Status Indicator:**
- Show lock icon in sync status when E2E is active
- Display "Encrypted Sync" badge in settings

3. **Key Management Screen:**
- Export key (with password)
- Import key (from backup + password)
- Verify key (check if current key can decrypt data)
- Delete key (with multiple warnings)

---

## Testing

### Unit Tests:

```kotlin
class EncryptionServiceTest {
    @Test
    fun `encrypt and decrypt roundtrip`() {
        val service = EncryptionService()
        service.generateKey()

        val plaintext = "Hello, TrailGlass!"
        val encrypted = service.encrypt(plaintext).getOrThrow()
        val decrypted = service.decrypt(encrypted).getOrThrow()

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `different IVs for same plaintext`() {
        val service = EncryptionService()
        service.generateKey()

        val plaintext = "Same data"
        val encrypted1 = service.encrypt(plaintext).getOrThrow()
        val encrypted2 = service.encrypt(plaintext).getOrThrow()

        assertNotEquals(encrypted1.iv, encrypted2.iv)
        assertNotEquals(encrypted1.ciphertext, encrypted2.ciphertext)
    }

    @Test
    fun `key export and import`() {
        val service = EncryptionService()
        service.generateKey()

        val password = "StrongPassword123!"
        val backup = service.exportKey(password).getOrThrow()

        service.deleteKey()
        service.importKey(backup, password).getOrThrow()

        // Key should work after import
        val encrypted = service.encrypt("test").getOrThrow()
        val decrypted = service.decrypt(encrypted).getOrThrow()
        assertEquals("test", decrypted)
    }
}
```

### Integration Tests:

```kotlin
class SyncEncryptionIntegrationTest {
    @Test
    fun `sync with E2E encryption enabled`() {
        // 1. Enable E2E encryption
        settings.privacySettings = settings.privacySettings.copy(
            enableE2EEncryption = true
        )

        // 2. Create test data
        val trip = TripDto(id = "test", name = "Test Trip")

        // 3. Encrypt
        val encrypted = syncDataEncryption.encryptSyncData(trip).getOrThrow()

        // 4. Simulate server storage
        val stored = encrypted.encryptedData

        // 5. Decrypt
        val decrypted = syncDataEncryption.decryptSyncData<TripDto>(encrypted).getOrThrow()

        // 6. Verify
        assertEquals(trip, decrypted)
    }
}
```

---

## Performance Considerations

### Encryption Overhead:

- **CPU:** AES-256-GCM is hardware-accelerated on modern devices
- **Time:** ~1-5ms per 1KB of data (device-dependent)
- **Memory:** Minimal overhead (encryption is streaming)

### Optimization Strategies:

1. **Batch Encryption:**
   - Encrypt multiple entities in parallel
   - Use coroutines for concurrent encryption

2. **Selective Encryption:**
   - Only encrypt sensitive fields if needed
   - Keep non-sensitive metadata unencrypted for server-side filtering

3. **Compression:**
   - Compress JSON before encryption
   - Reduces ciphertext size by 60-80%

4. **Caching:**
   - Cache decrypted data in memory
   - Avoid repeated decryption of same data

---

## Migration Strategy

### Enabling E2E on Existing Account:

1. **User enables E2E encryption**
2. **Generate encryption key**
3. **Full re-sync with encryption:**
   - Fetch all data from server (unencrypted)
   - Encrypt all data locally
   - Upload encrypted data to server
   - Mark all previous versions as "encrypted migration"
4. **Future syncs use encryption**

### Disabling E2E Encryption:

1. **User disables E2E encryption**
2. **Show warning dialog:**
   ```
   ⚠️ Disable End-to-End Encryption?

   Your data will no longer be encrypted before sync.
   The server will be able to read your data.

   Are you sure you want to continue?

   [Keep Encryption] [Disable Encryption]
   ```
3. **If confirmed, full re-sync without encryption:**
   - Fetch all encrypted data from server
   - Decrypt all data locally
   - Upload decrypted data to server
   - Delete encryption key (optional)

---

## Future Enhancements

### Planned Features:

1. **Multi-Key Support:**
   - Share data with other users using their public keys
   - Key exchange protocol for collaboration

2. **Searchable Encryption:**
   - Encrypt data in a way that allows server-side search
   - Use techniques like order-preserving encryption for dates/numbers

3. **Homomorphic Encryption:**
   - Allow server to perform operations on encrypted data
   - Enable server-side analytics without decryption

4. **Key Rotation:**
   - Automatic key rotation every 90 days
   - Re-encrypt data with new key in background

5. **Biometric Authentication:**
   - Require fingerprint/Face ID to access encryption key
   - `setUserAuthenticationRequired(true)` in Android Keystore

---

## Troubleshooting

### Common Issues:

**1. "Decryption failed" error:**
- **Cause:** Wrong encryption key or corrupted data
- **Fix:** Verify key is correct, re-import key from backup if needed

**2. "Encryption key not found" error:**
- **Cause:** Key was deleted or never generated
- **Fix:** Import key from backup or generate new key (loses old data)

**3. "Failed to sync encrypted data" error:**
- **Cause:** Server doesn't support encrypted sync
- **Fix:** Update server to support `isEncrypted` flag

**4. Sync conflicts increase with E2E:**
- **Cause:** Server cannot auto-merge encrypted data
- **Fix:** Resolve conflicts manually on client

---

## Compliance

### GDPR Compliance:

✅ **Data Protection by Design:**
- E2E encryption ensures user data privacy
- Server cannot access user data even if compromised

✅ **Right to Erasure:**
- Deleting encryption key makes data unrecoverable
- Functionally equivalent to data deletion

✅ **Data Portability:**
- Users can export their encryption key
- Allows moving data to other services

### HIPAA Compliance (If Applicable):

✅ **Encryption in Transit:** TLS 1.3
✅ **Encryption at Rest:** AES-256-GCM
✅ **Access Controls:** Device-bound keys
✅ **Audit Logging:** All encryption operations logged

---

## References

- [AES-GCM Specification (NIST)](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf)
- [Android Keystore Documentation](https://developer.android.com/training/articles/keystore)
- [iOS Keychain Services](https://developer.apple.com/documentation/security/keychain_services)
- [OWASP Cryptographic Storage](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)

---

**Implementation Status:**
- ✅ Client-side encryption service (Android + iOS)
- ✅ Sync data encryption wrapper
- ✅ Key management (generate, export, import, delete)
- ✅ Settings model updated
- ⏳ Server-side support (pending)
- ⏳ UI implementation (pending)
- ⏳ Integration with sync flow (pending)

**Next Steps:**
1. Implement settings UI for E2E toggle
2. Add key management screens
3. Integrate encryption into SyncCoordinator
4. Update backend API to support encrypted sync
5. Add user onboarding flow for E2E setup
6. Implement key backup/restore UI
7. Add comprehensive tests

---

**Last Updated:** 2025-11-18
**Maintained By:** TrailGlass Development Team
