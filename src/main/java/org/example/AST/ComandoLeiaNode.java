package org.example.AST;

import org.example.AST.ASTResult;
import org.example.AST.ASTVisitor;

/** Nó de Comando: leia identificador */
public class ComandoLeiaNode extends ASTNode {
    private final IdentificadorNode identificador;

    public ComandoLeiaNode(IdentificadorNode identificador, int linha, int coluna) {
        super(linha, coluna);
        this.identificador = identificador;
    }

    public IdentificadorNode getIdentificador() {
        return identificador;
    }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó 'leia'.
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó ComandoLeiaNode
        sb.append(prefix).append("└── ComandoLeiaNode (leia)\n");

        // Novo prefixo para o filho (Identificador)
        String novoPrefix = prefix + (prefix.contains("└──") ? "    " : "│   ");

        // 2. Chama recursivamente no Identificador
        sb.append(identificador.imprimirArvore(novoPrefix + "    "));

        return sb.toString();
    }
}
