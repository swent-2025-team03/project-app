## TL;DR
Backend pour lier **Vétérinaire ↔️ Fermier** via **code à 6 chiffres** avec TTL.  
Pas d’UI dans cette PR. L’UI n’a qu’à appeler le ViewModel et afficher l’état.

## Ce que fait cette PR
- Ajoute la logique Firestore :
    - Génération d’un **code unique** par un vet, valable **10 min**.
    - **Réclamation** du code par un farmer.
    - Création du lien persistant **`connections/{vet__farmer}`**.
    - Invalidation du code (`status = USED`).
- Expose une API simple pour l’UI via ViewModel + `StateFlow`.

---

## Fichiers clés
- `ConnectionRepository.kt` : Firestore (create/claim/link).
- `ConnectionViewModel.kt` : `generateCode(vetId)`, `claimCode(code, farmerId)`, `state`.
- `ConnectionUiState.kt` : `Idle | Loading | CodeGenerated(code) | Connected(vetId) | Error(msg)`.

---

## Modèle Firestore
**Codes**


## Flux fonctionnel
1) Vet → `generateCode(vetId)` → doc dans `/connect_codes/{code}` → `CodeGenerated(code)`.
2) Farmer → `claimCode(code, farmerId)` → validations (existence, OPEN, non expiré) →  
   création `/connections/{vet__farmer}` + `status=USED` → `Connected(vetId)`.

---

## Intégration UI (à faire par @MadaMada08)
- Appeler `viewModel.generateCode(vetId)` et afficher `CodeGenerated(code)`.
- Champ saisie code → `viewModel.claimCode(code, farmerId)`.
- Afficher selon `state` : `Loading`, `Error(msg)`, `Connected(vetId)`.

---

## Règles Firestore (proposées)
```javascript
match /connect_codes/{code} {
  allow create: if request.auth.token.role == "vet";
  allow update: if request.auth.token.role == "farmer";
  allow read: if request.auth != null;
}
match /connections/{id} {
  allow read, write: if request.auth.uid in [resource.data.vetId, resource.data.farmerId];
}