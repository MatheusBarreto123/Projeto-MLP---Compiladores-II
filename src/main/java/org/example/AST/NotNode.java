package org.example.AST;

/**
 * Nó unário para o operador lógico NOT na AST.
 */
public class NotNode extends ASTNode {

    private final ASTNode condicao;

    public NotNode(ASTNode condicao, int linha, int coluna) {
        super(linha, coluna);
        this.condicao = condicao;
    }

    public ASTNode getCondicao() {
        return condicao;
    }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // Linha do próprio nó NOT
        sb.append(prefix)
                .append("NOT (linha ")
                .append(linha)
                .append(", coluna ")
                .append(coluna)
                .append(")\n");

        // Imprime a subárvore da condição com indentação extra
        if (condicao != null) {
            sb.append(condicao.imprimirArvore(prefix + "  "));
        }

        return sb.toString();
    }
}
