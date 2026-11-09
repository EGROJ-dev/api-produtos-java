package com.exemplo.api.http;

import com.exemplo.api.model.Produto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitura e escrita de JSON feitas à mão, só para fins didáticos.
 * Lê objetos simples (texto, número, true, false, null). Em projetos reais, use Jackson ou Gson.
 */
public final class Json {

    private Json() {}

    // ---------- escrita ----------

    public static String produto(Produto p) {
        return "{\"id\":" + p.id()
                + ",\"nome\":" + texto(p.nome())
                + ",\"preco\":" + p.preco().toPlainString()
                + ",\"estoque\":" + p.estoque() + "}";
    }

    public static String produtos(List<Produto> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(produto(lista.get(i)));
        }
        return sb.append(']').toString();
    }

    public static String erro(String mensagem) {
        return "{\"erro\":" + texto(mensagem) + "}";
    }

    public static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    // ---------- leitura ----------

    /** Converte {"a": "texto", "b": 1.5} em um Map (números viram BigDecimal). */
    public static Map<String, Object> lerObjeto(String json) {
        Leitor leitor = new Leitor(json);
        leitor.pularEspacos();
        leitor.esperar('{');
        Map<String, Object> mapa = new LinkedHashMap<>();
        leitor.pularEspacos();
        if (leitor.proximoE('}')) {
            leitor.fim();
            return mapa;
        }
        do {
            leitor.pularEspacos();
            String chave = leitor.lerTexto();
            leitor.pularEspacos();
            leitor.esperar(':');
            leitor.pularEspacos();
            mapa.put(chave, leitor.lerValor());
            leitor.pularEspacos();
        } while (leitor.proximoE(','));
        leitor.esperar('}');
        leitor.fim();
        return mapa;
    }

    private static final class Leitor {
        private final String s;
        private int i = 0;

        Leitor(String s) {
            this.s = s;
        }

        void pularEspacos() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        boolean proximoE(char c) {
            pularEspacos();
            if (i < s.length() && s.charAt(i) == c) {
                i++;
                return true;
            }
            return false;
        }

        void esperar(char c) {
            if (i >= s.length() || s.charAt(i) != c) {
                throw invalido();
            }
            i++;
        }

        void fim() {
            pularEspacos();
            if (i != s.length()) {
                throw invalido();
            }
        }

        String lerTexto() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (i >= s.length()) {
                    break;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 > s.length()) {
                            throw invalido();
                        }
                        try {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw invalido();
                        }
                        i += 4;
                    }
                    default -> throw invalido();
                }
            }
            throw invalido();
        }

        Object lerValor() {
            if (i >= s.length()) {
                throw invalido();
            }
            if (s.charAt(i) == '"') {
                return lerTexto();
            }
            int inicio = i;
            while (i < s.length() && ",}".indexOf(s.charAt(i)) < 0) {
                i++;
            }
            String bruto = s.substring(inicio, i).trim();
            return switch (bruto) {
                case "null" -> null;
                case "true" -> Boolean.TRUE;
                case "false" -> Boolean.FALSE;
                default -> {
                    try {
                        yield new BigDecimal(bruto);
                    } catch (NumberFormatException e) {
                        throw invalido();
                    }
                }
            };
        }

        private ErroDeRequisicao invalido() {
            return new ErroDeRequisicao(400, "JSON inválido.");
        }
    }
}
