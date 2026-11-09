package com.exemplo.api.http;

/** Erro que vira uma resposta HTTP com o status indicado (400, 404, 405...). */
public class ErroDeRequisicao extends RuntimeException {

    private final int status;

    public ErroDeRequisicao(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
