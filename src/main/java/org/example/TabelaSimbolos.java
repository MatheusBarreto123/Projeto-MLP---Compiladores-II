package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * TabelaSimbolos.java
 * Estrutura de dados principal (Tabela Hash) para a Análise Semântica,
 * agora com suporte a escopos aninhados (implementado via String de escopo na chave).
 */
public class TabelaSimbolos {

    // A chave agora será uma string composta: "lexema@escopo" para permitir
    // variáveis de mesmo nome em escopos diferentes (ex: x@global e x@funcaoA)
    private final Map<String, EntradaTabelaSimbolos> tabela;
    // Pilha para rastrear o escopo atual, crucial para a análise semântica
    private final Stack<String> escopoAtual;

    public TabelaSimbolos() {
        this.tabela = new HashMap<>();
        this.escopoAtual = new Stack<>();
        // Inicia com o escopo global
        abrirNovoEscopo("global");
    }

    /**
     * Retorna a chave composta (lexema@escopo) para a inserção na tabela.
     */
    private String gerarChave(String lexema, String escopo) {
        return lexema + "@" + escopo;
    }

    /**
     * Adiciona um novo escopo à pilha (ex: início de função, 'if' ou bloco).
     */
    public void abrirNovoEscopo(String nomeEscopo) {
        this.escopoAtual.push(nomeEscopo);
        //
    }

    /**
     * Remove o escopo mais recente da pilha (ex: fim de função ou bloco).
     */
    public void fecharEscopo() {
        if (!this.escopoAtual.isEmpty()) {
            this.escopoAtual.pop();
        }
    }

    /**
     * Obtém o nome do escopo atual (topo da pilha).
     */
    public String getEscopoAtual() {
        return this.escopoAtual.peek();
    }


    /**
     * Insere um novo identificador na tabela, verificando duplicidade DENTRO DO ESCOPO ATUAL.
     */
    public void inserir(String lexema, TipoDado tipo, int linha, int coluna) throws Exception {
        String escopo = getEscopoAtual();
        String chave = gerarChave(lexema, escopo);

        if (tabela.containsKey(chave)) {
            // A checagem de duplicidade é feita apenas no escopo atual.
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Variável '" + lexema + "' já foi declarada no escopo '" + escopo +
                    "' (COD. SEM_01 - Variável duplicada).");
        }
        // A EntradaTabelaSimbolos agora recebe o escopo
        tabela.put(chave, new EntradaTabelaSimbolos(lexema, tipo, escopo));
    }

    // ----------------------------------------------------------------------
    // NOVO MÉTODO PARA SÍMBOLOS GERADOS PELO COMPILADOR
    // ----------------------------------------------------------------------

    /**
     * Insere um símbolo gerado pelo compilador (variável temporária T1, T2, etc.).
     * Temporários são geralmente considerados como pertencentes a um escopo global/função.
     * Estamos mantendo-os simples, sem escopo, para evitar a complexidade do Assembly.
     */
    public void inserirSimboloGerado(String lexema, TipoDado tipo) {
        // Para simplificar, temporários podem usar uma chave simples,
        // mas é mais seguro usar um escopo especial.
        String chave = gerarChave(lexema, "temp"); // Ex: T1@temp
        tabela.put(chave, new EntradaTabelaSimbolos(lexema, tipo, "temp"));
    }

    /**
     * Busca um identificador na tabela, **buscando do escopo atual para o global**.
     * Este é o coração da resolução de escopo.
     */
    public EntradaTabelaSimbolos buscar(String lexema) {
        // Percorre a pilha de escopos (do atual para o global)
        for (int i = escopoAtual.size() - 1; i >= 0; i--) {
            String escopo = escopoAtual.get(i);
            String chave = gerarChave(lexema, escopo);

            if (tabela.containsKey(chave)) {
                return tabela.get(chave);
            }
        }
        // Se não for encontrado em nenhum escopo aberto, tenta o escopo 'temp'
        String chaveTemp = gerarChave(lexema, "temp");
        if (tabela.containsKey(chaveTemp)) {
            return tabela.get(chaveTemp);
        }

        return null; // Não encontrado
    }

    /**
     * Exibe o conteúdo da Tabela de Símbolos.
     */
    public void imprimirTabela() {
        System.out.println("\n--- Tabela de Símbolos ---");
        System.out.println("Identificador     | Tipo        | Rótulo  | Escopo"); // CABEÇALHO ATUALIZADO
        System.out.println("------------------|-------------|---------|-----------");
        if (tabela.isEmpty()) {
            System.out.println("Tabela vazia (nenhuma variável declarada).");
        }
        for (EntradaTabelaSimbolos entrada : tabela.values()) {
            // A saída agora vem do método toString() da EntradaTabelaSimbolos atualizada.
            System.out.println(entrada);
        }
        System.out.println("---------------------------------------------");
    }
}