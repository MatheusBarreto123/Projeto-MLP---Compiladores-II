package org.example.AST;

import org.example.TipoDado;

/**
 * Nó de Identificador (Variável).
 * Representa o uso de um identificador (variável) em expressões ou comandos.
 */
public class IdentificadorNode extends ASTNode {
    private final String lexema;

    // Campo para guardar o tipo após a Análise Semântica (futuro)
    private TipoDado tipoDeclarado;

    public IdentificadorNode(String lexema, int linha, int coluna) {
        super(linha, coluna);
        this.lexema = lexema;
    }

    public String getLexema() { return lexema; }
    public void setTipoDeclarado(TipoDado tipo) { this.tipoDeclarado = tipo; }
    public TipoDado getTipoDeclarado() { return tipoDeclarado; }


    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }

    /**
     * Implementa a visualização da AST para o nó Identificador.
     * Como é um nó folha (não tem filhos AST), ele apenas imprime sua informação.
     *
     * @param prefix Prefixo de indentação.
     * @return Uma String formatada representando o nó.
     */
    @Override
    public String imprimirArvore(String prefix) {
        String tipoInfo = (tipoDeclarado != null) ? " (Tipo: " + tipoDeclarado + ")" : "";
        return prefix + "└── IdentificadorNode: " + lexema + tipoInfo + "\n";
    }
}