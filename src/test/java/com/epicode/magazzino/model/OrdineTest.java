package com.epicode.magazzino.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrdineTest {

    private static final LocalDateTime ADESSO = LocalDateTime.of(2026, 10, 26, 9, 0);

    private Prodotto viti;
    private Prodotto trapano;

    @BeforeEach
    void catalogo() {
        viti = new Prodotto("VIT", "Viti", new BigDecimal("6.90"), 100, 10);
        trapano = new Prodotto("TRA", "Trapano", new BigDecimal("89.00"), 5, 1);
    }

    @Test
    void ilTotaleESommaDeiSubtotali() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 3);      // 20,70
        ordine.aggiungiRiga(trapano, 2);   // 178,00
        assertEquals(new BigDecimal("198.70"), ordine.totale());
    }

    @Test
    void ogniRigaPrelevaDalMagazzino() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 2);
        assertEquals(3, trapano.getDisponibili());
    }

    @Test
    void loStessoProdottoDueVolteFiniscenellaStessaRiga() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 3);
        ordine.aggiungiRiga(viti, 2);
        assertEquals(1, ordine.getRighe().size());
        assertEquals(5, ordine.getRighe().get(0).getQuantita());
    }

    @Test
    void ilPrezzoDellaRigaRestaQuelloDelMomentoDellOrdine() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 1);
        assertEquals(new BigDecimal("6.90"), ordine.getRighe().get(0).getPrezzoUnitario());
    }

    @Test
    void loScontoDipendeDalleSoglieDiQuantita() {
        verificaSconto(1, 0, "6.90");
        verificaSconto(9, 0, "62.10");
        verificaSconto(10, 5, "65.55");
        verificaSconto(49, 5, "321.20");
        verificaSconto(50, 10, "310.50");
    }

    @Test
    void quantitaAggregateDeterminanoLoScontoDellaRiga() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 5);
        ordine.aggiungiRiga(viti, 5);

        assertEquals(1, ordine.getRighe().size());
        assertEquals(10, ordine.getRighe().get(0).getQuantita());
        assertEquals(5, ordine.getRighe().get(0).getScontoPercentuale());
        assertEquals(new BigDecimal("65.55"), ordine.getRighe().get(0).subtotale());
    }

    @Test
    void subtotaleScontatoSiArrotondaAlCentesimoConHalfUp() {
        Prodotto prodotto = new Prodotto("ARR", "Arrotondamento", new BigDecimal("0.05"), 10, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, 10);

        assertEquals(new BigDecimal("0.48"), ordine.getRighe().get(0).subtotale());
    }

    @Test
    void spedizioneGratuitaQuandoIlTotaleMerceScontatoRaggiungeLaSoglia() {
        Ordine sottoSoglia = ordineConUnaRiga("149.99", 1);
        Ordine sogliaEsatta = ordineConUnaRiga("150.00", 1);

        assertEquals(new BigDecimal("9.90"), sottoSoglia.speseSpedizione());
        assertEquals(new BigDecimal("159.89"), sottoSoglia.totaleDaPagare());
        assertEquals(new BigDecimal("0.00"), sogliaEsatta.speseSpedizione());
        assertEquals(new BigDecimal("150.00"), sogliaEsatta.totaleDaPagare());
    }

    @Test
    void sogliaSpedizioneSiCalcolaSulTotaleMerceDopoGliSconti() {
        Ordine ordine = ordineConUnaRiga("15.78", 10);

        assertEquals(new BigDecimal("157.80"),
                ordine.getRighe().get(0).getPrezzoUnitario().multiply(BigDecimal.TEN));
        assertEquals(new BigDecimal("149.91"), ordine.totale());
        assertEquals(new BigDecimal("9.90"), ordine.speseSpedizione());
        assertEquals(new BigDecimal("159.81"), ordine.totaleDaPagare());
    }

    @Test
    void annullareRestituisceIPezzi() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 2);
        ordine.annulla();
        assertEquals(StatoOrdine.ANNULLATO, ordine.getStato());
        assertEquals(5, trapano.getDisponibili());
    }

    @Test
    void unOrdineSpeditoNonSiAnnulla() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 1);
        ordine.spedisci();
        assertThrows(OperazioneNonConsentitaException.class, ordine::annulla);
        assertEquals(4, trapano.getDisponibili());
    }

    @Test
    void unOrdineAnnullatoNonSiAnnullaDueVolte() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(trapano, 1);
        ordine.annulla();
        assertThrows(OperazioneNonConsentitaException.class, ordine::annulla);
        assertEquals(5, trapano.getDisponibili());
    }

    @Test
    void nonSiAggiungonoRigheAUnOrdineSpedito() {
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(viti, 1);
        ordine.spedisci();
        assertThrows(OperazioneNonConsentitaException.class, () -> ordine.aggiungiRiga(viti, 1));
    }

    @Test
    void clienteObbligatorio() {
        assertThrows(RichiestaNonValidaException.class, () -> new Ordine("  ", ADESSO));
    }

    private void verificaSconto(int quantita, int percentuale, String subtotale) {
        Prodotto prodotto = new Prodotto("VIT-" + quantita, "Viti", new BigDecimal("6.90"), quantita, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, quantita);

        assertEquals(percentuale, ordine.getRighe().get(0).getScontoPercentuale());
        assertEquals(new BigDecimal(subtotale), ordine.getRighe().get(0).subtotale());
    }

    private Ordine ordineConUnaRiga(String prezzoUnitario, int quantita) {
        Prodotto prodotto = new Prodotto("PR-" + prezzoUnitario + "-" + quantita, "Prodotto",
                new BigDecimal(prezzoUnitario), quantita, 0);
        Ordine ordine = new Ordine("Rossi", ADESSO);
        ordine.aggiungiRiga(prodotto, quantita);
        return ordine;
    }
}
