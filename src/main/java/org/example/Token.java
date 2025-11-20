package org.example;

//REPRESENTA UMA UNIDADE LÉXICA (TOKEN) EXTRAIDO PELO ANALISADOR LÉXICO
public class Token {
    private final TipoToken tipo;
    private final String lexema;
    private final int linha;
    private final int coluna;

    //METODO CONSTRUTOR
    public Token(TipoToken tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    //GETTERS
    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

@Override
    public String toString() {
         return "(" + this.tipo.name() + ", " + this.lexema + ", " + this.linha + ", " + this.coluna + ")";
    }
}
