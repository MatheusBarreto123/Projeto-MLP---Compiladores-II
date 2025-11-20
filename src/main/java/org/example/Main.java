package org.example;

import java.util.List;

/**
 * Main.java
 * Classe principal para testar o Analisador Léxico e Sintático.
 */
// Main.java (Atualizado)

public class Main {
    public static void main(String[] args) {
        // Exemplo da MLP fornecido
        String codigoFonte =
                "$\n" +
                        "inteiro a, b;\n" +
                        "se (a > 10) entao b = b + 1;\n" +
                        "$.";

        System.out.println("--- 1. Análise Léxica da MLP ---");
        System.out.println("Código Fonte:\n" + codigoFonte);
        System.out.println("---------------------------------");

        AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte);
        List<Token> tokensLexico = lexico.analisar();

        System.out.println("\nTokens Encontrados:");
        for (Token token : tokensLexico) {
            System.out.println(token);
        }
        System.out.println("---------------------------------");

        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokensLexico);

        // 3. Execução da análise sintática e semântica de declaração
        sintatico.analisar();

        // 4. Exibição da Tabela de Símbolos, se a análise for bem-sucedida
        if (sintatico.analiseBemSucedida()) {
            sintatico.imprimirTabelaDeSimbolos();
        }
    }
}