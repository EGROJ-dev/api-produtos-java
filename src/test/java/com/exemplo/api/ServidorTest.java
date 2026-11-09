package com.exemplo.api;

import com.exemplo.api.db.Banco;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Testes de ponta a ponta: sobem o servidor de verdade e fazem requisições HTTP reais. */
class ServidorTest {

    private Servidor servidor;
    private HttpClient cliente;
    private String base;

    @BeforeEach
    void subirServidor() throws Exception {
        Banco banco = Banco.emMemoria("teste" + UUID.randomUUID().toString().replace("-", ""));
        servidor = new Servidor(0, banco); // porta 0 = o sistema escolhe uma porta livre
        servidor.iniciar();
        base = "http://localhost:" + servidor.porta() + "/produtos";
        cliente = HttpClient.newHttpClient();
    }

    @AfterEach
    void pararServidor() {
        servidor.parar();
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(base + caminho))
                .header("Content-Type", "application/json");
        req.method(metodo, corpo == null ? BodyPublishers.noBody() : BodyPublishers.ofString(corpo));
        return cliente.send(req.build(), BodyHandlers.ofString());
    }

    private static final String CANETA = "{\"nome\":\"Caneta\",\"preco\":3.5,\"estoque\":10}";

    @Test
    void listaVaziaNoComeco() throws Exception {
        HttpResponse<String> r = enviar("GET", "", null);
        assertEquals(200, r.statusCode());
        assertEquals("[]", r.body());
    }

    @Test
    void criaEBuscaUmProduto() throws Exception {
        HttpResponse<String> criado = enviar("POST", "", CANETA);
        assertEquals(201, criado.statusCode());
        assertEquals("/produtos/1", criado.headers().firstValue("Location").orElse(""));
        assertEquals("{\"id\":1,\"nome\":\"Caneta\",\"preco\":3.50,\"estoque\":10}", criado.body());

        HttpResponse<String> busca = enviar("GET", "/1", null);
        assertEquals(200, busca.statusCode());
        assertEquals(criado.body(), busca.body());
    }

    @Test
    void listaOsProdutosCriados() throws Exception {
        enviar("POST", "", CANETA);
        enviar("POST", "", "{\"nome\":\"Caderno\",\"preco\":20,\"estoque\":4}");

        HttpResponse<String> r = enviar("GET", "", null);

        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"nome\":\"Caneta\""));
        assertTrue(r.body().contains("\"nome\":\"Caderno\""));
    }

    @Test
    void atualizaUmProduto() throws Exception {
        enviar("POST", "", CANETA);

        HttpResponse<String> r = enviar("PUT", "/1", "{\"nome\":\"Caneta azul\",\"preco\":4,\"estoque\":7}");

        assertEquals(200, r.statusCode());
        assertEquals("{\"id\":1,\"nome\":\"Caneta azul\",\"preco\":4.00,\"estoque\":7}", r.body());
        assertTrue(enviar("GET", "/1", null).body().contains("Caneta azul"));
    }

    @Test
    void removeUmProduto() throws Exception {
        enviar("POST", "", CANETA);

        assertEquals(204, enviar("DELETE", "/1", null).statusCode());
        assertEquals(404, enviar("GET", "/1", null).statusCode());
    }

    @Test
    void devolve404ParaIdInexistente() throws Exception {
        assertEquals(404, enviar("GET", "/99", null).statusCode());
        assertEquals(404, enviar("PUT", "/99", CANETA).statusCode());
        assertEquals(404, enviar("DELETE", "/99", null).statusCode());
    }

    @Test
    void devolve400ParaDadosInvalidos() throws Exception {
        assertEquals(400, enviar("POST", "", "{\"preco\":3,\"estoque\":1}").statusCode());
        assertEquals(400, enviar("POST", "", "{\"nome\":\"X\",\"preco\":-1,\"estoque\":1}").statusCode());
        assertEquals(400, enviar("POST", "", "{\"nome\":\"X\",\"preco\":1,\"estoque\":1.5}").statusCode());
        assertEquals(400, enviar("POST", "", "isto não é json").statusCode());
        assertEquals(400, enviar("GET", "/abc", null).statusCode());
        assertTrue(enviar("GET", "", null).body().equals("[]"));
    }

    @Test
    void devolve405ParaMetodoNaoPermitido() throws Exception {
        HttpResponse<String> r = enviar("PATCH", "/1", "{}");
        assertEquals(405, r.statusCode());
        assertTrue(r.headers().firstValue("Allow").isPresent());
    }

    @Test
    void preservaAcentosENomesComAspas() throws Exception {
        HttpResponse<String> r = enviar("POST", "",
                "{\"nome\":\"Pão \\\"francês\\\"\",\"preco\":0.75,\"estoque\":100}");

        assertEquals(201, r.statusCode());
        assertTrue(r.body().contains("\"nome\":\"Pão \\\"francês\\\"\""));
    }
}
