package com.exemplo.api.model;

import com.exemplo.api.http.ErroDeRequisicao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** Dados que o cliente envia para criar ou alterar um produto, já validados. */
public record ProdutoEntrada(String nome, BigDecimal preco, int estoque) {

    public static ProdutoEntrada de(Map<String, Object> json) {
        if (!(json.get("nome") instanceof String nome) || nome.isBlank()) {
            throw new ErroDeRequisicao(400, "O campo 'nome' é obrigatório.");
        }
        if (nome.length() > 100) {
            throw new ErroDeRequisicao(400, "O campo 'nome' pode ter no máximo 100 caracteres.");
        }
        if (!(json.get("preco") instanceof BigDecimal preco) || preco.signum() < 0) {
            throw new ErroDeRequisicao(400, "O campo 'preco' é obrigatório e não pode ser negativo.");
        }
        int estoque;
        try {
            if (!(json.get("estoque") instanceof BigDecimal bruto)) {
                throw new ErroDeRequisicao(400, "O campo 'estoque' é obrigatório.");
            }
            estoque = bruto.intValueExact();
        } catch (ArithmeticException e) {
            throw new ErroDeRequisicao(400, "O campo 'estoque' precisa ser um número inteiro.");
        }
        if (estoque < 0) {
            throw new ErroDeRequisicao(400, "O campo 'estoque' não pode ser negativo.");
        }
        return new ProdutoEntrada(nome.trim(), preco.setScale(2, RoundingMode.HALF_UP), estoque);
    }
}
