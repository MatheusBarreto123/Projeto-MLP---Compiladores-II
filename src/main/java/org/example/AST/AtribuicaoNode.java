package org.example.AST;

/** * Nó de Atribuição: LHS = RHS.
 * Representa a operação de atribuição de um valor ou resultado de expressão a um identificador.
 */
public class AtribuicaoNode extends ASTNode {
    private final IdentificadorNode identificador; // Lado Esquerdo (LHS)
    private final ASTNode expressaoRHS; // Lado Direito (RHS)

    public AtribuicaoNode(IdentificadorNode identificador, ASTNode expressaoRHS, int linha, int coluna) {
        super(linha, coluna);
        this.identificador = identificador;
        this.expressaoRHS = expressaoRHS;
    }

    public IdentificadorNode getIdentificador() { return identificador; }
    public ASTNode getExpressaoRHS() { return expressaoRHS; }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /** Te amoooooooo vida
     * Implementa a visualização da AST para o nó de Atribuição.
     * Imprime o nó e chama recursivamente para o identificador (LHS) e a expressão (RHS).
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó AtribuicaoNode
        sb.append(prefix).append("└── AtribuicaoNode (=)\n");

        // Novo prefixo para os filhos
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Imprime o Identificador (LHS)
        sb.append(novoPrefix).append("└── LHS (Identificador):\n");
        sb.append(identificador.imprimirArvore(novoPrefix + "    "));

        // 3. Imprime a Expressão (RHS)
        sb.append(novoPrefix).append("└── RHS (Expressão):\n");
        sb.append(expressaoRHS.imprimirArvore(novoPrefix + "    "));

        return sb.toString();
    }
}