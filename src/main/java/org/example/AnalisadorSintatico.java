package org.example;

import java.util.List;

/**
 * AnalisadorSintatico.java
 * Implementa a Análise Preditiva Recursiva e delega a Análise Semântica
 * para AnalisadorSemantico.java.
 */
public class AnalisadorSintatico {
    private final List<Token> tokens;
    // O Analisador Sintático agora usa o Analisador Semântico
    private final AnalisadorSemantico analisadorSemantico;
    private int indiceAtual = 0;
    private boolean sucesso = false;

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
        this.analisadorSemantico = new AnalisadorSemantico(); // Inicializa o Analisador Semântico
    }

    // --- Métodos de Controle ---

    public boolean analiseBemSucedida() {
        return sucesso;
    }

    // Delega a impressão da Tabela de Símbolos para a classe semântica
    public void imprimirTabelaDeSimbolos() {
        analisadorSemantico.getTabelaSimbolos().imprimirTabela();
    }

    /**
     * Ponto de entrada do Analisador.
     */
    public void analisar() {
        System.out.println("\n--- Iniciando Análise Sintática e Semântica ---");
        try {
            inicio();

            if (tokenAtual().getTipo() == TipoToken.EOF) {
                System.out.println("Análise Sintática e Semântica concluída com sucesso!");
                sucesso = true;
            } else {
                throw new Exception("Erro Sintático: Tokens inesperados após o fim do programa.");
            }
        } catch (Exception e) {
            sucesso = false;
            System.err.println(e.getMessage());
            Token erroToken = tokenAtual();
            System.err.println("Localização: Linha " + erroToken.getLinha() + ", Coluna " + erroToken.getColuna());
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
            System.out.println("Consumido: " + atual.getLexema());
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
    private void inicio() throws Exception {
        consumir(TipoToken.INICIO_PROGRAMA);

        while (isTipoDeclaracao(tokenAtual().getTipo())) {
            tipo();
        }

        while (isComando(tokenAtual().getTipo())) {
            comando();
        }

        consumir(TipoToken.FIM_PROGRAMA);
    }

    private boolean isTipoDeclaracao(TipoToken tipo) {
        return tipo == TipoToken.INTEIRO || tipo == TipoToken.REAL || tipo == TipoToken.CARACTER;
    }

    // tipo ::= (inteiro | real | caracter) identificador { , identificador } ;
    private void tipo() throws Exception {
        TipoToken tipoTokenDeclarado = tokenAtual().getTipo();
        TipoDado tipoDadoDeclarado = tipoTokenParaTipoDado(tipoTokenDeclarado);

        // 1. Consome o tipo
        consumir(tipoTokenDeclarado);

        // 2. Consome e insere o primeiro identificador
        Token tokenID = tokenAtual();
        consumir(TipoToken.IDENTIFICADOR);
        // SEMÂNTICA: Inserir (delegado a TabelaSimbolos)
        analisadorSemantico.getTabelaSimbolos().inserir(
                tokenID.getLexema(), tipoDadoDeclarado, tokenID.getLinha(), tokenID.getColuna()
        );

        // 3. { , identificador } - Repetição
        while (tokenAtual().getTipo() == TipoToken.VIRGULA) {
            consumir(TipoToken.VIRGULA);

            tokenID = tokenAtual();
            consumir(TipoToken.IDENTIFICADOR);
            // SEMÂNTICA: Inserir (delegado a TabelaSimbolos)
            analisadorSemantico.getTabelaSimbolos().inserir(
                    tokenID.getLexema(), tipoDadoDeclarado, tokenID.getLinha(), tokenID.getColuna()
            );
        }

        // 4. Consome o delimitador final
        consumir(TipoToken.PONTO_VIRGULA);
    }

    private boolean isComando(TipoToken tipo) {
        return tipo == TipoToken.SE ||
                tipo == TipoToken.ENQUANTO ||
                tipo == TipoToken.IDENTIFICADOR;
    }

    private void comando() throws Exception {
        TipoToken tipo = tokenAtual().getTipo();

        if (tipo == TipoToken.SE) {
            condicional();
        } else if (tipo == TipoToken.ENQUANTO) {
            iterativo();
        } else if (tipo == TipoToken.IDENTIFICADOR) {
            atribuicao();
        } else {
            throw new Exception("Erro Sintático: Esperado um comando (se, enquanto, identificador).");
        }
    }

    // condicional ::= se condição entao comando [ senao comando ]
    private void condicional() throws Exception {
        consumir(TipoToken.SE);
        condicao();
        consumir(TipoToken.ENTAO);
        comando();

        if (tokenAtual().getTipo() == TipoToken.SENAO) {
            consumir(TipoToken.SENAO);
            comando();
        }
    }

    // iterativo ::= enquanto condição comando
    private void iterativo() throws Exception {
        consumir(TipoToken.ENQUANTO);
        condicao();
        comando();
    }

    // atribuição ::= identificador = valor { operador valor } ;
    private void atribuicao() throws Exception {
        // 1. LHS (L-value)
        Token tokenLHS = tokenAtual();
        // SEMÂNTICA: Checa declaração e obtém o tipo LHS
        TipoDado tipoLHS = analisadorSemantico.checarDeclaracao(
                tokenLHS.getLexema(), tokenLHS.getLinha(), tokenLHS.getColuna()
        );
        consumir(TipoToken.IDENTIFICADOR);

        consumir(TipoToken.ATRIBUICAO);

        // 2. RHS (R-value) - Obtém o tipo do resultado inicial
        TipoDado tipoRHS = valor();

        // { operador valor } - Agrega as operações à direita para determinar o tipo final
        while (isOperadorAritmetico(tokenAtual().getTipo())) {
            operador();
            TipoDado tipoOperando = valor();

            // SEMÂNTICA: Delega a determinação do tipo final da subexpressão
            tipoRHS = analisadorSemantico.determinarTipoExpressao(
                    tipoRHS, tipoOperando, tokenAtual().getLinha(), tokenAtual().getColuna()
            );
        }

        // 3. SEMÂNTICA: Checagem de compatibilidade de atribuição final
        analisadorSemantico.checarAtribuicao(
                tipoLHS, tipoRHS, tokenLHS.getLinha(), tokenLHS.getColuna()
        );

        consumir(TipoToken.PONTO_VIRGULA);
    }

    // valor ::= expressão | identificador
    private TipoDado valor() throws Exception { // Retorna TipoDado
        if (tokenAtual().getTipo() == TipoToken.ABRE_PARENTESES || tokenAtual().getTipo() == TipoToken.NUMERICO) {
            return expressao();
        } else if (tokenAtual().getTipo() == TipoToken.IDENTIFICADOR) {
            Token idToken = tokenAtual();
            // SEMÂNTICA: Checa declaração e obtém o tipo
            TipoDado tipoID = analisadorSemantico.checarDeclaracao(
                    idToken.getLexema(), idToken.getLinha(), idToken.getColuna()
            );
            consumir(TipoToken.IDENTIFICADOR);
            return tipoID;
        } else {
            throw new Exception("Erro Sintático: Esperado expressão ou identificador.");
        }
    }

    // operador ::= + | * | / | RESTO
    private boolean isOperadorAritmetico(TipoToken tipo) {
        return tipo == TipoToken.SOMA ||
                tipo == TipoToken.MULTIPLICACAO ||
                tipo == TipoToken.DIVISAO ||
                tipo == TipoToken.RESTO;
    }

    private void operador() throws Exception {
        TipoToken tipo = tokenAtual().getTipo();
        if (isOperadorAritmetico(tipo)) {
            consumir(tipo);
        } else {
            throw new Exception("Erro Sintático: Esperado um operador aritmético (+, *, /, RESTO).");
        }
    }

    // expressao ::= número | ( expressão operador expressão )
    private TipoDado expressao() throws Exception { // Retorna TipoDado (Tipo resultante)
        if (tokenAtual().getTipo() == TipoToken.NUMERICO) {
            String lexema = tokenAtual().getLexema();
            consumir(TipoToken.NUMERICO);
            // Inferência de Tipo (local, semântica trivial)
            return lexema.contains(".") ? TipoDado.REAL : TipoDado.INTEIRO;

        } else if (tokenAtual().getTipo() == TipoToken.ABRE_PARENTESES) {
            consumir(TipoToken.ABRE_PARENTESES);

            TipoDado tipoE1 = expressao();
            operador();
            TipoDado tipoE2 = expressao();
            consumir(TipoToken.FECHA_PARENTESES);

            // SEMÂNTICA: Delega a determinação do tipo resultante
            return analisadorSemantico.determinarTipoExpressao(
                    tipoE1, tipoE2, tokenAtual().getLinha(), tokenAtual().getColuna()
            );

        } else {
            throw new Exception("Erro Sintático: Esperado número ou '(' para iniciar expressão.");
        }
    }

    // --- Métodos de Condição (Usam apenas checagem de declaração) ---

    private void condicao() throws Exception {
        consumir(TipoToken.ABRE_PARENTESES);

        // SEMÂNTICA: Checa declaração
        analisadorSemantico.checarDeclaracao(tokenAtual().getLexema(), tokenAtual().getLinha(), tokenAtual().getColuna());
        consumir(TipoToken.IDENTIFICADOR);

        if (tokenAtual().getTipo() == TipoToken.NAO) {
            consumir(TipoToken.NAO);
            consumir(TipoToken.ABRE_PARENTESES);
            condicao();
            consumir(TipoToken.FECHA_PARENTESES);
            logica_opcional();
        } else {
            logico();
            termo();
            consumir(TipoToken.FECHA_PARENTESES);
            logica_opcional();
        }
    }

    private void logica_opcional() throws Exception {
        while (tokenAtual().getTipo() == TipoToken.E || tokenAtual().getTipo() == TipoToken.OU) {
            if (tokenAtual().getTipo() == TipoToken.E) {
                consumir(TipoToken.E);
            } else {
                consumir(TipoToken.OU);
            }

            consumir(TipoToken.ABRE_PARENTESES);

            // SEMÂNTICA: Checa declaração
            analisadorSemantico.checarDeclaracao(tokenAtual().getLexema(), tokenAtual().getLinha(), tokenAtual().getColuna());
            consumir(TipoToken.IDENTIFICADOR);

            logico();
            termo();
            consumir(TipoToken.FECHA_PARENTESES);
        }
    }

    // logico ::= > | < | <= | >= | == | !=
    private void logico() throws Exception {
        TipoToken tipo = tokenAtual().getTipo();
        if (tipo == TipoToken.MAIOR_QUE || tipo == TipoToken.MENOR_QUE ||
                tipo == TipoToken.MAIOR_IGUAL || tipo == TipoToken.MENOR_IGUAL ||
                tipo == TipoToken.IGUAL || tipo == TipoToken.DIFERENTE) {

            consumir(tipo);
        } else {
            throw new Exception("Erro Sintático: Esperado operador lógico/relacional.");
        }
    }

    // termo ::= identificador | número
    private void termo() throws Exception {
        if (tokenAtual().getTipo() == TipoToken.IDENTIFICADOR) {
            // SEMÂNTICA: Checa declaração
            analisadorSemantico.checarDeclaracao(tokenAtual().getLexema(), tokenAtual().getLinha(), tokenAtual().getColuna());
            consumir(TipoToken.IDENTIFICADOR);
        } else if (tokenAtual().getTipo() == TipoToken.NUMERICO) {
            consumir(TipoToken.NUMERICO);
        } else {
            throw new Exception("Erro Sintático: Esperado identificador ou número como termo da condição.");
        }
    }
}