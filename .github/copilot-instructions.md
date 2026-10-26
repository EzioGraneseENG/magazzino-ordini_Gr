# Contesto del progetto
Questo progetto riguarda un' applicazione per la gestione di magazzino e ordini

Quando modifichi il repository, preserva l'architettura esistente, le regole di business, le convenzioni di codice e la strategia di test.

Preferisci modifiche piccole, focalizzate e coerenti rispetto a refactoring estesi non richiesti.

Prima di implementare una modifica:

1) analizza il codice esistente interessato dalla modifica;
2) analizza i test correlati;
3) identifica le regole di business coinvolte;
4) individua il layer corretto in cui implementare la modifica;
5) implementa la soluzione più semplice e coerente con l'architettura;
6) aggiungi o aggiorna i test necessari;
7) identifica i casi limite (valori nulli, valori limite, situazioni di potenziale conflitto...) per ciascun caso di test
8) esegui i test pertinenti e successivamente l'intera suite di test.
9) Non modificare il comportamento esistente dell'applicazione se il task non lo richiede esplicitamente.

---
# Stack tecnologico
Utilizza lo stack tecnologico già adottato dal progetto:

- Linguaggio: Java 25
- Framework: Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Frontend: Thymeleaf
- DB: H2
- Test: JUnit 5
- Test: Spring Boot Test
- MockMvc
- Maven Wrapper

Non introdurre nuovi framework, librerie, plugin Maven o componenti infrastrutturali se non sono chiaramente necessari per il requisito da implementare.

Utilizza il Maven Wrapper presente nel repository anziché assumere che Maven sia installato globalmente.

---
# Architettura
L'applicazione segue il seguente flusso:

*controller -> service -> model/repository*

Mantieni questa separazione delle responsabilità durante l'implementazione e il refactoring del codice.

**model**

Contiene:

entità di dominio;
regole di business;
eventuali value object;
eccezioni di dominio;
logica relativa allo stato delle entità.
Quando una regola può essere espressa direttamente dal modello di dominio, implementala nel model.

Il codice di dominio non deve dipendere dal layer web.

**repository**

Contiene i repository Spring Data JPA e la logica di accesso alla persistenza.

Non inserire regole di business nei repository.

**service**

Contiene i casi d'uso applicativi e coordina il modello di dominio con i repository.

I service devono:

orchestrare le operazioni necessarie a un caso d'uso;
utilizzare le regole definite nel modello;
definire i corretti confini transazionali quando necessario;
garantire la consistenza delle operazioni che coinvolgono ordini e magazzino.
Non duplicare nei service regole già correttamente incapsulate nel modello.

**web**

Contiene:
- controller Spring MVC;
- form;
- validazione dell'input web;
- gestione degli errori;
- preparazione dei dati destinati alle view Thymeleaf.

I controller devono:
- ricevere e validare l'input HTTP o dei form;
- invocare il service appropriato;
- preparare i dati necessari alla view;
- selezionare la view o il redirect appropriato.
- I controller non devono contenere regole di business.

Non accedere direttamente ai repository dai controller se l'operazione rappresenta un caso d'uso applicativo.

**config**

Contiene la configurazione dell'applicazione e gli aspetti infrastrutturali, compresi componenti quali Clock e l'inizializzazione dei dati.

**Regole di business**

Considera le seguenti regole come invarianti del dominio.

Non modificarle implicitamente per semplificare un'implementazione.

*Ciclo di vita dell'ordine*

Un nuovo ordine nasce nello stato: CONFERMATO

Le transizioni consentite sono esclusivamente: 
CONFERMATO -> SPEDITO
oppure:
CONFERMATO -> ANNULLATO

Non introdurre implicitamente ulteriori transizioni di stato.

Un ordine SPEDITO non può essere annullato.

*Gestione delle quantità di magazzino*

La creazione di un ordine preleva dal magazzino le quantità dei prodotti ordinati.
Se la quantità disponibile di uno dei prodotti non è sufficiente, l'ordine non deve essere creato.
Evita situazioni nelle quali un'operazione fallita lasci ordine e magazzino in uno stato parzialmente aggiornato.
L'annullamento di un ordine deve restituire al magazzino le quantità precedentemente prelevate.
Le operazioni che modificano contemporaneamente ordine e disponibilità di magazzino devono preservare la consistenza dei dati.

*Righe dell'ordine*

