package org.example.AST;

/**
 * Nó de Comando Iterativo: enquanto condição comando.
 * Representa a estrutura de controle de fluxo de repetição (loop WHILE).
 */
public class IterativoNode extends ASTNode {
    private final ASTNode condicao;
    private final ASTNode comandoCorpo; // O comando ou bloco de comandos a ser executado repetidamente

    public IterativoNode(ASTNode condicao, ASTNode comandoCorpo, int linha, int coluna) {
        super(linha, coluna);
        this.condicao = condicao;
        this.comandoCorpo = comandoCorpo;
    }

    public ASTNode getCondicao() { return condicao; }
    public ASTNode getComandoCorpo() { return comandoCorpo; }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó Iterativo.
     * Imprime o nó e chama recursivamente para a condição e o corpo do comando.
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó IterativoNode
        sb.append(prefix).append("└── IterativoNode (ENQUANTO)\n");

        // Novo prefixo para os filhos
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Imprime a Condição
        sb.append(novoPrefix).append("└── Condição do Loop:\n");
        sb.append(condicao.imprimirArvore(novoPrefix + "    "));

        // 3. Imprime o Corpo do Comando
        sb.append(novoPrefix).append("└── Corpo do Comando:\n");
        sb.append(comandoCorpo.imprimirArvore(novoPrefix + "    "));

        return sb.toString();
    }
}