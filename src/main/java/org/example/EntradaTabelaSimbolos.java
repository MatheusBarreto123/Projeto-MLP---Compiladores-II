package org.example;

/**
 * EntradaTabelaSimbolos.java
 * Representa uma entrada na Tabela de Símbolos.
 */
public class EntradaTabelaSimbolos {
    private final String lexema;
    private final TipoDado tipo;

    public EntradaTabelaSimbolos(String lexema, TipoDado tipo) {
        this.lexema = lexema;
        this.tipo = tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public TipoDado getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return String.format("%-15s | %s", lexema, tipo.name());
    }
}
