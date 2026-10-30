# Cadastro Service — Linketinder

Microsserviço REST para cadastro e gerenciamento de candidatos e empresas do **Linketinder**. Foi desenvolvido em Groovy, sem frameworks web, utilizando o `HttpServer` nativo do JDK e persistência com JDBC.

O projeto faz parte do desafio **K2-T3** e funciona como uma aplicação independente, conectada ao mesmo banco PostgreSQL do Linketinder.

## Tecnologias

- Java 21 e Groovy 4
- Gradle
- `HttpServer` do JDK
- PostgreSQL e JDBC
- `groovy-json`
- Spock e JUnit Platform

## Configuração do banco

Crie o arquivo `app/src/main/resources/db.properties`:

```properties
db.type=postgres
db.url=jdbc:postgresql://localhost:5432/linketinder
db.user=seu_usuario
db.password=sua_senha
```

O arquivo está no `.gitignore` para evitar o versionamento de credenciais. O banco deve utilizar a estrutura do Linketinder e ter a extensão `pgcrypto` habilitada para o hash das senhas.

## Executando

```bash
./gradlew run
# Compila o projeto e inicia a API em http://localhost:8080.
```

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/candidates` | Lista candidatos |
| `GET` | `/candidates/{id}` | Busca um candidato |
| `POST` | `/candidates` | Cadastra um candidato |
| `PUT` | `/candidates/{id}` | Atualiza um candidato |
| `DELETE` | `/candidates/{id}` | Remove um candidato |
| `GET` | `/companies` | Lista empresas |
| `GET` | `/companies/{id}` | Busca uma empresa |
| `POST` | `/companies` | Cadastra uma empresa |
| `PUT` | `/companies/{id}` | Atualiza uma empresa |
| `DELETE` | `/companies/{id}` | Remove uma empresa |

As requisições e respostas utilizam JSON. O arquivo [`requests.http`](requests.http) contém exemplos prontos para executar pelo HTTP Client do IntelliJ.

## Testes

```bash
./gradlew test
# Executa os testes unitários dos controllers.

./gradlew integrationTest
# Executa os testes que dependem do PostgreSQL configurado.
```

## Origem

Este microsserviço reutiliza e adapta a modelagem do banco, os DAOs, as fábricas de conexão e as transações JDBC do projeto [Linketinder-Project](https://github.com/Athus27/Linketinder-Project).

A integração com o frontend é explorada por meio de requisições HTTP com JSON. O serviço pode ser consumido pela tela de cadastro ou testado separadamente pelo arquivo `requests.http`.
