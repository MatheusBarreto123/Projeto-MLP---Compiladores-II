package org.example;

/**
 * AnalisadorSemantico.java
 * Classe responsável por implementar a lógica de verificação de tipos e gerenciar a Tabela de Símbolos.
 */
public class AnalisadorSemantico {
    private final TabelaSimbolos tabelaSimbolos;

    public AnalisadorSemantico() {
        this.tabelaSimbolos = new TabelaSimbolos();
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    /**
     * Semântica (Declaração): Verifica se a variável foi declarada (Regra SEM_02).
     */
    public TipoDado checarDeclaracao(String lexema, int linha, int coluna) throws Exception {
        EntradaTabelaSimbolos entrada = tabelaSimbolos.buscar(lexema);
        if (entrada == null) {
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Variável '" + lexema + "' não declarada (COD. SEM_02).");
        }
        return entrada.getTipo();
    }


    /**
     * Semântica (Atribuição): Verifica compatibilidade de tipos (Regra SEM_04).
     * Permite alargamento (INTEIRO -> REAL), mas não o inverso.
     * @param tipoLHS Tipo da variável no lado esquerdo.
     * @param tipoRHS Tipo resultante da expressão no lado direito.
     */
    public void checarAtribuicao(TipoDado tipoLHS, TipoDado tipoRHS, int linha, int coluna) throws Exception {
        // 1. Tipos devem ser numéricos
        if (tipoLHS == TipoDado.CARACTER || tipoRHS == TipoDado.CARACTER) {
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Atribuição inválida envolvendo tipo CARACTER (COD. SEM_04).");
        }

        // 2. Compatibilidade
        if (tipoLHS == TipoDado.REAL && tipoRHS == TipoDado.INTEIRO) {
            // OK: Alargamento (Widening)
            return;
        }

        if (tipoLHS != tipoRHS) {
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Atribuição incompatível. Não é possível atribuir '" + tipoRHS.name() +
                    "' a variável '" + tipoLHS.name() + "' (COD. SEM_04).");
        }
    }

    /**
     * Semântica (Expressão): Determina o tipo resultante de uma operação binária.
     * @param tipoE1 Tipo do operando 1.
     * @param tipoE2 Tipo do operando 2.
     * @param linha Linha para relatar erro.
     * @param coluna Coluna para relatar erro.
     * @return O TipoDado promovido (REAL > INTEIRO).
     */
    public TipoDado determinarTipoExpressao(TipoDado tipoE1, TipoDado tipoE2, int linha, int coluna) throws Exception {
        // Checagem de operações inválidas (Regra SEM_03)
        if (tipoE1 == TipoDado.CARACTER || tipoE2 == TipoDado.CARACTER) {
            throw new Exception("Erro Semântico (Linha " + linha + ", Coluna " + coluna +
                    "): Operação aritmética inválida com tipo CARACTER (COD. SEM_03).");
        }

        // Promoção de Tipo: Se um for REAL, o resultado é REAL
        if (tipoE1 == TipoDado.REAL || tipoE2 == TipoDado.REAL) {
            return TipoDado.REAL;
        }

        // Caso contrário, o resultado é INTEIRO
        return TipoDado.INTEIRO;
    }

    /**
     * Registra o tipo de uma variável temporária gerada pelo C3E.
     * Isso permite que temporários sejam tratados como variáveis válidas
     * e tenham seus tipos checados em expressões subsequentes.
     * * @param temporario O nome do temporário (ex: "T1").
     * @param tipo O TipoDado do temporário (INTEIRO ou REAL).
     */
    public void registrarTipoTemporario(String temporario, TipoDado tipo) {
        this.tabelaSimbolos.inserirSimboloGerado(temporario, tipo);
    }

}