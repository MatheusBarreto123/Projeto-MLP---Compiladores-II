package org.example.AST;

/**
 * Classe base abstrata para todos os nós da Árvore Sintática Abstrata (AST).
 * Contém informações comuns a todos os nós, como localização no código fonte
 * (linha e coluna) e define os métodos abstratos para o Padrão Visitor e
 * para a visualização da árvore.
 */
public abstract class ASTNode {

    public int linha;
    public int coluna;

    public ASTNode(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;

    }

    /**
     * Retorna o número da linha onde o nó foi declarado no código fonte.
     */
    public int getLinha() {
        return linha;
    }

    /**
     * Retorna o número da coluna onde o nó foi declarado no código fonte.
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * Método abstrato para o Padrão Visitor.
     * Deve ser implementado por todos os nós para aceitar um visitor,
     * permitindo a análise semântica, geração de código, etc.
     * * @param visitor O objeto visitor que percorrerá o nó.
     * @return O resultado da visita (dependente da implementação do visitor).
     * @throws Exception Em caso de erro durante a visita.
     */
    public abstract ASTResult accept(ASTVisitor visitor) throws Exception;

    /**
     * Método abstrato para imprimir a estrutura da Árvore Sintática Abstrata (AST).
     * Cada nó concreto deve implementar este método para se auto-imprimir,
     * e então chamar recursivamente o mesmo método em seus filhos, ajustando
     * o prefixo de indentação.
     *
     * @param prefix O prefixo de indentação a ser usado para este nó e seus filhos.
     * @return Uma String formatada representando a subárvore.
     */
    public abstract String imprimirArvore(String prefix);
}