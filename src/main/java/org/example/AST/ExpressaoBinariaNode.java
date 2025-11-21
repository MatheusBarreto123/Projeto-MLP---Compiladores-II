package org.example.AST;

import org.example.Token;

/**
 * Nó de Expressão Binária: E1 OP E2.
 * Representa operações aritméticas ou lógicas/relacionais (como na condição)
 * que possuem dois operandos e um operador.
 */
public class ExpressaoBinariaNode extends ASTNode {
    private final ASTNode operando1;
    private final Token operador;
    private final ASTNode operando2;

    public ExpressaoBinariaNode(ASTNode operando1, Token operador, ASTNode operando2, int linha, int coluna) {
        super(linha, coluna);
        this.operando1 = operando1;
        this.operador = operador;
        this.operando2 = operando2;
    }

    public ASTNode getOperando1() { return operando1; }
    public Token getOperador() { return operador; }
    public ASTNode getOperando2() { return operando2; }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó de Expressão Binária.
     * Imprime o nó e chama recursivamente para os dois operandos filhos.
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó, destacando o operador
        sb.append(prefix).append("└── ExpressaoBinariaNode (Op: ").append(operador.getLexema()).append(")\n");

        // Novo prefixo para os filhos
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Imprime o Operando 1 (Esquerda)
        sb.append(novoPrefix).append("└── Operando 1 (Esquerda):\n");
        // Chama recursivamente no Operando 1, ajustando a indentação
        sb.append(operando1.imprimirArvore(novoPrefix + "    "));

        // 3. Imprime o Operando 2 (Direita)
        sb.append(novoPrefix).append("└── Operando 2 (Direita):\n");
        // Chama recursivamente no Operando 2, ajustando a indentação
        sb.append(operando2.imprimirArvore(novoPrefix + "    "));

        return sb.toString();
    }
}