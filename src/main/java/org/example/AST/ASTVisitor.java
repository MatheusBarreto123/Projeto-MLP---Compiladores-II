package org.example.AST;


/**
 * ASTVisitor.java
 * Interface que define as operações de visita para cada tipo de nó da AST.
 */
public interface ASTVisitor {

    // NÓS DE ESTRUTURA
    ASTResult visit(ProgramaNode node) throws Exception;
    ASTResult visit(CondicionalNode node) throws Exception;
    ASTResult visit(IterativoNode node) throws Exception;

    // NÓS DE COMANDO
    ASTResult visit(AtribuicaoNode node) throws Exception;
    ASTResult visit(ComandoLeiaNode node) throws Exception;
    ASTResult visit(ComandoEscrevaNode node) throws Exception;

    // NÓS DE EXPRESSÃO E VALOR
    ASTResult visit(ExpressaoBinariaNode node) throws Exception;
    ASTResult visit(CondicaoBinariaNode node) throws Exception; // Se você modelar a condição como nó
    ASTResult visit(IdentificadorNode node) throws Exception;
    ASTResult visit(LiteralNode node) throws Exception;
    ASTResult visit(ExpressaoCompostaNode node)throws Exception;
    ASTResult visit(NotNode node) throws Exception;

}
