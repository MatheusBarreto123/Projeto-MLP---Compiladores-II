package org.example;

import java.util.HashMap;
import java.util.Map;

/**
 * TabelaSimbolos.java
 * Estrutura de dados principal (Tabela Hash) para a Análise Semântica.
 */
public class TabelaSimbolos {
    private final Map<String, EntradaTabelaSimbolos> tabela;

    public TabelaSimbolos() {
        this.tabela = new HashMap<>();
    }

    /**
     * Insere um novo identificador na tabela, verificando duplicidade.
     */
    public void inserir(String lexema, TipoDado tipo, int linha, int coluna) throws Exception {
        if (tabela.containsKey(lexema)) {
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Variável '" + lexema + "' já foi declarada (COD. SEM_01 - Variável duplicada).");
        }
        tabela.put(lexema, new EntradaTabelaSimbolos(lexema, tipo));
    }

    /**
     * Busca um identificador na tabela.
     */
    public EntradaTabelaSimbolos buscar(String lexema) {
        return tabela.get(lexema);
    }

    /**
     * Exibe o conteúdo da Tabela de Símbolos.
     */
    public void imprimirTabela() {
        System.out.println("\n--- Tabela de Símbolos ---");
        System.out.println("Identificador     | Tipo");
        System.out.println("------------------|-----");
        if (tabela.isEmpty()) {
            System.out.println("Tabela vazia (nenhuma variável declarada).");
        }
        for (EntradaTabelaSimbolos entrada : tabela.values()) {
            System.out.println(entrada);
        }
        System.out.println("--------------------------");
    }
}
