package org.example;

/**
 * TipoToken.java
 * Enumeração que lista todas as categorias de tokens
 * reconhecidas pela Mini-Linguagem Portugol (MLP).
 */
public enum TipoToken {
    // Palavras Reservadas da Estrutura
    INICIO_PROGRAMA ("$"),
    FIM_PROGRAMA ("$."),

    // Palavras Reservadas de Tipos
    INTEIRO ("inteiro"),
    REAL ("real"),
    CARACTER ("caracter"),

    // Palavras Reservadas de Controle
    SE ("se"),
    ENTAO ("entao"),
    SENAO ("senao"),
    ENQUANTO ("enquanto"),
    RESTO ("RESTO"),

    // Palavras Reservadas Lógicas
    E ("E"),
    OU ("OR"),
    NAO ("NOT"),

    // Símbolos Especiais e Operadores
    ATRIBUICAO ("="),
    SOMA ("+"),
    MULTIPLICACAO ("*"),
    DIVISAO ("/"),

    // Relações (Lógico/Relacional)
    MAIOR_QUE (">"),
    MENOR_QUE ("<"),
    IGUAL ("=="),
    DIFERENTE ("!="),
    MAIOR_IGUAL (">="),
    MENOR_IGUAL ("<="),

    // Delimitadores
    ABRE_PARENTESES ("("),
    FECHA_PARENTESES (")"),
    VIRGULA (","),
    PONTO_VIRGULA (";"),

    // Literais e Identificadores (Reconhecidos por ER)
    IDENTIFICADOR ("Identificador"),
    NUMERICO ("Número"),

    // Finalização e Erro
    EOF ("Fim de Arquivo"),
    ERRO ("Erro Léxico");

    // Campo para armazenar o lexema (útil para palavras-chave)
    private final String lexema;

    TipoToken(String lexema) {
        this.lexema = lexema;
    }

    public String getLexema() {
        return lexema;
    }
}
