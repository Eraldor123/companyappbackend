---
name: Frontend Komponenta
about: Šablona pro novou frontend komponentu s checklistem kvality
title: '[FE]: '
labels: frontend, task
---

## Popis
(Zde popiš, co má komponenta dělat a proč ji vytváříme.)

## Checklist kvality

### 1. Statické a typové testy
- [ ] TypeScript/Typy definované a sedí.
- [ ] ESLint bez varování.
- [ ] Kód je přehledný (ne příliš dlouhý).

### 2. Funkční testy
- [ ] Komponenta se renderuje bez chyb.
- [ ] Defaultní stav (bez props) je funkční.
- [ ] Interakce (kliknutí/změny) fungují.
- [ ] Komponenta zobrazuje stavy Loading, Error, Empty.

### 3. Integrace a Data
- [ ] API volání jsou mockovaná.
- [ ] Data flow správně zpracovává props.
- [ ] Chybové stavy API jsou ošetřeny.

### 4. Přístupnost (A11y) - Klíčové pro Kiosk
- [ ] Touch Targets: Tlačítka jsou min. 44x44px.
- [ ] Kontrast barev splňuje standardy.
- [ ] Použity správné HTML tagy (button vs div).

### 5. Vizuální validace
- [ ] Tablet/Kiosk layout je responzivní.
- [ ] Snapshot vytvořen (pokud je potřeba).
