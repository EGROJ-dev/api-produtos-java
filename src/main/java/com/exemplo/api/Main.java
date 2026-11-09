package com.exemplo.api;

import com.exemplo.api.db.Banco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Files.createDirectories(Path.of("dados"));

        Servidor servidor = new Servidor(porta, Banco.emArquivo(Path.of("dados", "produtos")));
        servidor.iniciar();
        System.out.println("API no ar em http://localhost:" + servidor.porta() + "/produtos (Ctrl+C para parar)");
    }
}
