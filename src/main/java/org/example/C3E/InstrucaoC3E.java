package org.example.C3E;

/**
 * InstrucaoC3E.java
 * Representa uma instrução no Código de 3 Endereços (TAC).
 */
public class InstrucaoC3E {
    private final String resultado;
    private final String operando1;
    private final String operador;
    private final String operando2;

    public InstrucaoC3E(String resultado, String operando1, String operador, String operando2) {
        this.resultado = resultado;
        this.operando1 = operando1;
        this.operador = operador;
        this.operando2 = operando2;
    }

    /**
     * Formata a instrução para exibição, adaptando-se a diferentes mnemônicos.
     * Ex: T1 = a + 1; LABEL L1; JMPFALSE T1, L2
     */
    @Override
    public String toString() {
        if (operador.isEmpty() && operando1.isEmpty() && operando2.isEmpty()) {
            // Instrução de rótulo (LABEL)
            return String.format("%s", resultado);
        } else if (operador.isEmpty()) {
            // Instrução de atribuição simples ou salto (JMP L1, T1 = a)
            if (resultado.startsWith("JMP") || resultado.startsWith("LABEL")) { // Rótulo ou Salto simples
                return String.format("%s %s", resultado, operando1);
            } else { // Atribuição T1 = a
                return String.format("%s = %s", resultado, operando1);
            }
        } else if (operando2.isEmpty()) {
            // Instruções unárias, atribuição direta (=) ou CALL, etc.
            return String.format("%s %s %s", resultado, operador, operando1);
        } else {
            // Instruções binárias (ADD, SUB, CMP)
            return String.format("%s = %s %s %s", resultado, operando1, operador, operando2);
        }
    }

    // Getters
    public String getResultado() { return resultado; }
    public String getOperando1() { return operando1; }
    public String getOperador() { return operador; }
    public String getOperando2() { return operando2; }
}
