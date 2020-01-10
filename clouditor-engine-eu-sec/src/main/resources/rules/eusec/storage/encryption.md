# Enable Encryption

Storage objects should be encrypted either with AES-256 or client-side encrypted using RSA-OAEP.

```ccl
StorageObject has encryption.algorithm within "AES-256", "RSA-OAEP 2048 SHA-256"
```

## Controls

- Cloud Control Matrix/EKM-04
- BSI C5/KRY-03
