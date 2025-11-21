package org.example;

/**
 * EntradaTabelaSimbolos.java
 * Representa uma entrada na Tabela de Símbolos, incluindo
 * informações de alocação e escopo.
 */
public class EntradaTabelaSimbolos {
    private final String lexema;
    private final TipoDado tipo;
    private final int tamanhoBytes;
    private final String rotuloAssembly; // Nome usado no .data (ex: "a", "T1")
    private final String escopo; // NOVO CAMPO: Identifica o escopo (ex: "global", "main", "if_1", etc.)

    // Construtor original (ideal para escopo global ou quando o escopo é simples)
    public EntradaTabelaSimbolos(String lexema, TipoDado tipo, String escopo) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.rotuloAssembly = lexema;
        this.escopo = escopo; // ATUALIZADO

        // Define o tamanho em bytes para a arquitetura x86 (dd = 4 bytes)
        switch (tipo) {
            case INTEIRO:
            case BOOLEANO:
            case REAL:
                // Assumindo 4 bytes (dd = Double Doubleword)
                this.tamanhoBytes = 4;
                break;
            case CARACTER:
                // Assumindo 1 byte (db = Define Byte)
                this.tamanhoBytes = 1;
                break;
            default:
                this.tamanhoBytes = 0;
        }
    }

    // Sobrecarga para compatibilidade com o construtor anterior (assumindo escopo "global" por padrão)
    public EntradaTabelaSimbolos(String lexema, TipoDado tipo) {
        this(lexema, tipo, "global");
    }

    // Getters
    public String getLexema() {
        return lexema;
    }
    public TipoDado getTipo() {
        return tipo;
    }
    public int getTamanhoBytes() {
        return tamanhoBytes;
    }
    public String getRotuloAssembly() {
        return rotuloAssembly;
    }
    public String getEscopo() { // NOVO GETTER
        return escopo;
    }

    @Override
    public String toString() {
        // Exibição atualizada com o campo escopo
        return String.format("%-18s| %-12s| %-8s| %-10s", lexema, tipo.name(), rotuloAssembly, escopo);
    }
}