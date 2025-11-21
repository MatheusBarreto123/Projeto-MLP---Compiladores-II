package org.example.AST;

import org.example.AnalisadorSemantico;
import org.example.TipoDado;

/**
 * ASTSemanticAnalyzer.java
 * Percorre a AST para realizar a Análise Semântica (checagem de tipos).
 */
public class ASTSemanticAnalyzer implements ASTVisitor {

    // O Analisador Semântico original é usado aqui como utilitário de checagem
    private final AnalisadorSemantico analisadorSemantico;

    public ASTSemanticAnalyzer(AnalisadorSemantico analisadorSemantico) {
        this.analisadorSemantico = analisadorSemantico;
    }

    // --- NÓS DE ESTRUTURA ---

    @Override
    public ASTResult visit(ProgramaNode node) throws Exception {
        // Visita todos os comandos na ordem
        for (ASTNode comando : node.getComandos()) {
            comando.accept(this);
        }
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    @Override
    public ASTResult visit(CondicionalNode node) throws Exception {
        // 1. Visita a condição: deve retornar um tipo numérico compatível
        ASTResult condicaoResultado = node.getCondicao().accept(this);

        // Regra Semântica: A condição deve ser numérica (INTEIRO ou REAL)
        // NOTA: Se a condição for uma ExpressaoComposta, ela retorna BOOLEANO, que é OK.
        TipoDado tipoCondicao = condicaoResultado.getTipo();

        // Assumindo que BOOLEANO é o resultado de qualquer expressão lógica, incluindo Compostas e Binárias.
        if (tipoCondicao != TipoDado.INTEIRO &&
                tipoCondicao != TipoDado.REAL &&
                tipoCondicao != TipoDado.BOOLEANO) {
            throw new Exception("Erro Semântico (Linha " + node.linha +
                    "): Condição de controle 'se' deve resultar em valor numérico ou booleano.");
        }

        // 2. Visita o bloco ENTÃO
        node.getComandoEntao().accept(this);

        // 3. Visita o bloco SENÃO (se existir)
        if (node.getComandoSenao() != null) {
            node.getComandoSenao().accept(this);
        }
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    @Override
    public ASTResult visit(IterativoNode node) throws Exception {
        // 1. Visita a condição (mesma checagem do Condicional)
        ASTResult condicaoResultado = node.getCondicao().accept(this);

        TipoDado tipoCondicao = condicaoResultado.getTipo();
        if (tipoCondicao != TipoDado.INTEIRO &&
                tipoCondicao != TipoDado.REAL &&
                tipoCondicao != TipoDado.BOOLEANO) {
            throw new Exception("Erro Semântico (Linha " + node.linha +
                    "): Condição de controle 'enquanto' deve resultar em valor numérico ou booleano.");
        }

        // 2. Visita o corpo do laço
        node.getComandoCorpo().accept(this);
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // --- NÓS DE COMANDO ---

    @Override
    public ASTResult visit(AtribuicaoNode node) throws Exception {
        // 1. Visita o LHS (Identificador)
        IdentificadorNode idNode = node.getIdentificador();
        ASTResult idResultado = idNode.accept(this);
        TipoDado tipoLHS = idResultado.getTipo();

        // 2. Visita o RHS (Expressão ou Valor)
        ASTResult rhsResultado = node.getExpressaoRHS().accept(this);
        TipoDado tipoRHS = rhsResultado.getTipo();

        // 3. Checagem de Atribuição (Reutiliza a lógica existente)
        analisadorSemantico.checarAtribuicao(
                tipoLHS, tipoRHS, idNode.linha, idNode.coluna
        );

        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // Exemplo de implementação para Leitura
    @Override
    public ASTResult visit(ComandoLeiaNode node) throws Exception {
        // A leitura não altera o tipo, mas checa se a variável existe.
        // O nó 'leia' geralmente contém um IdentificadorNode.
        node.getIdentificador().accept(this);
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // Exemplo de implementação para Escrita
    @Override
    public ASTResult visit(ComandoEscrevaNode node) throws Exception {
        // O nó 'escreva' precisa que a expressão seja validada
        node.getExpressao().accept(this);
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // --- NÓS DE EXPRESSÃO E VALOR ---

    @Override
    public ASTResult visit(ExpressaoBinariaNode node) throws Exception {
        // 1. Visita operando 1 (recursão na árvore)
        ASTResult resultadoE1 = node.getOperando1().accept(this);

        // 2. Visita operando 2
        ASTResult resultadoE2 = node.getOperando2().accept(this);

        // 3. Determina o tipo resultante (Reutiliza a lógica existente)
        TipoDado tipoResultado = analisadorSemantico.determinarTipoExpressao(
                resultadoE1.getTipo(),
                resultadoE2.getTipo(),
                node.linha,
                node.coluna
        );

        return new ASTResult(tipoResultado);
    }

    // Expressão lógica composta (E/OR)
    @Override
    public ASTResult visit(ExpressaoCompostaNode node) throws Exception {
        // 1. Visita a Condição Esquerda
        ASTResult resultadoEsquerda = node.getCondicaoEsquerda().accept(this);
        TipoDado tipoEsquerda = resultadoEsquerda.getTipo();

        // 2. Visita a Condição Direita
        ASTResult resultadoDireita = node.getCondicaoDireita().accept(this);
        TipoDado tipoDireita = resultadoDireita.getTipo();

        boolean tipoEsquerdaValido =
                tipoEsquerda == TipoDado.BOOLEANO ||
                        tipoEsquerda == TipoDado.INTEIRO ||
                        tipoEsquerda == TipoDado.REAL;

        boolean tipoDireitaValido =
                tipoDireita == TipoDado.BOOLEANO ||
                        tipoDireita == TipoDado.INTEIRO ||
                        tipoDireita == TipoDado.REAL;

        if (!tipoEsquerdaValido || !tipoDireitaValido) {
            throw new Exception("Erro Semântico (Linha " + node.linha +
                    "): Operadores lógicos 'E'/'OR' requerem condições booleanas ou numéricas.");
        }

        // Resultado de E/OR é sempre booleano
        return new ASTResult(TipoDado.BOOLEANO);
    }

    @Override
    public ASTResult visit(CondicaoBinariaNode node) throws Exception {
        // 1. Visita operando 1
        ASTResult resultadoE1 = node.getOperando1().accept(this);
        // 2. Visita operando 2
        ASTResult resultadoE2 = node.getOperando2().accept(this);

        // Na MLP, as condições só devem envolver tipos numéricos (INTEIRO ou REAL)
        TipoDado tipoE1 = resultadoE1.getTipo();
        TipoDado tipoE2 = resultadoE2.getTipo();

        if ((tipoE1 != TipoDado.INTEIRO && tipoE1 != TipoDado.REAL) ||
                (tipoE2 != TipoDado.INTEIRO && tipoE2 != TipoDado.REAL)) {
            throw new Exception("Erro Semântico (Linha " + node.linha +
                    "): Operação relacional inválida com tipo CARACTER.");
        }

        // O analisador semântico interno garante que a operação é válida.
        analisadorSemantico.determinarTipoExpressao(
                tipoE1, tipoE2, node.linha, node.coluna
        );

        // O tipo resultante de uma condição binária é BOOLEANO.
        return new ASTResult(TipoDado.BOOLEANO);
    }

    @Override
    public ASTResult visit(IdentificadorNode node) throws Exception {
        // Checa a declaração e obtém o tipo
        TipoDado tipo = analisadorSemantico.checarDeclaracao(
                node.getLexema(), node.linha, node.coluna
        );
        // Armazena o tipo no próprio nó (atributo sintetizado)
        node.setTipoDeclarado(tipo);
        return new ASTResult(tipo);
    }

    @Override
    public ASTResult visit(LiteralNode node) throws Exception {
        // Retorna o tipo inferido do literal
        return new ASTResult(node.getTipoInferido());
    }

    // --- NOVO: NÓ NOT ---

    @Override
    public ASTResult visit(NotNode node) throws Exception {
        // Visita a condição interna
        ASTResult innerResult = node.getCondicao().accept(this);
        TipoDado tipoInner = innerResult.getTipo();

        // NOT só faz sentido em booleano ou numérico tratado como booleano
        if (tipoInner != TipoDado.BOOLEANO &&
                tipoInner != TipoDado.INTEIRO &&
                tipoInner != TipoDado.REAL) {
            throw new Exception("Erro Semântico (Linha " + node.linha +
                    "): Operador NOT exige condição numérica ou booleana.");
        }

        // Resultado de NOT é sempre BOOLEANO
        return new ASTResult(TipoDado.BOOLEANO);
    }
}
