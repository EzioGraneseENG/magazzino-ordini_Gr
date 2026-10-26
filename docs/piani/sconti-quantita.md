# Piano di implementazione: sconti quantità e spedizione ordini

## Problema e approccio

Estendere il calcolo degli ordini esistente con sconti per quantità aggregata di ciascuna riga, subtotali scontati con arrotondamento `HALF_UP`, spese di spedizione e totale da pagare. Il modello di dominio resta responsabile dei calcoli; le view Thymeleaf presentano i valori forniti dal modello.

Decisione chiarita dall’utente: la soglia per la spedizione gratuita si confronta con il totale merce **dopo gli sconti** (somma dei subtotali scontati). Il totale da pagare è il totale merce scontato più 9,90 € quando il totale merce scontato è inferiore a 150,00 €; la spedizione è gratuita da 150,00 € in su.

## Contesto tecnico

- Java 25, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Thymeleaf e H2; nessuna nuova dipendenza.
- Modello coinvolto: `RigaOrdine` e `Ordine`; controller e service esistenti continuano a gestire il caso d’uso senza nuove regole web.
- View coinvolte: `ordine-dettaglio.html` e `ordini.html`.
- Test pertinenti: `OrdineTest`, `OrdineServiceTest` e `PagineTest`; eseguire anche l’intera suite con Maven Wrapper.
- Conservare `BigDecimal`, il prezzo unitario fissato alla creazione della riga, l’aggregazione delle quantità dello stesso prodotto e le transizioni di stato attuali.

## Verifica dei vincoli architetturali

- Regole di sconto e calcolo importi nel dominio: rispettato.
- Nessun accesso ai repository dai controller e nessuna nuova dipendenza: rispettato.
- Nessuna modifica agli invarianti di magazzino, stato ordine o prezzo storico: rispettato.
- Test di dominio per i calcoli; test MVC per la presentazione: rispettato.

## Passi di implementazione

### Piano 1.1 — Calcoli nel dominio

**Requisiti:** R1, R2, R3, R4, R5  
Implementare in `RigaOrdine` lo sconto ricavato dalla quantità complessiva (0% per 1–9, 5% per 10–49, 10% da 50) e il subtotale scontato arrotondato a due decimali con `HALF_UP`. In `Ordine`, esporre il totale merce scontato, applicare a esso la soglia di spedizione (9,90 € sotto 150,00 €, altrimenti zero) e calcolare il totale da pagare. Riutilizzare i calcoli delle righe senza alterare l’attuale aggregazione né il prezzo fissato.

**Task**

- [ ] T001 [Plan:1.1] Aggiungere calcolo percentuale e subtotale scontato in `src/main/java/com/epicode/magazzino/model/RigaOrdine.java`; aggiungere totale merce scontato, spedizione calcolata sul totale scontato e totale da pagare in `src/main/java/com/epicode/magazzino/model/Ordine.java`.
- [ ] T002 [Plan:1.1] Aggiornare `src/test/java/com/epicode/magazzino/model/OrdineTest.java` per soglie 1/9/10/49/50, quantità aggregate 5+5, arrotondamento `HALF_UP`, totale merce scontato inferiore/esatto a 150,00 € (incluso il caso di totale lordo sopra soglia ma scontato sotto soglia) e somma del totale da pagare.

### Piano 1.2 — Presentazione ordine

**Requisiti:** R6  
Nel dettaglio mostrare la percentuale di sconto per riga e, nel riepilogo, totale merce scontato, spedizione (compresa l’indicazione di gratuità) e totale da pagare. Nell’elenco usare il totale da pagare al posto del solo totale merce.

**Task**

- [ ] T003 [Plan:1.2] Aggiornare `src/main/resources/templates/ordine-dettaglio.html` e `src/main/resources/templates/ordini.html` per visualizzare sconto, subtotali scontati e importi riepilogativi.
- [ ] T004 [Plan:1.2] Aggiornare `src/test/java/com/epicode/magazzino/web/PagineTest.java` per verificare nel dettaglio la percentuale, merce, spedizione e dovuto, e nell’elenco il totale da pagare.

### Piano 1.3 — Verifica completa

**Requisiti:** R1, R2, R3, R4, R5, R6  
Verificare che il calcolo applicativo e la persistenza dell’ordine continuino a funzionare con i nuovi importi, quindi eseguire i test mirati e la suite completa tramite `mvnw.cmd`.

**Task**

- [ ] T005 [Plan:1.3] Aggiornare le asserzioni di importo in `src/test/java/com/epicode/magazzino/service/OrdineServiceTest.java` e verificare tutti i test con `.\mvnw.cmd test`.

### Piano 1.4 — Documentazione della feature

**Requisiti:** R1, R2, R3, R4, R5, R6  
Aggiungere al `README.md`, dopo le regole di business già presenti, una nuova sezione **Funzionalità** con una descrizione concisa della feature **Gestione Sconto**. Documentare gli scaglioni basati sulla quantità aggregata della riga, l’arrotondamento dei subtotali, la soglia di spedizione gratuita applicata al totale merce scontato e gli importi visibili nel dettaglio e nell’elenco ordini.

- [ ] T006 [Plan:1.4] Aggiungere dopo le regole di business una sezione “Funzionalità” che documenti Gestione Sconto nel `README.md`, coerentemente con gli scaglioni, l’arrotondamento, la soglia post-sconto e il riepilogo degli importi definiti in R1–R6.

## Mappatura requisiti

| Regola | Copertura nel piano | Evidenza prevista |
|---|---|---|
| R1 — Percentuale per quantità della riga | 1.1, 1.2 | `RigaOrdine.java`, `OrdineTest.java`, `ordine-dettaglio.html` |
| R2 — Subtotale scontato arrotondato `HALF_UP` | 1.1, 1.2 | `RigaOrdine.java`, `OrdineTest.java`, `ordine-dettaglio.html` |
| R3 — Quantità aggregata dello stesso prodotto | 1.1 | `Ordine.java`, `OrdineTest.java` |
| R4 — Totale merce dato dalla somma dei subtotali scontati | 1.1, 1.2 | `Ordine.java`, `OrdineTest.java`, `ordine-dettaglio.html` |
| R5 — Spedizione gratuita da 150,00 € di merce scontata e totale da pagare | 1.1, 1.2 | `Ordine.java`, `OrdineTest.java`, `ordine-dettaglio.html`, `ordini.html` |
| R6 — Sconto e riepilogo nel dettaglio, dovuto nell’elenco | 1.2 | `ordine-dettaglio.html`, `ordini.html`, `PagineTest.java` |
| Documentazione — Descrizione della feature Gestione Sconto | 1.4 | `README.md` |
