package org.example;

import org.example.AST.*; // CondicionalNode, IterativoNode, etc.

import java.util.ArrayList;
import java.util.List;

/**
 * AnalisadorSintatico.java
 * Implementa a Análise Preditiva Recursiva e constrói a Árvore Sintática Abstrata (AST).
 */
public class AnalisadorSintatico {
    private final List<Token> tokens;
    private final AnalisadorSemantico analisadorSemantico;
    private int indiceAtual = 0;
    private boolean sucesso = false;
    private ProgramaNode astRaiz;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
        this.analisadorSemantico = new AnalisadorSemantico();
    }

    // --- Métodos de Controle ---
    public boolean analiseBemSucedida() {
        return sucesso;
    }

    public ProgramaNode getAstRaiz() {
        return astRaiz;
    }

    public void imprimirTabelaDeSimbolos() {
        analisadorSemantico.getTabelaSimbolos().imprimirTabela();
    }

    public AnalisadorSemantico getAnalisadorSemantico() {
        return analisadorSemantico;
    }

    public void analisar() {
        System.out.println("\n--- Iniciando Análise Sintática e Construção da AST ---");
        try {
            astRaiz = inicio();

            if (tokenAtual().getTipo() == TipoToken.EOF) {
                sucesso = true;
                System.out.println("Resultado: Análise Sintática concluída. A estrutura do programa é válida.");
            } else {
                throw new Exception("Erro Sintático: Tokens inesperados após o fim do programa.");
            }
        } catch (Exception e) {
            sucesso = false;
            System.err.println(e.getMessage());
            Token erroToken = tokenAtual();
            int linha = erroToken.getLinha() != -1 ? erroToken.getLinha() :
                    (indiceAtual > 0 ? tokens.get(indiceAtual - 1).getLinha() : -1);
            int coluna = erroToken.getColuna() != -1 ? erroToken.getColuna() :
                    (indiceAtual > 0 ? tokens.get(indiceAtual - 1).getColuna() : -1);
            System.err.println("Localização: Linha " + linha + ", Coluna " + coluna);
        }
    }

    // --- Métodos de Suporte ---
    private Token tokenAtual() {
        if (indiceAtual < tokens.size()) {
            return tokens.get(indiceAtual);
        }
        return new Token(TipoToken.EOF, "", -1, -1);
    }

    private void consumir(TipoToken tipoEsperado) throws Exception {
        Token atual = tokenAtual();
        if (atual.getTipo() == tipoEsperado) {
            indiceAtual++;
        } else {
            throw new Exception("Erro Sintático (Linha " + atual.getLinha() + ", Coluna " + atual.getColuna() +
                    "): Esperado " + tipoEsperado.getLexema() + " mas encontrado " + atual.getLexema());
        }
    }

    private TipoDado tipoTokenParaTipoDado(TipoToken tipoToken) {
        return switch (tipoToken) {
            case INTEIRO -> TipoDado.INTEIRO;
            case REAL -> TipoDado.REAL;
            case CARACTER -> TipoDado.CARACTER;
            default -> TipoDado.INDETERMINADO;
        };
    }

    // --- Regras de Produção ---

    // início ::= $ { tipo } { comando } $.
    private ProgramaNode inicio() throws Exception {
        Token startToken = tokenAtual();
        consumir(TipoToken.INICIO_PROGRAMA);

        // 1. Processa Tipos (Declarações)
        while (isTipoDeclaracao(tokenAtual().getTipo())) {
            tipo();
        }

        // 2. Processa Comandos
        List<ASTNode> listaComandos = new ArrayList<>();
        while (isComando(tokenAtual().getTipo())) {
            listaComandos.add(comando());
        }

        consumir(TipoToken.FIM_PROGRAMA);

        // 3. Retorna o nó raiz
        return new ProgramaNode(listaComandos, startToken.getLinha(), startToken.getColuna());
    }

    private boolean isTipoDeclaracao(TipoToken tipo) {
        return tipo == TipoToken.INTEIRO || tipo == TipoToken.REAL || tipo == TipoToken.CARACTER;
    }

    // tipo ::= (inteiro | real | caracter) identificador { , identificador } ;
    private void tipo() throws Exception {
        TipoToken tipoTokenDeclarado = tokenAtual().getTipo();
        TipoDado tipoDadoDeclarado = tipoTokenParaTipoDado(tipoTokenDeclarado);

        consumir(tipoTokenDeclarado);
        Token tokenID = tokenAtual();
        consumir(TipoToken.IDENTIFICADOR);
        analisadorSemantico.getTabelaSimbolos().inserir(
                tokenID.getLexema(), tipoDadoDeclarado, tokenID.getLinha(), tokenID.getColuna()
        );

        while (tokenAtual().getTipo() == TipoToken.VIRGULA) {
            consumir(TipoToken.VIRGULA);
            tokenID = tokenAtual();
            consumir(TipoToken.IDENTIFICADOR);
            analisadorSemantico.getTabelaSimbolos().inserir(
                    tokenID.getLexema(), tipoDadoDeclarado, tokenID.getLinha(), tokenID.getColuna()
            );
        }
        consumir(TipoToken.PONTO_VIRGULA);
    }

    private boolean isComando(TipoToken tipo) {
        return tipo == TipoToken.SE || tipo == TipoToken.ENQUANTO || tipo == TipoToken.IDENTIFICADOR;
    }

    // comando ::= condicional | iterativo | atribuição
    private ASTNode comando() throws Exception {
        TipoToken tipo = tokenAtual().getTipo();

        if (tipo == TipoToken.SE) {
            return condicional();
        } else if (tipo == TipoToken.ENQUANTO) {
            return iterativo();
        } else if (tipo == TipoToken.IDENTIFICADOR) {
            return atribuicao();
        } else {
            throw new Exception("Erro Sintático: Esperado um comando (SE, ENQUANTO, ou IDENTIFICADOR).");
        }
    }

    // condicional ::= se condição entao comando [ senao comando ]
    private ASTNode condicional() throws Exception {
        Token seToken = tokenAtual();
        consumir(TipoToken.SE);
        ASTNode condicaoNode = condicao();
        consumir(TipoToken.ENTAO);
        ASTNode comandoEntao = comando();

        ASTNode comandoSenao = null;
        if (tokenAtual().getTipo() == TipoToken.SENAO) {
            consumir(TipoToken.SENAO);
            comandoSenao = comando();
        }

        return new CondicionalNode(condicaoNode, comandoEntao, comandoSenao,
                seToken.getLinha(), seToken.getColuna());
    }

    // iterativo ::= enquanto condição comando
    private ASTNode iterativo() throws Exception {
        Token enquantoToken = tokenAtual();
        consumir(TipoToken.ENQUANTO);
        ASTNode condicaoNode = condicao();
        ASTNode comandoCorpo = comando();

        return new IterativoNode(condicaoNode, comandoCorpo,
                enquantoToken.getLinha(), enquantoToken.getColuna());
    }

    // atribuição ::= identificador = valor { operador valor } ;
    private ASTNode atribuicao() throws Exception {
        Token tokenLHS = tokenAtual();
        IdentificadorNode identificadorLHS = new IdentificadorNode(
                tokenLHS.getLexema(), tokenLHS.getLinha(), tokenLHS.getColuna()
        );
        consumir(TipoToken.IDENTIFICADOR);

        consumir(TipoToken.ATRIBUICAO);

        ASTNode expressaoRHS = valor();

        while (isOperadorAritmetico(tokenAtual().getTipo())) {
            Token operador = tokenAtual();
            consumir(operador.getTipo());
            ASTNode operando2 = valor();

            expressaoRHS = new ExpressaoBinariaNode(
                    expressaoRHS, operador, operando2,
                    operador.getLinha(), operador.getColuna()
            );
        }

        consumir(TipoToken.PONTO_VIRGULA);

        return new AtribuicaoNode(
                identificadorLHS, expressaoRHS,
                tokenLHS.getLinha(), tokenLHS.getColuna()
        );
    }

    // valor ::= identificador | número | expressão entre parênteses
    private ASTNode valor() throws Exception {
        if (tokenAtual().getTipo() == TipoToken.NUMERICO
                || tokenAtual().getTipo() == TipoToken.IDENTIFICADOR
                || tokenAtual().getTipo() == TipoToken.ABRE_PARENTESES) {
            return expressao();
        } else {
            throw new Exception("Erro Sintático: Esperado expressão, identificador ou número.");
        }
    }

    private boolean isOperadorAritmetico(TipoToken tipo) {
        return tipo == TipoToken.SOMA
                || tipo == TipoToken.MULTIPLICACAO
                || tipo == TipoToken.DIVISAO
                || tipo == TipoToken.RESTO;
    }

    // expressao ::= ( expressao ) | identificador | número | expressão binária aritmética
    private ASTNode expressao() throws Exception {
        if (tokenAtual().getTipo() == TipoToken.NUMERICO) {
            Token numToken = tokenAtual();
            consumir(TipoToken.NUMERICO);
            return new LiteralNode(numToken);
        } else if (tokenAtual().getTipo() == TipoToken.IDENTIFICADOR) {
            Token idToken = tokenAtual();
            consumir(TipoToken.IDENTIFICADOR);
            return new IdentificadorNode(idToken.getLexema(), idToken.getLinha(), idToken.getColuna());
        } else if (tokenAtual().getTipo() == TipoToken.ABRE_PARENTESES) {

            Token inicioToken = tokenAtual();
            consumir(TipoToken.ABRE_PARENTESES);

            ASTNode expressaoInterna = expressao();

            if (isOperadorAritmetico(tokenAtual().getTipo())) {
                Token operador = tokenAtual();
                consumir(operador.getTipo());
                ASTNode operando2 = expressao();
                expressaoInterna = new ExpressaoBinariaNode(
                        expressaoInterna, operador, operando2,
                        inicioToken.getLinha(), inicioToken.getColuna()
                );
            }

            consumir(TipoToken.FECHA_PARENTESES);
            return expressaoInterna;

        } else {
            Token t = tokenAtual();
            throw new Exception("Erro Sintático (Linha " + t.getLinha() + ", Coluna " + t.getColuna() +
                    "): Esperado número, identificador ou '(' para iniciar expressão.");
        }
    }

    // ================================
    //  CONDIÇÕES (NOT, E, OR)
    // ================================

    /**
     * condição ::= condicaoSimples { (E | OU) condicaoSimples }
     *
     * condicaoSimples ::= '(' condicaoInterna ')'
     *
     * Exemplos aceitos:
     *   se (x <= 5.0) entao ...
     *   se (x <= 5.0) OR (z > 10) entao ...
     *   se (NOT b < a) entao ...
     *   se (NOT (b < a)) entao ...
     */
    private ASTNode condicao() throws Exception {
        // Primeiro bloco entre parênteses
        ASTNode condicaoAtual = condicaoSimples();

        // Zero ou mais "E"/"OR" seguidos de outro bloco entre parênteses
        while (tokenAtual().getTipo() == TipoToken.E ||
                tokenAtual().getTipo() == TipoToken.OU) {

            Token opComposto = tokenAtual(); // E ou OR
            consumir(opComposto.getTipo());

            ASTNode proximaCondicao = condicaoSimples();

            condicaoAtual = new ExpressaoCompostaNode(
                    condicaoAtual, opComposto, proximaCondicao,
                    opComposto.getLinha(), opComposto.getColuna()
            );
        }

        return condicaoAtual;
    }

    // condicaoSimples ::= '(' condicaoInterna ')'
    private ASTNode condicaoSimples() throws Exception {
        consumir(TipoToken.ABRE_PARENTESES);
        ASTNode inner = condicaoInterna();
        consumir(TipoToken.FECHA_PARENTESES);
        return inner;
    }

    /**
     * condicaoInterna ::= [NAO] comparacao
     *                   | [NAO] '(' condicaoInterna ')'
     *
     * Isso permite:
     *   (b < a)
     *   (NOT b < a)
     *   (NOT (b < a))
     */
    private ASTNode condicaoInterna() throws Exception {
        boolean temNot = false;
        Token notToken = null;

        if (tokenAtual().getTipo() == TipoToken.NAO) {
            notToken = tokenAtual();
            consumir(TipoToken.NAO);
            temNot = true;
        }

        ASTNode base;

        if (tokenAtual().getTipo() == TipoToken.ABRE_PARENTESES) {
            // NOT ( ... ) ou apenas ( ... )
            consumir(TipoToken.ABRE_PARENTESES);
            base = condicaoInterna();
            consumir(TipoToken.FECHA_PARENTESES);
        } else {
            // NOT comparacao  ou  comparacao
            base = comparacao();
        }

        if (temNot) {
            return new NotNode(base,
                    notToken.getLinha(), notToken.getColuna());
        } else {
            return base;
        }
    }

    // comparacao ::= termo logico termo
    private ASTNode comparacao() throws Exception {
        ASTNode t1 = termo();
        Token operadorRelacional = tokenAtual();
        logico();
        ASTNode t2 = termo();

        return new CondicaoBinariaNode(
                t1, operadorRelacional, t2,
                operadorRelacional.getLinha(), operadorRelacional.getColuna()
        );
    }

    // logico ::= > | < | <= | >= | == | !=
    private void logico() throws Exception {
        TipoToken tipo = tokenAtual().getTipo();
        if (tipo == TipoToken.MAIOR_QUE || tipo == TipoToken.MENOR_QUE ||
                tipo == TipoToken.MAIOR_IGUAL || tipo == TipoToken.MENOR_IGUAL ||
                tipo == TipoToken.IGUAL || tipo == TipoToken.DIFERENTE) {
            consumir(tipo);
        } else {
            Token t = tokenAtual();
            throw new Exception("Erro Sintático (Linha " + t.getLinha() + ", Coluna " + t.getColuna() +
                    "): Esperado um operador lógico (>, <, <=, >=, ==, !=) mas encontrado " + t.getLexema());
        }
    }

    // termo ::= identificador | numero
    private ASTNode termo() throws Exception {
        if (tokenAtual().getTipo() == TipoToken.IDENTIFICADOR) {
            Token t = tokenAtual();
            consumir(TipoToken.IDENTIFICADOR);
            return new IdentificadorNode(t.getLexema(), t.getLinha(), t.getColuna());
        } else if (tokenAtual().getTipo() == TipoToken.NUMERICO) {
            Token t = tokenAtual();
            consumir(TipoToken.NUMERICO);
            return new LiteralNode(t);
        } else {
            Token t = tokenAtual();
            throw new Exception("Erro Sintático (Linha " + t.getLinha() + ", Coluna " + t.getColuna() +
                    "): Esperado identificador ou número como termo da condição.");
        }
    }
}
