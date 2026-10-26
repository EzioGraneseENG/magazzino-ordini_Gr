package com.epicode.magazzino.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Le pagine principali si aprono e il form crea davvero un ordine. */
@SpringBootTest
@AutoConfigureMockMvc
class PagineTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void elencoProdotti() throws Exception {
        mvc.perform(get("/prodotti"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trapano a batteria 18V")));
    }

    @Test
    void nuovoOrdineMostraIlPrezzoUnitarioDelleOpzioniSelezionabili() throws Exception {
        mvc.perform(get("/ordini/nuovo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prezzo unitario")))
                .andExpect(content().string(containsString("data-prezzo=\"3,20\"")))
                .andExpect(content().string(containsString("class=\"prezzo-unitario\" aria-live=\"polite\">—</span>")))
                .andExpect(content().string(containsString("opzione.dataset.prezzo + \" €\"")));
    }

    @Test
    void ilFormCreaUnOrdineEPortaAlDettaglio() throws Exception {
        mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Bianchi")
                        .param("righe[0].codice", "GUA-L")
                        .param("righe[0].quantita", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/ordini/*"));
    }

    @Test
    void dettaglioOrdineMostraScontoTotaliESpedizione() throws Exception {
        String urlDettaglio = mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Sconto")
                        .param("righe[0].codice", "GUA-L")
                        .param("righe[0].quantita", "10"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        mvc.perform(get(urlDettaglio))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("5%")))
                .andExpect(content().string(containsString("30,40 €")))
                .andExpect(content().string(containsString("9,90 €")))
                .andExpect(content().string(containsString("40,30 €")));
    }

    @Test
    void elencoOrdiniMostraIlTotaleDaPagare() throws Exception {
        mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Lista")
                        .param("righe[0].codice", "GUA-L")
                        .param("righe[0].quantita", "2"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/ordini"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ferramenta Lista")))
                .andExpect(content().string(containsString("16,30 €")));
    }

    @Test
    void dettaglioOrdineIndicaLaSpedizioneGratuita() throws Exception {
        String urlDettaglio = mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Spedizione")
                        .param("righe[0].codice", "TRA-18V")
                        .param("righe[0].quantita", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        mvc.perform(get(urlDettaglio))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gratuita")))
                .andExpect(content().string(containsString("178,00 €")));
    }

    @Test
    void ilFormMostraLErroreSeLaScortaNonBasta() throws Exception {
        mvc.perform(post("/ordini")
                        .param("cliente", "Ferramenta Bianchi")
                        .param("righe[0].codice", "NAS-50")
                        .param("righe[0].quantita", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scorta insufficiente per NAS-50")));
    }

    @Test
    void ordineInesistente() throws Exception {
        mvc.perform(get("/ordini/9999"))
                .andExpect(status().isNotFound());
    }
}
