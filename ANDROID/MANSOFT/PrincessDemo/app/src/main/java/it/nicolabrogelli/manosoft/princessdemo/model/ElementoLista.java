package it.nicolabrogelli.manosoft.princessdemo.model;

/**
 * Created by Nicola on 05/10/2017.
 */

public class ElementoLista {

    private String titolo;
    private String descrizione;
    private String tipo;

    public ElementoLista(String titolo, String descrizione, String tipo) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.tipo = tipo;
    }

    public String getTitolo() {
        return this.titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return this.descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getTipo() {
        return this.tipo;
    }



}
