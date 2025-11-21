package org.example.AST;

import org.example.TipoDado;
import org.example.Token;

/**
 * Nó de Literal (Número).
 * Representa um valor constante (número inteiro ou real) no código.
 */
public class LiteralNode extends ASTNode {
    private final Token token; // O token NUMERICO

    public LiteralNode(Token token) {
        super(token.getLinha(), token.getColuna());
        this.token = token;
    }

    public String getValor() { return token.getLexema(); }

    // Infere o TipoDado do literal (assumindo que literais de ponto flutuante contêm '.')
    public TipoDado getTipoInferido() {
        return token.getLexema().contains(".") ? TipoDado.REAL : TipoDado.INTEIRO;
    }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó Literal.
     * Como é um nó folha, ele apenas imprime seu valor e tipo inferido.
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando o nó.
     */
    @Override
    public String imprimirArvore(String prefix) {
        String valor = getValor();
        TipoDado tipo = getTipoInferido();
        String tipoInfo = (tipo != null) ? " (Tipo: " + tipo + ")" : "";

        return prefix + "└── LiteralNode (Valor: " + valor + ")" + tipoInfo + "\n";
    }
}