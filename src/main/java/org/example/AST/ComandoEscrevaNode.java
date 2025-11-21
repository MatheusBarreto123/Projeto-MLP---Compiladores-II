package org.example.AST;

/**
 * Nó de Comando: escreva expressao/identificador/literal.
 * Representa o comando de saída de dados (escrita).
 */
public class ComandoEscrevaNode extends ASTNode {
    private final ASTNode expressao; // Pode ser IdentificadorNode, LiteralNode, ExpressaoBinariaNode

    public ComandoEscrevaNode(ASTNode expressao, int linha, int coluna) {
        super(linha, coluna);
        this.expressao = expressao;
    }

    public ASTNode getExpressao() {
        return expressao;
    }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó 'escreva'.
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó ComandoEscrevaNode
        sb.append(prefix).append("└── ComandoEscrevaNode (escreva)\n");

        // Novo prefixo para o filho (Expressão)
        // A lógica do prefixo é adaptada para criar a linha vertical correta na AST.
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Chama recursivamente na Expressão
        // Note: Adicionamos mais espaços na frente do novoPrefix para garantir a indentação correta do filho.
        sb.append(expressao.imprimirArvore(novoPrefix + "    "));

        return sb.toString();
    }
}