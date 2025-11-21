package org.example.AST;

import org.example.TipoDado;

/**
 * ASTResult.java
 * Classe auxiliar para armazenar o resultado da análise de um nó da AST.
 * Na fase semântica, o principal resultado é o TipoDado.
 */
public class ASTResult {
    private final TipoDado tipo;
    private final String endereco; // Onde o resultado da expressão foi armazenado (ID ou T1)

    // Construtor usado pelo ASTSemanticAnalyzer (fase 1)
    public ASTResult(TipoDado tipo) {
        this(tipo, null);
    }

    // Construtor usado pelo C3EGenerator (fase 2)
    public ASTResult(TipoDado tipo, String endereco) {
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public TipoDado getTipo() {
        return tipo;
    }

    public String getEndereco() {
        return endereco;
    }
}
