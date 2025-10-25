## TL;DR
Backend to link Vet ↔ Farmer via a 6‑digit code with TTL.  
No UI in this PR. The UI only needs to call the ViewModel and render the state.

## What this PR does
- Adds Firestore logic:
  - Generation of a unique code by a vet, valid for 10 minutes.
  - Code claim by a farmer.
  - Creation of a persistent link `connections/{vet__farmer}`.
  - Code invalidation (`status = USED`).
- Exposes a simple API for the UI via ViewModel + `StateFlow`.

---

## Key files
- `ConnectionRepository.kt`: Firestore (create/claim/link).
- `ConnectionViewModel.kt`: `generateCode(vetId)`, `claimCode(code, farmerId)`, `state`.
- `ConnectionUiState.kt`: `Idle | Loading | CodeGenerated(code) | Connected(vetId) | Error(msg)`.

---

## Firestore model
**Codes**


## Flow
1) Vet → `generateCode(vetId)` → doc created at `/connect_codes/{code}` → `CodeGenerated(code)`.
2) Farmer → `claimCode(code, farmerId)` → validations (exists, OPEN, not expired) →
   create `/connections/{vet__farmer}` + set `status=USED` → `Connected(vetId)`.

---

## UI integration (to be done by @MadaMada08)
- Call `viewModel.generateCode(vetId)` and display `CodeGenerated(code)`.
- Input field to enter the code → `viewModel.claimCode(code, farmerId)`.
- Render according to `state`: `Loading`, `Error(msg)`, `Connected(vetId)`.

---

## Proposed Firestore rules
```javascript
match /connect_codes/{code} {
  allow create: if request.auth.token.role == "vet";
  allow update: if request.auth.token.role == "farmer";
  allow read: if request.auth != null;
}
match /connections/{id} {
  allow read, write: if request.auth.uid in [resource.data.vetId, resource.data.farmerId];
}
```
