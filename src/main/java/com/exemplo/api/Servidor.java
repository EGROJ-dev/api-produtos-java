package com.exemplo.api;

import com.exemplo.api.db.Banco;
import com.exemplo.api.http.ErroDeRequisicao;
import com.exemplo.api.http.Json;
import com.exemplo.api.model.Produto;
import com.exemplo.api.model.ProdutoEntrada;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * API REST de produtos usando só o servidor HTTP que já vem no JDK.
 *
 * GET    /produtos        lista todos
 * POST   /produtos        cria um produto
 * GET    /produtos/{id}   busca um produto
 * PUT    /produtos/{id}   altera um produto
 * DELETE /produtos/{id}   apaga um produto
 */
public class Servidor {

    private static final int LIMITE_CORPO_BYTES = 64 * 1024;

    private final HttpServer http;
    private final ProdutoRepositorio repositorio;

    public Servidor(int porta, Banco banco) throws IOException {
        this.repositorio = new ProdutoRepositorio(banco);
        // só aceita conexões da própria máquina (localhost)
        this.http = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), porta), 0);
        this.http.createContext("/produtos", this::tratar);
    }

    public void iniciar() {
        http.start();
    }

    public void parar() {
        http.stop(0);
    }

    public int porta() {
        return http.getAddress().getPort();
    }

    private void tratar(HttpExchange troca) throws IOException {
        try {
            rotear(troca);
        } catch (ErroDeRequisicao e) {
            responder(troca, e.status(), Json.erro(e.getMessage()));
        } catch (RuntimeException e) {
            responder(troca, 500, Json.erro("Erro interno do servidor."));
        } finally {
            troca.close();
        }
    }

    private void rotear(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String[] partes = troca.getRequestURI().getPath().split("/"); // ["", "produtos", "{id}"]
        if (partes.length > 3) {
            throw new ErroDeRequisicao(404, "Rota não encontrada.");
        }

        if (partes.length == 2) { // /produtos
            switch (metodo) {
                case "GET" -> responder(troca, 200, Json.produtos(repositorio.listar()));
                case "POST" -> {
                    ProdutoEntrada entrada = lerEntrada(troca);
                    Produto criado = repositorio.inserir(entrada);
                    troca.getResponseHeaders().add("Location", "/produtos/" + criado.id());
                    responder(troca, 201, Json.produto(criado));
                }
                default -> metodoNaoPermitido(troca, "GET, POST");
            }
            return;
        }

        int id = lerId(partes[2]); // /produtos/{id}
        switch (metodo) {
            case "GET" -> responder(troca, 200, Json.produto(
                    repositorio.buscar(id).orElseThrow(() -> naoEncontrado(id))));
            case "PUT" -> responder(troca, 200, Json.produto(
                    repositorio.atualizar(id, lerEntrada(troca)).orElseThrow(() -> naoEncontrado(id))));
            case "DELETE" -> {
                if (!repositorio.remover(id)) {
                    throw naoEncontrado(id);
                }
                responder(troca, 204, null);
            }
            default -> metodoNaoPermitido(troca, "GET, PUT, DELETE");
        }
    }

    private static int lerId(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new ErroDeRequisicao(400, "O id precisa ser um número.");
        }
    }

    private static ErroDeRequisicao naoEncontrado(int id) {
        return new ErroDeRequisicao(404, "Produto " + id + " não encontrado.");
    }

    private static ProdutoEntrada lerEntrada(HttpExchange troca) throws IOException {
        byte[] corpo = troca.getRequestBody().readNBytes(LIMITE_CORPO_BYTES + 1);
        if (corpo.length > LIMITE_CORPO_BYTES) {
            throw new ErroDeRequisicao(413, "Corpo da requisição grande demais.");
        }
        return ProdutoEntrada.de(Json.lerObjeto(new String(corpo, StandardCharsets.UTF_8)));
    }

    private void metodoNaoPermitido(HttpExchange troca, String permitidos) throws IOException {
        troca.getResponseHeaders().add("Allow", permitidos);
        responder(troca, 405, Json.erro("Método não permitido."));
    }

    private static void responder(HttpExchange troca, int status, String corpo) throws IOException {
        if (corpo == null) {
            troca.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        troca.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        troca.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = troca.getResponseBody()) {
            saida.write(bytes);
        }
    }
}
