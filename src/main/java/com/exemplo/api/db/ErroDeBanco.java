package com.exemplo.api.db;

public class ErroDeBanco extends RuntimeException {
    public ErroDeBanco(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
