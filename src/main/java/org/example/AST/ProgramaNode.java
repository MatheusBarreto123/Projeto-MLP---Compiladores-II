package org.example.AST;

import org.example.TipoDado;
import org.example.Token;

import java.util.List;
import java.util.stream.Collectors;


/** Nó Raiz: Representa o programa completo */
public class ProgramaNode extends ASTNode {
    private final List<ASTNode> comandos;

    public ProgramaNode(List<ASTNode> comandos, int linha, int coluna) {
        super(linha, coluna);
        this.comandos = comandos;
    }

    public List<ASTNode> getComandos() { return comandos; }

    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da Árvore Sintática Abstrata (AST) para o nó raiz do programa.
     * Esta implementação utiliza indentação simples (espaços) para demonstrar a hierarquia.
     * * @param prefix Prefixo de indentação (geralmente vazio na chamada inicial).
     * @return Uma String formatada representando a subárvore.
     */
    @Override
    public String imprimirArvore(String prefix) {
        StringBuilder sb = new StringBuilder();

        // 1. Imprime o próprio nó (Raiz)
        sb.append(prefix).append("ProgramaNode (Raiz)\n");

        // Novo prefixo para os comandos filhos. Aumenta a indentação.
        String novoPrefix = prefix + "    ";

        // Itera sobre a lista de comandos (filhos diretos)
        for (ASTNode comando : comandos) {
            // Concatena a representação do nó filho (recursão).
            // É responsabilidade do nó filho adicionar seu próprio nome/detalhes
            // e passar o novoPrefix para seus próprios filhos.
            sb.append(comando.imprimirArvore(novoPrefix));
        }

        return sb.toString();
    }
}








