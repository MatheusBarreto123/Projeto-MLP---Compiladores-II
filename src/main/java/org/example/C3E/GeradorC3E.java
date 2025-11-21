package org.example.C3E;

import java.util.ArrayList;
import java.util.List;

/**
 * GeradorC3E.java
 * Gerencia a emissão de instruções e a contagem de temporários/rótulos.
 */
public class GeradorC3E {
    private final List<InstrucaoC3E> codigo;
    private int contadorTemporario;
    private int contadorRotulo;

    public GeradorC3E() {
        this.codigo = new ArrayList<>();
        this.contadorTemporario = 0;
        this.contadorRotulo = 0;
    }

    /**
     * Gera e retorna um novo temporário (ex: T1, T2).
     * Usado para armazenar resultados de sub-expressões.
     */
    public String novoTemporario() {
        contadorTemporario++;
        return "T" + contadorTemporario;
    }

    /**
     * Gera e retorna um novo rótulo (ex: L1, L2).
     * Usado para controle de fluxo (condicionais e laços).
     */
    public String novoRotulo() {
        contadorRotulo++;
        return "L" + contadorRotulo;
    }

    /**
     * Emite uma instrução no Código de 3 Endereços.
     * Ex: emitir("T1", "a", "+", "1") -> T1 = a + 1
     */
    public void emitir(String resultado, String operando1, String operador, String operando2) {
        codigo.add(new InstrucaoC3E(resultado, operando1, operador, operando2));
    }

    /**
     * Emite um rótulo no código.
     * Ex: emitirRotulo("L1") -> L1:
     */
    public void emitirRotulo(String rotulo) {
        emitir(rotulo + ":", "", "", "");
    }

    /**
     * Imprime o Código de 3 Endereços gerado.
     */
    public void imprimirCodigo() {
        System.out.println("\n--- Código de 3 Endereços (C3E) ---");
        int linha = 0;
        for (InstrucaoC3E instr : codigo) {
            System.out.println(linha + ": " + instr);
            linha++;
        }
        System.out.println("------------------------------------");
    }

    public List<InstrucaoC3E> getCodigo() {
        return codigo;
    }
}
