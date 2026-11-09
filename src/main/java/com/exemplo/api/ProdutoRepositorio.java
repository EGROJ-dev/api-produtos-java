package com.exemplo.api;

import com.exemplo.api.db.Banco;
import com.exemplo.api.db.ErroDeBanco;
import com.exemplo.api.model.Produto;
import com.exemplo.api.model.ProdutoEntrada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD (criar, ler, atualizar, apagar) da tabela produto com JDBC. */
public class ProdutoRepositorio {

    private final Banco banco;

    public ProdutoRepositorio(Banco banco) {
        this.banco = banco;
    }

    public List<Produto> listar() {
        try (Connection c = banco.abrir();
             PreparedStatement ps = c.prepareStatement("SELECT id, nome, preco, estoque FROM produto ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Produto> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao listar produtos.", e);
        }
    }

    public Optional<Produto> buscar(int id) {
        try (Connection c = banco.abrir();
             PreparedStatement ps = c.prepareStatement("SELECT id, nome, preco, estoque FROM produto WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao buscar produto.", e);
        }
    }

    public Produto inserir(ProdutoEntrada entrada) {
        String sql = "INSERT INTO produto (nome, preco, estoque) VALUES (?, ?, ?)";
        try (Connection c = banco.abrir();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entrada.nome());
            ps.setBigDecimal(2, entrada.preco());
            ps.setInt(3, entrada.estoque());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return new Produto(chaves.getInt(1), entrada.nome(), entrada.preco(), entrada.estoque());
            }
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao inserir produto.", e);
        }
    }

    /** Devolve o produto atualizado, ou vazio se o id não existir. */
    public Optional<Produto> atualizar(int id, ProdutoEntrada entrada) {
        String sql = "UPDATE produto SET nome = ?, preco = ?, estoque = ? WHERE id = ?";
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entrada.nome());
            ps.setBigDecimal(2, entrada.preco());
            ps.setInt(3, entrada.estoque());
            ps.setInt(4, id);
            int linhas = ps.executeUpdate();
            return linhas == 0
                    ? Optional.empty()
                    : Optional.of(new Produto(id, entrada.nome(), entrada.preco(), entrada.estoque()));
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao atualizar produto.", e);
        }
    }

    public boolean remover(int id) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM produto WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroDeBanco("Erro ao remover produto.", e);
        }
    }

    private static Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(rs.getInt("id"), rs.getString("nome"), rs.getBigDecimal("preco"), rs.getInt("estoque"));
    }
}
