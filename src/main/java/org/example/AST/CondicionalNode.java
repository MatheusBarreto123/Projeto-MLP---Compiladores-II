package org.example.AST;

/**
 * Nó de Condicional: se condição entao comando [ senao comando ].
 * Representa a estrutura de controle de fluxo condicional.
 */
public class CondicionalNode extends ASTNode {
    private final ASTNode condicao;
    private final ASTNode comandoEntao;
    private final ASTNode comandoSenao; // Pode ser null

    public CondicionalNode(ASTNode condicao, ASTNode comandoEntao, ASTNode comandoSenao, int linha, int coluna) {
        super(linha, coluna);
        this.condicao = condicao;
        this.comandoEntao = comandoEntao;
        this.comandoSenao = comandoSenao;
    }

    public ASTNode getCondicao() { return condicao; }
    public ASTNode getComandoEntao() { return comandoEntao; }
    public ASTNode getComandoSenao() { return comandoSenao; }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó Condicional.
     * Imprime o nó e chama recursivamente para a condição, bloco 'entao' e, se existir, bloco 'senao'.
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó CondicionalNode
        sb.append(prefix).append("└── CondicionalNode (SE/SENAO)\n");

        // Novo prefixo para os filhos
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Imprime a Condição
        sb.append(novoPrefix).append("└── Condição:\n");
        sb.append(condicao.imprimirArvore(novoPrefix + "    "));

        // 3. Imprime o Bloco ENTÃO
        sb.append(novoPrefix).append("└── Comando ENTÃO:\n");
        sb.append(comandoEntao.imprimirArvore(novoPrefix + "    "));

        // 4. Imprime o Bloco SENÃO (se não for nulo)
        if (comandoSenao != null) {
            sb.append(novoPrefix).append("└── Comando SENÃO:\n");
            sb.append(comandoSenao.imprimirArvore(novoPrefix + "    "));
        }

        return sb.toString();
    }
}