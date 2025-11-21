package org.example;

import org.example.AST.ASTSemanticAnalyzer;
import org.example.AST.ProgramaNode;
import org.example.Assembly.AssemblyGenerator;
import org.example.C3E.C3EGeneratorVisitor;
import org.example.C3E.GeradorC3E;
import org.example.C3E.InstrucaoC3E;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Main.java
 * Classe principal que orquestra todas as fases do compilador (Léxica, Sintática,
 * Semântica, Geração C3E e Geração Assembly).
 */
public class Main {

    private static final String NOME_ARQUIVO_ASSEMBLY = "programa.asm";
    // O arquivo deve estar na pasta 'src/main/resources' ou na raiz do classpath.
    private static final String NOME_ARQUIVO_FONTE = "teste8_validaçãoEscopo.txt";

    public static void main(String[] args) {

        String codigoFonte;

        try {
            // =========================================================
            // 1. CARREGAR CÓDIGO FONTE DO ARQUIVO
            // =========================================================
            System.out.println("--- Compilando Arquivo: " + NOME_ARQUIVO_FONTE + " ---");
            codigoFonte = lerArquivo(NOME_ARQUIVO_FONTE);

            // =========================================================
            // FASE 1: ANÁLISE LÉXICA
            // =========================================================
            System.out.println("\n--- 1. Análise Léxica ---");
            System.out.println("Passo: O Analisador Léxico (Scanner) transforma o código-fonte em uma sequência de Tokens.");
            AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte);
            List<Token> tokens = lexico.analisar();
            System.out.println("Resultado: Análise Léxica concluída. Total de tokens: " + tokens.size());

            // Exibir a lista de tokens em formato de tabela
            System.out.println("\nTokens Identificados:");
            System.out.println(String.format("  | %-20s | %-15s | Linha | Coluna |", "TIPO DO TOKEN", "LEXEMA"));
            System.out.println("  |----------------------|-----------------|-------|--------|");
            for (Token token : tokens) {
                // Assumimos que Token possui os métodos getTipo(), getLexema(), getLinha(), getColuna()
                System.out.println(String.format("  | %-20s | %-15s | %-5d | %-6d |",
                        token.getTipo(),
                        token.getLexema().replace("\n", "\\n").replace("\r", "\\r"), // Trata quebras de linha
                        token.getLinha(),
                        token.getColuna()));
            }
            System.out.println("----------------------------------------------------------");


            // =========================================================
            // FASE 2: ANÁLISE SINTÁTICA E CONSTRUÇÃO DA AST
            // =========================================================
            System.out.println("\n--- 2. Análise Sintática / Construção da AST ---");
            System.out.println("Passo: O Analisador Sintático (Parser) verifica a gramática e constrói a Árvore Sintática Abstrata (AST).");

            AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
            sintatico.analisar(); // Popula a AST e a Tabela de Símbolos com as declarações

            if (sintatico.analiseBemSucedida()) {

                System.out.println("Resultado: Análise Sintática concluída. A estrutura do programa é válida.");

                ProgramaNode astRaiz = sintatico.getAstRaiz();

                // --- IMPRIMINDO A TABELA DE SÍMBOLOS ---
                TabelaSimbolos ts = sintatico.getAnalisadorSemantico().getTabelaSimbolos();
                ts.imprimirTabela();
                // ---------------------------------------

                // =========================================================
                // CORREÇÃO AQUI: CHAMADA REAL DO MÉTODO imprimirArvore()
                // =========================================================
                System.out.println("\n--- Estrutura da AST (Raiz: ProgramaNode) ---");
                // Chama o método imprimirArvore a partir do nó raiz
                if (astRaiz != null) {
                    System.out.println(astRaiz.imprimirArvore(""));
                } else {
                    System.out.println("Erro: O nó raiz da AST é nulo.");
                }
                System.out.println("---------------------------------------------");


                // =========================================================
                // FASE 3: ANÁLISE SEMÂNTICA (Checagem de Tipos)
                // =========================================================
                System.out.println("\n--- 3. Análise Semântica (Checagem de Tipos) ---");
                System.out.println("Passo: O Analisador Semântico (Visitor na AST) verifica o uso correto de variáveis e compatibilidade de tipos.");
                ASTSemanticAnalyzer semantico = new ASTSemanticAnalyzer(sintatico.getAnalisadorSemantico());
                astRaiz.accept(semantico);
                System.out.println("Resultado: Análise Semântica concluída sem erros de tipo.");

                // =========================================================
                // FASE 4: GERAÇÃO DE CÓDIGO INTERMEDIÁRIO (C3E)
                // =========================================================
                System.out.println("\n--- 4. Geração de Código de 3 Endereços (C3E) ---");
                System.out.println("Passo: O Gerador C3E percorre a AST e traduz comandos complexos em uma sequência de operações simples (instruções de 3 endereços).");
                GeradorC3E geradorC3E = new GeradorC3E();
                C3EGeneratorVisitor c3eVisitor = new C3EGeneratorVisitor(geradorC3E, sintatico.getAnalisadorSemantico());
                astRaiz.accept(c3eVisitor);

                List<InstrucaoC3E> codigoC3E = geradorC3E.getCodigo();
                System.out.println("Código C3E gerado (" + codigoC3E.size() + " instruções):");
                int linha = 0;
                for (InstrucaoC3E instrucao : codigoC3E) {
                    System.out.println(String.format("  [%02d] %s", linha++, instrucao));
                }
                System.out.println("--------------------------");


                // =========================================================
                // FASE 5: GERAÇÃO DE CÓDIGO FINAL (ASSEMBLY)
                // =========================================================
                System.out.println("\n--- 5. Geração de Código Assembly ---");
                System.out.println("Passo: O Gerador Assembly traduz o C3E para instruções de máquina (Assembly x86), usando a Tabela de Símbolos para alocação de memória.");
                AssemblyGenerator assemblyGen = new AssemblyGenerator(codigoC3E, ts);
                assemblyGen.gerarCodigo();
                List<String> codigoAssembly = assemblyGen.getCodigoAssembly();

                // ---------------------------------------------------------
                // 6. IMPRIMIR O CÓDIGO ASSEMBLY NO CONSOLE (Adição/Ajuste)
                // ---------------------------------------------------------
                System.out.println("\n--- Código Assembly Gerado (" + codigoAssembly.size() + " linhas) ---");
                for (String linha2 : codigoAssembly) {
                    // Imprime cada linha do Assembly
                    System.out.println("  " + linha2);
                }
                System.out.println("---------------------------------------------");
                // ---------------------------------------------------------

                // 7. SALVAR O CÓDIGO ASSEMBLY
                salvarArquivoAssembly(codigoAssembly);

                System.out.println("\nCompilação concluída com sucesso!");
                System.out.println("Código Assembly salvo em: " + NOME_ARQUIVO_ASSEMBLY);

            } else {
                System.err.println("\nCompilação falhou devido a erros sintáticos.");
            }

        } catch (IOException e) {
            System.err.println("\nERRO DE ARQUIVO: Não foi possível ler o arquivo '" + NOME_ARQUIVO_FONTE + "'.");
            System.err.println("Verifique se o arquivo está na pasta 'resources' ou na raiz do classpath do projeto.");
            System.err.println("Detalhe: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\nERRO DURANTE A COMPILAÇÃO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Função auxiliar para ler o conteúdo de um arquivo de texto
     * usando o ClassLoader, tornando o acesso ao arquivo independente do diretório de execução.
     */
    private static String lerArquivo(String nomeArquivo) throws IOException {
        StringBuilder conteudo = new StringBuilder();

        // Obtém o recurso como InputStream
        try (java.io.InputStream is = Main.class.getClassLoader().getResourceAsStream(nomeArquivo);
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {

            if (is == null) {
                // Lança IOException para ser capturado no bloco try-catch principal
                throw new IOException("Arquivo não encontrado no classpath.");
            }

            String linha;
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
        }
        return conteudo.toString();
    }

    /**
     * Salva o código Assembly gerado em um arquivo .asm.
     */
    private static void salvarArquivoAssembly(List<String> codigoAssembly) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOME_ARQUIVO_ASSEMBLY))) {
            for (String linha : codigoAssembly) {
                writer.println(linha);
            }
        }
    }
}