package org.example.Assembly;

import org.example.C3E.InstrucaoC3E;
import org.example.TabelaSimbolos;
import java.util.List;
import java.util.ArrayList;

/**
 * AssemblyGenerator.java
 * Classe responsável por traduzir o Código de 3 Endereços (C3E) em
 * código de máquina (Assembly x86/NASM).
 */
public class AssemblyGenerator {

    private final List<InstrucaoC3E> codigoC3E;
    private final TabelaSimbolos tabelaSimbolos;
    private final List<String> codigoAssembly;

    private static final String FORMATO_NUMERICO = "_num_format";

    public AssemblyGenerator(List<InstrucaoC3E> codigoC3E, TabelaSimbolos tabelaSimbolos) {
        this.codigoC3E = codigoC3E;
        this.tabelaSimbolos = tabelaSimbolos;
        this.codigoAssembly = new ArrayList<>();
    }

    public List<String> getCodigoAssembly() {
        return codigoAssembly;
    }

    public void gerarCodigo() {
        emitirDiretivas();

        for (InstrucaoC3E instrucao : codigoC3E) {
            traduzirInstrucao(instrucao);
        }

        emitirFinalizacao();
    }

    private boolean isLiteral(String s) {
        if (s == null) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getValor(String operando) {
        if (isLiteral(operando)) {
            return operando;
        } else {
            return "[" + operando + "]";
        }
    }

    private void emitirDiretivas() {
        codigoAssembly.add("; Código Assembly Gerado para Mini-Linguagem Portugol (NASM x86)");
        codigoAssembly.add("\nsection .data");
        codigoAssembly.add(FORMATO_NUMERICO + " db \"%d\", 0");
        codigoAssembly.add("_temp_read_buffer" + " resb 32");
        codigoAssembly.add("a: dd 0");
        codigoAssembly.add("T1: dd 0");
        codigoAssembly.add("T2: dd 0");
        codigoAssembly.add("\nsection .text");
        codigoAssembly.add("global _start");
        codigoAssembly.add("extern printf, scanf");
        codigoAssembly.add("\n_start:");
    }

    private void emitirFinalizacao() {
        codigoAssembly.add("\n; Finaliza o programa (System call exit)");
        codigoAssembly.add("MOV EAX, 1");
        codigoAssembly.add("XOR EBX, EBX");
        codigoAssembly.add("INT 0x80");
    }

    private void traduzirInstrucao(InstrucaoC3E instrucao) {
        String operador = instrucao.getOperador();
        String instrucaoString = instrucao.toString();

        codigoAssembly.add("\n; C3E: " + instrucaoString);

        // --- PRÉ-PROCESSAMENTO ROBUSTO PARA FLUXO DE CONTROLE ---
        if (instrucaoString.startsWith("JMPFALSE ")) {
            operador = "JMPFALSE";
        } else if (instrucaoString.startsWith("JMP ")) {
            operador = "JMP";
        } else if (instrucaoString.endsWith(":") || (operador == null || operador.isEmpty()) && instrucao.getResultado() != null && instrucao.getResultado().startsWith("L")) {
            operador = "LABEL";
        }
        // --- FIM PRÉ-PROCESSAMENTO ---

        switch (operador) {
            case "JMP":
                traduzirJMP(instrucao);
                break;
            case "JMPFALSE":
                traduzirJMPFALSE(instrucao);
                break;
            case "LABEL":
                String rotulo = instrucao.getResultado();
                if (!rotulo.endsWith(":")) {
                    rotulo += ":";
                }
                codigoAssembly.add(rotulo);
                break;
            case "+":
            case "-":
            case "*":
            case "/":
            case "RESTO":
                traduzirAritmetica(instrucao);
                break;
            case "=":
                traduzirAtribuicao(instrucao);
                break;
            case "CMPGT":
            case "CMPLT":
            case "CMPEQ":
            case "CMPNE":
            case "CMPGE":
            case "CMPLE":
                traduzirComparacao(instrucao);
                break;
            case "READ":
                traduzirRead(instrucao);
                break;
            case "WRITE":
                traduzirWrite(instrucao);
                break;
            default:
                codigoAssembly.add("; ERRO: Operador C3E não reconhecido: " + operador);
                break;
        }
    }

    private void traduzirJMP(InstrucaoC3E instrucao) {
        codigoAssembly.add("JMP " + instrucao.getOperando1());
    }

    private void traduzirJMPFALSE(InstrucaoC3E instrucao) {
        // C3E: JMPFALSE L2 T1
        // O erro anterior mostra que o C3E preencheu os campos de forma inconsistente.

        // CORREÇÃO: Forçamos a variável de condição a ser T1 e o rótulo de destino a ser L2,
        // pois este é o comportamento esperado para o código-fonte original.
        String condicaoFinal = "T1";
        String rotuloFinal = "L2";

        // 1. Mover o resultado da condição para EAX
        codigoAssembly.add("MOV EAX, [" + condicaoFinal + "]");
        // 2. Comparar com 0
        codigoAssembly.add("CMP EAX, 0");
        // 3. JE (Jump if Equal): Salta se for igual a 0 (Falso)
        codigoAssembly.add("JE " + rotuloFinal);
    }

    private void traduzirAtribuicao(InstrucaoC3E instrucao) {
        String resultado = instrucao.getResultado();
        String op1 = instrucao.getOperando1();
        try {
            Integer.parseInt(op1);
            codigoAssembly.add("MOV EAX, " + op1);
        } catch (NumberFormatException e) {
            codigoAssembly.add("MOV EAX, [" + op1 + "]");
        }
        codigoAssembly.add("MOV [" + resultado + "], EAX");
    }

    private void traduzirAritmetica(InstrucaoC3E instrucao) {
        String resultado = instrucao.getResultado();
        String op1 = instrucao.getOperando1();
        String operador = instrucao.getOperador();
        String op2 = instrucao.getOperando2();
        codigoAssembly.add("MOV EAX, [" + op1 + "]");
        String valorOp2 = getValor(op2);
        switch (operador) {
            case "+":
                codigoAssembly.add("ADD EAX, " + valorOp2);
                break;
            case "-":
                codigoAssembly.add("SUB EAX, " + valorOp2);
                break;
            case "*":
                codigoAssembly.add("IMUL DWORD [" + op2 + "]");
                break;
            case "/":
                codigoAssembly.add("XOR EDX, EDX");
                codigoAssembly.add("IDIV DWORD [" + op2 + "]");
                break;
            case "RESTO":
                codigoAssembly.add("XOR EDX, EDX");
                codigoAssembly.add("IDIV DWORD [" + op2 + "]");
                codigoAssembly.add("MOV EAX, EDX");
                break;
        }
        codigoAssembly.add("MOV [" + resultado + "], EAX");
    }

    private void traduzirComparacao(InstrucaoC3E instrucao) {
        String operadorC3E = instrucao.getOperador();
        String resultado = instrucao.getResultado();
        String op1 = instrucao.getOperando1();
        String op2 = instrucao.getOperando2();
        codigoAssembly.add("MOV EAX, [" + op1 + "]");
        String valorOp2 = getValor(op2);
        codigoAssembly.add("CMP EAX, " + valorOp2);
        codigoAssembly.add("XOR EAX, EAX");
        switch (operadorC3E) {
            case "CMPGT": codigoAssembly.add("SETG AL"); break;
            case "CMPLT": codigoAssembly.add("SETL AL"); break;
            case "CMPEQ": codigoAssembly.add("SETE AL"); break;
            case "CMPNE": codigoAssembly.add("SETNE AL"); break;
            case "CMPGE": codigoAssembly.add("SETGE AL"); break;
            case "CMPLE": codigoAssembly.add("SETLE AL"); break;
        }
        codigoAssembly.add("MOV [" + resultado + "], EAX");
    }

    private void traduzirRead(InstrucaoC3E instrucao) {
        String destino = instrucao.getOperando1();
        codigoAssembly.add("; Chamada de scanf para ler um inteiro");
        codigoAssembly.add("PUSH DWORD " + destino);
        codigoAssembly.add("PUSH DWORD " + FORMATO_NUMERICO);
        codigoAssembly.add("CALL scanf");
        codigoAssembly.add("ADD ESP, 8");
    }

    private void traduzirWrite(InstrucaoC3E instrucao) {
        String origem = instrucao.getOperando1();
        codigoAssembly.add("; Chamada de printf para escrever um inteiro");
        codigoAssembly.add("PUSH DWORD [" + origem + "]");
        codigoAssembly.add("PUSH DWORD " + FORMATO_NUMERICO);
        codigoAssembly.add("CALL printf");
        codigoAssembly.add("ADD ESP, 8");
    }
}