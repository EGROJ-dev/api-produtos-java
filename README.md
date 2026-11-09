# 🛒 API de Produtos em Java

![CI](https://github.com/EGROJ-dev/api-produtos-java/actions/workflows/ci.yml/badge.svg)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Licença MIT](https://img.shields.io/badge/licença-MIT-blue)

API REST de produtos feita com **Java 17 puro**: usa o servidor HTTP que já vem no JDK, **JDBC** para falar com o banco **H2** e uma leitura de JSON escrita à mão. Sem Spring, sem frameworks.

É um projeto de estudo para entender **o que um framework faz por baixo dos panos**: rotas, códigos de status HTTP, validação de entrada e acesso a banco.

## Objetivo

Entender **o que um framework faz por baixo dos panos**, construindo uma API REST só com o que já vem no JDK: servidor HTTP, acesso a banco, validação de dados e respostas com os códigos de status corretos.

## Funcionalidades

- Cadastrar, listar, buscar, alterar e apagar produtos
- Validação dos dados enviados (nome, preço e estoque), com mensagens de erro em JSON
- Respostas com os códigos HTTP adequados (`200`, `201`, `204`, `400`, `404`, `405`)
- Dados gravados em banco SQL, que continuam ali depois de reiniciar a API

## Endpoints

| Método | Rota | O que faz | Resposta |
|---|---|---|---|
| `GET` | `/produtos` | Lista todos | `200` |
| `POST` | `/produtos` | Cria um produto | `201` + cabeçalho `Location` |
| `GET` | `/produtos/{id}` | Busca um produto | `200` ou `404` |
| `PUT` | `/produtos/{id}` | Altera um produto | `200` ou `404` |
| `DELETE` | `/produtos/{id}` | Apaga um produto | `204` ou `404` |

Dados inválidos devolvem `400` com uma mensagem, como `{"erro":"O campo 'nome' é obrigatório."}`. Métodos não suportados devolvem `405`.

Formato de um produto:

```json
{ "nome": "Café", "preco": 18.90, "estoque": 12 }
```

## Tecnologias

| Tecnologia | Para que serve no projeto |
|---|---|
| Java 17 | Linguagem do projeto |
| `com.sun.net.httpserver` | Servidor HTTP que já vem no JDK |
| JDBC + H2 | Acesso a um banco SQL que roda dentro do programa |
| Maven | Compilação e gerenciamento de dependências |
| JUnit 5 + `HttpClient` | Testes de ponta a ponta, com requisições HTTP reais |
| GitHub Actions | Integração contínua: roda os testes a cada alteração |

## Conceitos praticados

| Conceito | Onde ver |
|---|---|
| Servidor HTTP com `com.sun.net.httpserver` | `Servidor.java` |
| Rotas, métodos HTTP e códigos de status | `Servidor.rotear` |
| CRUD com JDBC e `PreparedStatement` | `ProdutoRepositorio.java` |
| Validação de entrada e erros em JSON | `ProdutoEntrada` e `ErroDeRequisicao` |
| Leitura e escrita de JSON sem biblioteca | `http/Json.java` |
| Testes de ponta a ponta com `HttpClient` | `ServidorTest.java` |
| Servidor ouvindo só em `localhost` por segurança | `Servidor` (construtor) |

## Como executar

Requisitos: JDK 17+ e Maven 3.8+.

```bash
git clone https://github.com/EGROJ-dev/api-produtos-java.git
cd api-produtos-java
mvn clean package
java -jar target/api-produtos-java-1.0.0.jar
```

A API sobe em `http://localhost:8080/produtos` (para outra porta: `java -jar target/api-produtos-java-1.0.0.jar 9000`). Os dados ficam na pasta `dados/`.

## Exemplos com curl

No Git Bash ou no Linux/macOS:

```bash
# criar
curl -i -X POST localhost:8080/produtos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Café","preco":18.9,"estoque":12}'

# listar
curl localhost:8080/produtos

# alterar
curl -X PUT localhost:8080/produtos/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Café torrado","preco":21,"estoque":8}'

# apagar
curl -X DELETE localhost:8080/produtos/1
```

Resposta do primeiro comando:

```
HTTP/1.1 201 Created
Location: /produtos/1

{"id":1,"nome":"Café","preco":18.90,"estoque":12}
```

## Testes

```bash
mvn test
```

São 9 testes que sobem o servidor de verdade (em uma porta livre) e fazem requisições HTTP reais, cobrindo criação, busca, alteração, remoção, erros `400`, `404` e `405` e acentos.

## Limitações (de propósito)

- O leitor de JSON só entende objetos simples. Em um projeto real, use Jackson ou Gson.
- Sem autenticação.
- Sem paginação na listagem.

## Próximos passos

- [ ] Migrar para Spring Boot e comparar com esta versão
- [ ] Paginação e busca por nome
- [ ] Autenticação com token
- [ ] Documentar a API com OpenAPI

## Autor

**Jorge Gabriel Cordeiro do Nascimento**
[LinkedIn](https://www.linkedin.com/in/jorge-gabriel-cordeiro-9b25702b) · [GitHub](https://github.com/EGROJ-dev)

## Licença

Distribuído sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
