applyTo: "src/test/**"

# Strategia di test
Ogni modifica che introduce o cambia un comportamento deve essere coperta da un test appropriato, considerando i casi limite, i casi di errore, i casi particolari (esempio valori nulli, valori negativi, valori massimi).

Preferisci test di dominio senza avviare il contesto Spring per verificare:

- regole di business;
- transizioni di stato;
- disponibilità di magazzino;
- prelievo e ripristino delle quantità;
- aggregazione delle righe d'ordine;
- prezzi e importi;
- validazioni del dominio.

Utilizza Spring Boot Test e MockMvc quando devi verificare comportamenti che richiedono Spring o il layer MVC.

QUando crei un caso di test e di validazione, utilizza dove possibili valori nominali anzichè calcolati (esempio: se devi verificare uno sconto, nell'assert.equals non inserire il campo calcolato ma metti un valore specifico per lo sconto da verificare e comparare con il risultato del calcolo)

Non utilizzare un test di integrazione se lo stesso comportamento può essere verificato efficacemente con un test di dominio più semplice.

Quando correggi un bug, aggiungi quando possibile un test di regressione che dimostri il problema e verifichi la correzione.

Non eliminare, disabilitare o indebolire un test esistente soltanto per far passare la build.

Evita mocking non necessario degli oggetti di dominio.

*Validazione delle modifiche*
Dopo ogni modifica, esegui i test appropriati.
Se un test fallisce:
- identifica la causa del fallimento;
- verifica se il problema è nel codice modificato oppure evidenzia un'incompatibilità con il requisito;
- correggi l'implementazione anziché modificare il test per nascondere il problema.