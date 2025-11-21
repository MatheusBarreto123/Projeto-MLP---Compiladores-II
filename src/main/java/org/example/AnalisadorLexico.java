package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalisadorLexico.java
 * Classe responsável por ler o código fonte da MLP e extrair uma
 * sequência de Tokens.
 */
public class AnalisadorLexico {
    private final String codigoFonte;
    private int ponteiro = 0; // Posição atual no código
    private int linha = 1;    // Linha atual
    private int coluna = 1;   // Coluna atual

    // Mapa de Palavras Reservadas para Lookup Rápido
    private static final Map<String, TipoToken> PALAVRAS_RESERVADAS;
    static {
        PALAVRAS_RESERVADAS = new HashMap<>();
        // Inicializa o mapa com as palavras reservadas do enum
        for (TipoToken tipo : TipoToken.values()) {
            if (tipo.getLexema().matches("[a-zA-Z]+")) {
                PALAVRAS_RESERVADAS.put(tipo.getLexema(), tipo);
            }
        }
        // Adiciona operadores/símbolos de múltiplas letras (como RESTO)
        PALAVRAS_RESERVADAS.put("RESTO", TipoToken.RESTO);
        PALAVRAS_RESERVADAS.put("E", TipoToken.E);
        PALAVRAS_RESERVADAS.put("OR", TipoToken.OU);
        PALAVRAS_RESERVADAS.put("NOT", TipoToken.NAO);
    }