Se lo stesso prodotto viene aggiunto più volte allo stesso ordine, deve essere rappresentato da una sola riga con la quantità complessiva appropriata.
Il prezzo registrato nella riga dell'ordine deve essere quello del prodotto al momento della creazione dell'ordine.
Una successiva variazione del prezzo del prodotto non deve modificare il prezzo memorizzato negli ordini già creati.

*Valori monetari*

Utilizza BigDecimal per prezzi, importi e totali.
Non utilizzare double o float per rappresentare valori monetari.
Per gli arrotondamenti, rispetta le convenzioni già utilizzate nel codice esistente.

---
# Test

Ogni modifica che introduce o cambia un comportamento deve essere coperta da un test appropriato

---

# Linee guida per il codice
Segui lo stile, la nomenclatura, la struttura dei package e le convenzioni già presenti nelle classi vicine a quella che stai modificando.

Preferisci:

- nomi chiari e coerenti con il dominio;
- metodi con una responsabilità ben definita;
- dependency injection tramite costruttore;
- comportamento di business esplicito;
- riutilizzo dei tipi e delle eccezioni di dominio esistenti;
- soluzioni semplici e direttamente collegate al requisito.

Evita:

- duplicazione della logica di business;
- astrazioni non necessarie;
- generalizzazioni speculative;
- refactoring non collegati al task corrente;
- nuove dipendenze per funzionalità già supportate dallo stack esistente;
- modifiche cosmetiche estese insieme a una modifica funzionale.

Quando aggiungi una nuova classe, inseriscila nel package responsabile della relativa funzionalità.

**Modalità operativa dell'agente**

Prima di implementare un task non banale, analizza il repository e identifica:

- componenti coinvolti;
- regole di business interessate;
- test esistenti pertinenti;
- layer architetturali coinvolti;
- approccio implementativo previsto.
- Successivamente procedi con l'implementazione.

Durante l'implementazione:

- preserva i comportamenti esistenti non interessati dal task;
- mantieni la logica di business fuori dai controller;
- preserva la consistenza transazionale delle operazioni su ordine e magazzino;
- accompagna le modifiche al codice di produzione con i relativi test;
- mantieni il diff focalizzato sul requisito richiesto;
- evita refactoring non necessari.

Gestione delle ambiguità
Non inventare nuove regole di business quando il requisito non è sufficientemente chiaro.
In presenza di un comportamento ambiguo:
- analizza le classi di dominio esistenti;
- analizza i test esistenti;
- verifica i comportamenti già implementati;
- privilegia una soluzione coerente con gli invarianti esistenti.
- Se il requisito richiesto entra in conflitto con una regola di business esistente, non modificare silenziosamente la regola per completare il task.
Evidenzia il conflitto prima di introdurre il nuovo comportamento.

*Validazione delle modifiche*
Dopo ogni modifica, esegui i test appropriati.
Se un test fallisce:
- identifica la causa del fallimento;
- verifica se il problema è nel codice modificato oppure evidenzia un'incompatibilità con il requisito;
- correggi l'implementazione anziché modificare il test per nascondere il problema.

Al termine del task, riepiloga:
- cosa è stato modificato;
- quali regole di business sono state coinvolte;
- quali test sono stati aggiunti o modificati;
- quali test sono stati eseguiti;
- l'esito dei test.
- Criteri di completamento

Considera un task completato quando:
- il requisito richiesto è implementato;
- l'architettura esistente è rispettata;
- gli invarianti di dominio continuano a essere rispettati;
- la modifica è coperta dai test appropriati;
- i test pertinenti passano;
- non sono state introdotte modifiche non necessarie;
- non sono state introdotte nuove dipendenze senza necessità.

---
**Azioni da evitare**

*Non:*

inserire logica di business nei controller MVC;
accedere direttamente ai repository dai controller per implementare casi d'uso applicativi;
aggirare gli invarianti del dominio per semplificare un'implementazione;
utilizzare tipi floating-point per i calcoli monetari;
modificare accidentalmente le transizioni di stato degli ordini;
lasciare ordine e magazzino in uno stato parzialmente aggiornato;
eliminare o indebolire test per adattarli a un comportamento errato;
introdurre dipendenze senza una necessità concreta;
eseguire refactoring estesi non richiesti dal task;
inventare requisiti o comportamenti non presenti nel dominio.