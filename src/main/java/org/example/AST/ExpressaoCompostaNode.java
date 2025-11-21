package org.example.AST;

import org.example.Token; // Assumindo que a classe Token está no pacote org.example

/**
 * ExpressaoCompostaNode
 * Representa uma expressão lógica composta (condição composta) na AST,
 * unindo duas sub-condições através dos operadores lógicos E (AND) ou OR (OU).
 * * Exemplo: (x <= 5.0) OR (z > 10)
 */
public class ExpressaoCompostaNode extends ASTNode {

    private final ASTNode condicaoEsquerda;
    private final Token operadorComposto; // Será um Token do tipo TipoToken.E ou TipoToken.OU
    private final ASTNode condicaoDireita;

    /**
     * Construtor para ExpressaoCompostaNode.
     *
     * @param condicaoEsquerda O nó AST da condição à esquerda.
     * @param operadorComposto O token do operador lógico (E ou OR).
     * @param condicaoDireita  O nó AST da condição à direita.
     * @param linha            A linha de início (geralmente do operador composto).
     * @param coluna           A coluna de início (geralmente do operador composto).
     */
    public ExpressaoCompostaNode(ASTNode condicaoEsquerda, Token operadorComposto, ASTNode condicaoDireita, int linha, int coluna) {
        super(linha, coluna);
        this.condicaoEsquerda = condicaoEsquerda;
        this.operadorComposto = operadorComposto;
        this.condicaoDireita = condicaoDireita;
    }

    // --- Getters para acesso ---
    public ASTNode getCondicaoEsquerda() {
        return condicaoEsquerda;
    }

    public Token getOperadorComposto() {
        return operadorComposto;
    }

    public ASTNode getCondicaoDireita() {
        return condicaoDireita;
    }

    // --- Implementação do método toString (útil para visualização/debug da AST) ---
    @Override
    public String toString() {
        return String.format(
                "ExpressaoComposta(%s %s %s)",
                condicaoEsquerda.toString(),
                operadorComposto.getLexema(),
                condicaoDireita.toString()
        );
    }


    @Override
    public ASTResult accept(ASTVisitor visitor) throws Exception {
        return visitor.visit(this);
    }


    @Override
    public String imprimirArvore(String prefixo) {
        // Constrói a string do nó atual
        StringBuilder sb = new StringBuilder();

        sb.append(prefixo).append("├── ExpressaoCompostaNode (").append(getOperadorComposto().getLexema()).append(")\n");

        String novoPrefixo = prefixo + "│   ";

        // Concatena a Condição Esquerda (usando o valor retornado pelo filho)
        sb.append(novoPrefixo).append("├── Condicao Esquerda:\n");
        sb.append(getCondicaoEsquerda().imprimirArvore(novoPrefixo + "│   "));

        // Concatena a Condição Direita (usando o valor retornado pelo filho)
        sb.append(novoPrefixo).append("└── Condicao Direita:\n");
        sb.append(getCondicaoDireita().imprimirArvore(novoPrefixo + "    "));

        // Retorna a string final
        return sb.toString();
    }
    // Você deve adicionar aqui a lógica para Geração de Código Intermediário
    // no futuro (Etapa 2 do projeto).
    // Exemplo:
    // @Override
    // public List<String> toCodigoIntermediario(TabelaSimbolos ts) { ... }
}