    public AnalisadorLexico(String codigoFonte) {
        // Adiciona '\n' ou espaço final para garantir que o último token seja tratado
        this.codigoFonte = codigoFonte + (codigoFonte.endsWith("\n") ? "" : "\n");
    }

    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = proximoToken();
            if (token.getTipo() != TipoToken.EOF) {
                tokens.add(token);
            }
            if (token.getTipo() == TipoToken.ERRO) {
                System.err.println("Erro Léxico: " + token.getLexema() + " na Linha " + token.getLinha() + ", Coluna " + token.getColuna());
                break;
            }
        } while (token.getTipo() != TipoToken.EOF);

        // Adiciona o token EOF para o Analisador Sintático saber o fim do arquivo
        tokens.add(new Token(TipoToken.EOF, "", linha, coluna));
        return tokens;
    }

    // --- Lógica do Autômato Léxico (AFD) ---

    private Token proximoToken() {
        // 1. Ignorar espaços em branco, quebras de linha E COMENTÁRIOS.
        ignorarEspacosEQuebras();

        // Se chegamos ao fim do arquivo
        if (ponteiro >= codigoFonte.length()) {
            // Ajusta a linha/coluna para a posição após o último token (ou final real)
            return new Token(TipoToken.EOF, "Fim", linha, coluna);
        }

        char caractereAtual = codigoFonte.charAt(ponteiro);
        int colunaInicio = coluna;

        // 2. Reconhecer Identificadores ou Palavras Reservadas
        if (Character.isLetter(caractereAtual)) {
            return reconhecerIdentificadorOuReservada(colunaInicio);
        }

        // 3. Reconhecer Números (dígito+ ou .dígito+)
        if (Character.isDigit(caractereAtual) || (caractereAtual == '.' && proximoCaractereEDigito())) {
            return reconhecerNumero(colunaInicio);
        }

        // 4. Reconhecer Símbolos Especiais e Operadores
        return reconhecerSimbolos(caractereAtual, colunaInicio);
    }

    // Auxiliar para a função reconhecerNumero, para evitar tokenização incorreta de '.'
    private boolean proximoCaractereEDigito() {
        return ponteiro + 1 < codigoFonte.length() && Character.isDigit(codigoFonte.charAt(ponteiro + 1));
    }


    private void ignorarEspacosEQuebras() {
        while (ponteiro < codigoFonte.length()) {
            char c = codigoFonte.charAt(ponteiro);

            if (c == ' ' || c == '\t' || c == '\r') {
                ponteiro++;
                coluna++;
            } else if (c == '\n') {
                ponteiro++;
                linha++;
                coluna = 1; // Reinicia a coluna na nova linha
            } else if (c == '/') {
                // Checa se é o início de um comentário de linha '//'
                if (ponteiro + 1 < codigoFonte.length() && codigoFonte.charAt(ponteiro + 1) == '/') {
                    // É um comentário. Consome até o final da linha ou EOF.
                    ponteiro += 2; // Consome o '//'
                    coluna += 2;

                    while (ponteiro < codigoFonte.length()) {
                        char proximoC = codigoFonte.charAt(ponteiro);
                        if (proximoC == '\n') {
                            // Encontrou o final do comentário (que é o '\n'), consome-o
                            ponteiro++;
                            linha++;
                            coluna = 1;
                            break; // Sai do loop interno e volta para ignorarEspacosEQuebras
                        }
                        ponteiro++;
                        coluna++;
                    }
                    // Continua o loop ignorarEspacosEQuebras para checar por mais espaços/comentários
                } else {
                    // Não é um comentário, é o operador DIVISAO. Para e deixa para reconhecerSimbolos.
                    break;
                }
            } else {
                break; // Caractere significativo (não espaço/quebra/comentário) encontrado
            }
        }
    }

    private Token reconhecerIdentificadorOuReservada(int colunaInicio) {
        StringBuilder sb = new StringBuilder();

        while (ponteiro < codigoFonte.length() &&
                (Character.isLetterOrDigit(codigoFonte.charAt(ponteiro)))) {

            if (sb.length() < 10) {
                sb.append(codigoFonte.charAt(ponteiro));
                ponteiro++;
                coluna++;
            } else {
                // Ignorar caracteres após o limite de 10
                ponteiro++;
                coluna++;
            }
        }

        String lexema = sb.toString();
        // Verifica se é uma Palavra Reservada (Lookup na HashMap)
        TipoToken tipo = PALAVRAS_RESERVADAS.getOrDefault(lexema, TipoToken.IDENTIFICADOR);

        return new Token(tipo, lexema, linha, colunaInicio);
    }

    private Token reconhecerNumero(int colunaInicio) {
        StringBuilder sb = new StringBuilder();
        boolean hasDecimal = false;

        // Se começou com '.', já marca como decimal
        if (codigoFonte.charAt(ponteiro) == '.') {
            sb.append(codigoFonte.charAt(ponteiro));
            ponteiro++;
            coluna++;
            hasDecimal = true;

            // Já checado em proximoToken() que há dígitos após o ponto, mas para robustez
            if (ponteiro >= codigoFonte.length() || !Character.isDigit(codigoFonte.charAt(ponteiro))) {
                // Este caso deve ser evitado se proximoToken estiver correto, mas é um bom fallback
                return new Token(TipoToken.ERRO, "Número mal formado (ponto sem dígitos)", linha, colunaInicio);
            }
        }

        // Reconhece a parte inteira (se houver)
        while (ponteiro < codigoFonte.length() && Character.isDigit(codigoFonte.charAt(ponteiro))) {
            sb.append(codigoFonte.charAt(ponteiro));
            ponteiro++;
            coluna++;
        }

        // Verifica a parte fracionária
        if (!hasDecimal && ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '.') {
            sb.append(codigoFonte.charAt(ponteiro));
            ponteiro++;
            coluna++;
            hasDecimal = true;

            // Reconhece dígitos após o ponto
            while (ponteiro < codigoFonte.length() && Character.isDigit(codigoFonte.charAt(ponteiro))) {
                sb.append(codigoFonte.charAt(ponteiro));
                ponteiro++;
                coluna++;
            }
        }

        // Verifica se é um ponto solto, se o lexema for apenas ".", é ERRO.
        if (sb.toString().equals(".")) {
            return new Token(TipoToken.ERRO, "Ponto solto não reconhecido.", linha, colunaInicio);
        }

        return new Token(TipoToken.NUMERICO, sb.toString(), linha, colunaInicio);
    }

    private Token reconhecerSimbolos(char caractereAtual, int colunaInicio) {
        ponteiro++;
        coluna++;

        switch (caractereAtual) {
            case '$':
                // Pode ser '$' (INICIO_PROGRAMA) ou '$.' (FIM_PROGRAMA)
                if (ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '.') {
                    ponteiro++;
                    coluna++;
                    return new Token(TipoToken.FIM_PROGRAMA, "$.", linha, colunaInicio);
                }
                return new Token(TipoToken.INICIO_PROGRAMA, "$", linha, colunaInicio);
            case ';':
                return new Token(TipoToken.PONTO_VIRGULA, ";", linha, colunaInicio);
            case ',':
                return new Token(TipoToken.VIRGULA, ",", linha, colunaInicio);
            case '(':
                return new Token(TipoToken.ABRE_PARENTESES, "(", linha, colunaInicio);
            case ')':
                return new Token(TipoToken.FECHA_PARENTESES, ")", linha, colunaInicio);
            case '+':
                return new Token(TipoToken.SOMA, "+", linha, colunaInicio);
            case '*':
                return new Token(TipoToken.MULTIPLICACAO, "*", linha, colunaInicio);
            case '/':
                // CORREÇÃO: Já tratamos '//' em ignorarEspacosEQuebras(). Aqui, é apenas DIVISAO.
                // Se o Analisador Lexico não tivesse ignorado o '//', ele cairia aqui duas vezes.
                // Com o ajuste em ignorarEspacosEQuebras(), este caso só deve ser o operador de DIVISAO.
                return new Token(TipoToken.DIVISAO, "/", linha, colunaInicio);

            case '=':
                // Pode ser '==' (IGUAL) ou '=' (ATRIBUICAO)
                if (ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '=') {
                    ponteiro++;
                    coluna++;
                    return new Token(TipoToken.IGUAL, "==", linha, colunaInicio);
                }
                return new Token(TipoToken.ATRIBUICAO, "=", linha, colunaInicio);
            case '>':
                // Pode ser '>=' (MAIOR_IGUAL) ou '>' (MAIOR_QUE)
                if (ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '=') {
                    ponteiro++;
                    coluna++;
                    return new Token(TipoToken.MAIOR_IGUAL, ">=", linha, colunaInicio);
                }
                return new Token(TipoToken.MAIOR_QUE, ">", linha, colunaInicio);
            case '<':
                // Pode ser '<=' (MENOR_IGUAL) ou '<' (MENOR_QUE)
                if (ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '=') {
                    ponteiro++;
                    coluna++;
                    return new Token(TipoToken.MENOR_IGUAL, "<=", linha, colunaInicio);
                }
                return new Token(TipoToken.MENOR_QUE, "<", linha, colunaInicio);
            case '!':
                // Assumindo '!=' para DIFERENTE
                if (ponteiro < codigoFonte.length() && codigoFonte.charAt(ponteiro) == '=') {
                    ponteiro++;
                    coluna++;
                    return new Token(TipoToken.DIFERENTE, "!=", linha, colunaInicio);
                }
                // Se não for '!=', é um símbolo não reconhecido
                ponteiro--;
                coluna--;
                return new Token(TipoToken.ERRO, String.valueOf(caractereAtual), linha, colunaInicio);

            default:
                // Caractere não reconhecido
                return new Token(TipoToken.ERRO, String.valueOf(caractereAtual), linha, colunaInicio);
        }
    }
}