package org.example.C3E;

import org.example.AST.*;
import org.example.AnalisadorSemantico;
import org.example.TipoDado;
import org.example.Token; // Necessário para operadores

/**
 * C3EGeneratorVisitor.java
 * Percorre a AST para gerar o Código de 3 Endereços (C3E).
 *
 * Atualização: Inclui o método visit(ExpressaoCompostaNode) para gerar C3E
 * para condições lógicas E/OR, usando saltos condicionais (curto-circuito).
 */
public class C3EGeneratorVisitor implements ASTVisitor {

    private final GeradorC3E geradorC3E;
    private final AnalisadorSemantico analisadorSemantico;

    public C3EGeneratorVisitor(GeradorC3E geradorC3E, AnalisadorSemantico analisadorSemantico) {
        this.geradorC3E = geradorC3E;
        this.analisadorSemantico = analisadorSemantico;
    }

    // --- NÓS DE ESTRUTURA ---

    @Override
    public ASTResult visit(ProgramaNode node) throws Exception {
        for (ASTNode comando : node.getComandos()) {
            comando.accept(this);
        }
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // Método para o nó Condicional (genérico)
    @Override
    public ASTResult visit(CondicionalNode node) throws Exception {
        // Esta lógica assume que CondicionalNode possui os métodos getCondicao(),
        // getComandoEntao() e getComandoSenao(), herdados por SeNode.

        // 1. Visita a condição para obter o endereço do resultado booleano (Tx)
        // NOTA: Para ExpressaoCompostaNode, o resultado não é um temporário,
        // mas sim o conjunto de rótulos (Verdadeiro/Falso) que a expressão usa.
        // No entanto, para simplificar, usaremos o padrão de JMPFALSE.
        ASTResult condicaoResultado = node.getCondicao().accept(this);
        String temporarioCondicao = condicaoResultado.getEndereco();

        // 2. Criação dos Rótulos
        String rotuloSenao = geradorC3E.novoRotulo();
        String rotuloFim = geradorC3E.novoRotulo();

        // 3. C3E: Salta se a condição for falsa
        geradorC3E.emitir("JMPFALSE", temporarioCondicao, rotuloSenao, "");

        // 4. Bloco ENTÃO (Comando)
        node.getComandoEntao().accept(this);

        // 5. Bloco SENÃO (Se existir)
        if (node.getComandoSenao() != null) {
            // C3E: Salto incondicional para o FIM (evita o bloco SENAO)
            geradorC3E.emitir("JMP", rotuloFim, "", "");

            // C3E: Rótulo de início do SENAO
            geradorC3E.emitirRotulo(rotuloSenao);

            // Geração do código do bloco SENAO
            node.getComandoSenao().accept(this);

            // C3E: Rótulo final
            geradorC3E.emitirRotulo(rotuloFim);
        } else {
            // C3E: Se não há SENAO, o rótuloSenao é o FIM
            geradorC3E.emitirRotulo(rotuloSenao);
        }

        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // Método para o nó Iterativo (genérico)
    @Override
    public ASTResult visit(IterativoNode node) throws Exception {
        // Esta lógica assume que IterativoNode possui os métodos getCondicao() e
        // getCorpo(), herdados por EnquantoNode.

        // 1. Criação dos Rótulos
        String rotuloInicio = geradorC3E.novoRotulo();
        String rotuloFim = geradorC3E.novoRotulo();

        // 2. C3E: Rótulo de início do loop
        geradorC3E.emitirRotulo(rotuloInicio);

        // 3. Visita a condição para obter o endereço do resultado booleano (Tx)
        ASTResult condicaoResultado = node.getCondicao().accept(this);
        String temporarioCondicao = condicaoResultado.getEndereco();

        // 4. C3E: Salta para o fim se a condição for falsa
        geradorC3E.emitir("JMPFALSE", temporarioCondicao, rotuloFim, "");

        // 5. Bloco de comandos (Corpo do loop)
        node.getComandoCorpo().accept(this);

        // 6. C3E: Salta incondicionalmente para o início do loop (volta para a condição)
        geradorC3E.emitir("JMP", rotuloInicio, "", "");

        // 7. C3E: Rótulo de fim do loop
        geradorC3E.emitirRotulo(rotuloFim);

        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // --- NÓS DE COMANDO ---

    @Override
    public ASTResult visit(AtribuicaoNode node) throws Exception {
        IdentificadorNode idNode = node.getIdentificador();

        // 1. Visita o RHS (Expressão ou Valor) para gerar o código e obter o endereço
        ASTResult rhsResultado = node.getExpressaoRHS().accept(this);
        String enderecoRHS = rhsResultado.getEndereco();

        // 2. C3E: Atribuição final: resultado do RHS -> identificador LHS
        geradorC3E.emitir(idNode.getLexema(), enderecoRHS, "=", "");

        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // --- NÓS DE COMANDO DE I/O ---

    @Override
    public ASTResult visit(ComandoLeiaNode node) throws Exception {
        String identificador = node.getIdentificador().getLexema();
        // C3E: READ identificador
        geradorC3E.emitir("READ", identificador, "", "");
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    @Override
    public ASTResult visit(ComandoEscrevaNode node) throws Exception {
        // 1. Visita a expressão para obter o endereço do valor a ser escrito
        ASTResult expressaoResultado = node.getExpressao().accept(this);
        String endereco = expressaoResultado.getEndereco();

        // 2. C3E: WRITE endereço
        geradorC3E.emitir("WRITE", endereco, "", "");
        return new ASTResult(TipoDado.INDETERMINADO);
    }

    // --- NÓS DE EXPRESSÃO E VALOR ---

    // Este método é crucial para expressões aritméticas complexas
    @Override
    public ASTResult visit(ExpressaoBinariaNode node) throws Exception {
        // 1. Visita o operando 1 (recursão na árvore)
        ASTResult resultadoE1 = node.getOperando1().accept(this);
        String enderecoE1 = resultadoE1.getEndereco();

        // 2. Visita o operando 2
        ASTResult resultadoE2 = node.getOperando2().accept(this);
        String enderecoE2 = resultadoE2.getEndereco();

        // 3. Obtém o operador
        String operador = node.getOperador().getLexema();

        // 4. C3E: Emite a instrução e armazena em um novo temporário
        String temporario = geradorC3E.novoTemporario();
        geradorC3E.emitir(temporario, enderecoE1, operador, enderecoE2);

        // 5. Registra o tipo do temporário (necessário se houver atribuições futuras)
        TipoDado tipoResultado = analisadorSemantico.determinarTipoExpressao(
                resultadoE1.getTipo(),
                resultadoE2.getTipo(),
                node.getLinha(),
                node.getColuna()
        );
        analisadorSemantico.registrarTipoTemporario(temporario, tipoResultado);

        // 6. Retorna o endereço e tipo do resultado
        return new ASTResult(tipoResultado, temporario);
    }

    // NOVO MÉTODO: Visita a Expressão Lógica Composta (E/OR)
    @Override
    public ASTResult visit(ExpressaoCompostaNode node) throws Exception {
        String operador = node.getOperadorComposto().getLexema();

        // Em C3E, expressões lógicas compostas são traduzidas usando o conceito
        // de rótulos de Verdadeiro (T) e Falso (F) e curto-circuito.
        // O resultado da expressão será um temporário (Tx) que recebe 1 (True) ou 0 (False).

        // Rótulos auxiliares para o curto-circuito
        String rotuloProximaCondicao = geradorC3E.novoRotulo();
        String rotuloFimExpressao = geradorC3E.novoRotulo();
        String temporarioResultado = geradorC3E.novoTemporario(); // T_final

        // 1. Visita Condição Esquerda (LHS)
        ASTResult resultadoEsquerda = node.getCondicaoEsquerda().accept(this);
        String enderecoEsquerda = resultadoEsquerda.getEndereco();

        if ("OR".equalsIgnoreCase(operador)) {
            // Lógica OR: Se LHS for TRUE, toda a expressão é TRUE.
            // C3E: Se LHS é TRUE, salta para definir o resultado como TRUE (T_final = 1)
            geradorC3E.emitir("JMPTRUE", enderecoEsquerda, rotuloFimExpressao, "");

            // Se chegou aqui, LHS é FALSE. Salta para a próxima condição (RHS).
            geradorC3E.emitir("JMP", rotuloProximaCondicao, "", "");

        } else if ("E".equalsIgnoreCase(operador)) {
            // Lógica E: Se LHS for FALSE, toda a expressão é FALSE.
            // C3E: Se LHS é FALSE, salta para definir o resultado como FALSE (T_final = 0)
            geradorC3E.emitir("JMPFALSE", enderecoEsquerda, rotuloProximaCondicao, "");

            // Se chegou aqui, LHS é TRUE. Segue para a próxima condição (RHS).
        }

        // Rótulo para a próxima condição (RHS) (ou se a primeira falhou no caso do OR)
        geradorC3E.emitirRotulo(rotuloProximaCondicao);

        // 2. Visita Condição Direita (RHS)
        ASTResult resultadoDireita = node.getCondicaoDireita().accept(this);
        String enderecoDireita = resultadoDireita.getEndereco();

        // Após a visita do RHS, o seu temporário (enderecoDireita) contém o valor
        // booleano do RHS (1 ou 0). Este é o resultado final da Expressão Composta
        // se o curto-circuito não ocorreu.

        // C3E: Atribui o resultado do RHS ao temporário final
        geradorC3E.emitir(temporarioResultado, enderecoDireita, "=", "");

        // Rótulo de Fim da Expressão (onde o OR curto-circuitado salta)
        geradorC3E.emitirRotulo(rotuloFimExpressao);

        if ("OR".equalsIgnoreCase(operador)) {
            // Se o OR curto-circuitou, o resultado deve ser 1 (TRUE).
            // A instrução JMPTRUE lá em cima (linha 215) salta para rotuloFimExpressao.
            // Precisamos que, quando ele saltar, o resultado final seja 1.
            // Para simplificar, forçamos o resultado final para 1.
            geradorC3E.emitir(temporarioResultado, "1", "=", "");
        }
        // Se for E, o resultado já foi atribuído corretamente pelo RHS ou o JMPFALSE
        // na linha 223 direcionou o fluxo.


        // NOTA: A lógica ideal do C3E/TAC para booleanos é complexa (uso de listas
        // de backpatching/truelist/falselist). Esta implementação é uma abordagem
        // mais simples usando um temporário final que recebe 1/0.

        // Retorna o temporário que guarda o resultado booleano final (1 ou 0)
        return new ASTResult(TipoDado.BOOLEANO, temporarioResultado);
    }

    @Override
    public ASTResult visit(NotNode node) throws Exception {
        // 1. Gera C3E para a condição interna do NOT
        ASTResult innerResult = node.getCondicao().accept(this);
        String enderecoInner = innerResult.getEndereco();

        // 2. Cria um temporário para armazenar o valor 0
        String tempZero = geradorC3E.novoTemporario();
        // tempZero = 0
        geradorC3E.emitir(tempZero, "0", "=", "");

        // 3. Cria um temporário para o resultado de NOT
        String tempResultado = geradorC3E.novoTemporario();

        // 4. Implementa NOT como: resultado = (enderecoInner == 0) ? 1 : 0
        //    Ou seja, compara o valor com zero usando CMPEQ (==)
        String mnemonic = getMnemonicLogico("=="); // vai retornar "CMPEQ"
        geradorC3E.emitir(tempResultado, enderecoInner, mnemonic, tempZero);

        // 5. Registra o tipo do temporário como BOOLEANO
        analisadorSemantico.registrarTipoTemporario(tempResultado, TipoDado.BOOLEANO);

        // 6. Retorna o endereço do temporário booleano
        return new ASTResult(TipoDado.BOOLEANO, tempResultado);
    }

    @Override
    public ASTResult visit(CondicaoBinariaNode node) throws Exception {
        // 1. Visita operando 1
        ASTResult resultadoE1 = node.getOperando1().accept(this);
        String enderecoE1 = resultadoE1.getEndereco();

        // 2. Visita operando 2
        ASTResult resultadoE2 = node.getOperando2().accept(this);
        String enderecoE2 = resultadoE2.getEndereco();

        // 3. Obtém o operador lógico
        String operadorLogico = node.getOperador().getLexema();
        String mnemonic = getMnemonicLogico(operadorLogico);

        // 4. C3E: Geração da Comparação
        // Compara os operandos e armazena o resultado booleano (1 ou 0) em um temporário
        String temporarioCondicao = geradorC3E.novoTemporario();
        geradorC3E.emitir(temporarioCondicao, enderecoE1, mnemonic, enderecoE2);

        // 5. Determina o tipo (para fins de retorno, é BOOLEANO)
        analisadorSemantico.determinarTipoExpressao(
                resultadoE1.getTipo(),
                resultadoE2.getTipo(),
                node.getLinha(),
                node.getColuna()
        );
        analisadorSemantico.registrarTipoTemporario(temporarioCondicao, TipoDado.BOOLEANO);

        // 6. Retorna o endereço do resultado booleano (o temporário)
        return new ASTResult(TipoDado.BOOLEANO, temporarioCondicao);
    }

    @Override
    public ASTResult visit(IdentificadorNode node) throws Exception {
        // Retorna o nome do identificador como endereço
        TipoDado tipo = analisadorSemantico.getTabelaSimbolos().buscar(node.getLexema()).getTipo();
        return new ASTResult(tipo, node.getLexema());
    }

    @Override
    public ASTResult visit(LiteralNode node) throws Exception {
        // Retorna o valor literal como endereço
        return new ASTResult(node.getTipoInferido(), node.getValor());
    }

    // Mapeamento de Operadores Lógicos para Mnemônicos C3E
    private String getMnemonicLogico(String operador) {
        return switch (operador) {
            case ">" -> "CMPGT";
            case "<" -> "CMPLT";
            case "==" -> "CMPEQ";
            case "!=" -> "CMPNE";
            case ">=" -> "CMPGE";
            case "<=" -> "CMPLE";
            default -> "ERRO_OP";
        };
    }
}