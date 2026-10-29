# Cadastro Service — Linketinder

Microsserviço desenvolvido em Groovy para explorar a separação do cadastro de candidatos e empresas do projeto Linketinder. A aplicação utiliza o servidor HTTP nativo do JDK, sem frameworks web.

Este repositório faz parte do desafio **K2-T3: implementar um microsserviço de cadastro para o Linketinder**.

## Origem do projeto

O microsserviço foi criado como uma aplicação Gradle independente, mas aproveita a modelagem e a persistência construídas anteriormente no repositório [Linketinder-Project](https://github.com/Athus27/Linketinder-Project).

Foram adaptados desse projeto:

- o modelo relacional do PostgreSQL, com as tabelas `User`, `Candidate`, `Company` e tabelas de competências;
- os DAOs responsáveis pelas operações JDBC;
- as fábricas de conexão com o PostgreSQL;
- o uso de transações, `PreparedStatement` e hash de senha com `pgcrypto`;
- parte dos testes de persistência e conexão.

O código foi movido para o pacote `org.ms` e está sendo integrado a uma API HTTP própria. O projeto original continua independente e representa a aplicação Linketinder anterior à separação do microsserviço.

## Tecnologias e bibliotecas

- Java 21;
- Groovy 4.0.29;
- Gradle 9.7;
- `com.sun.net.httpserver.HttpServer`, servidor HTTP nativo do JDK;
- `groovy-json`, por meio de `JsonOutput`, para produzir respostas JSON;
- PostgreSQL;
- PostgreSQL JDBC Driver 42.6.0;
- Spock 2.4 para testes;
- JUnit Platform para execução dos testes;
- Byte Buddy 1.18.3 e Objenesis 3.6 para criação de mocks de classes nos testes Spock;
- IntelliJ HTTP Client, por meio do arquivo `requests.http`, para testar os endpoints.

## Estrutura atual

```text
cadastro-service/
├── app/
│   └── src/
│       ├── main/
│       │   ├── groovy/org/ms/
│       │   │   ├── Main.groovy
│       │   │   ├── controller/
│       │   │   │   └── CandidateController.groovy
│       │   │   └── dao/
│       │   │       ├── connection/
│       │   │       ├── user/
│       │   │       └── competence/
│       │   └── resources/
│       │       └── db.properties
│       ├── test/groovy/org/ms/
│       │   └── controller/
│       └── integrationTest/groovy/org/ms/
│           └── dao/
├── requests.http
├── settings.gradle
└── gradlew
```

### Responsabilidades

- `Main`: cria o `HttpServer`, monta as dependências e registra as rotas;
- `CandidateController`: recebe o `HttpExchange`, interpreta a rota e produz a resposta HTTP;
- `CandidateDAO`: consulta e persiste candidatos usando JDBC;
- `dao/connection`: carrega as propriedades e abre conexões com o PostgreSQL;
- `dao/competence`: consulta e relaciona competências;
- `requests.http`: contém requisições para testar a API pelo IntelliJ.

## Configuração do banco

Crie localmente o arquivo `app/src/main/resources/db.properties`:

```properties
db.type=postgres
db.url=jdbc:postgresql://localhost:5432/linketinder
db.user=seu_usuario
db.password=sua_senha
```

O arquivo `db.properties` está no `.gitignore` para impedir o versionamento de credenciais.

O banco deve possuir a estrutura criada no `Linketinder-Project`. A extensão `pgcrypto` também é necessária para os comandos que armazenam senhas com hash.

## Executando

Inicie o microsserviço:

```bash
./gradlew run
# Compila o projeto e inicia o servidor em http://localhost:8080.
```

O processo precisa ser reiniciado depois de alterações no código, pois o projeto ainda não utiliza uma ferramenta de hot reload.

## Endpoints atuais

| Método | Rota | Estado | Descrição |
|---|---|---|---|
| `GET` | `/candidates` | Implementado | Lista todos os candidatos |
| `GET` | `/candidates/{id}` | Implementado | Busca um candidato pelo ID |
| `POST` | `/candidates` | Em desenvolvimento | Cadastrará um candidato |
| `PUT` | `/candidates/{id}` | Planejado | Atualizará um candidato |
| `DELETE` | `/candidates/{id}` | Planejado | Excluirá um candidato |
| `POST` | `/companies` | Planejado | Cadastrará uma empresa |

Exemplo:

```http
GET http://localhost:8080/candidates/1
```

Respostas utilizam JSON e o cabeçalho:

```text
Content-Type: application/json; charset=UTF-8
```

Quando um candidato não é encontrado, o controller responde com status `404`:

```json
{
  "erro": "Candidato não encontrado"
}
```

## Testes

Execute os testes unitários:

```bash
./gradlew test
# Executa os testes rápidos sem depender de uma conexão real com o banco.
```

Execute continuamente após alterações:

```bash
./gradlew test --continuous --console=plain
# Repete os testes quando os arquivos de código ou teste mudam.
```

Execute os testes de integração:

```bash
./gradlew integrationTest
# Testa a conexão real e exige PostgreSQL disponível e db.properties configurado.
```

Os testes unitários usam mocks para isolar o controller do banco. Os testes de integração verificam componentes que precisam de infraestrutura real, como a conexão PostgreSQL.

## Estado do desenvolvimento

Atualmente, o projeto já:

- inicia um servidor HTTP independente;
- registra o controller de candidatos na rota `/candidates`;
- lista candidatos do banco em JSON;
- busca candidato por ID;
- responde `404` quando o candidato não existe;
- possui teste unitário do comportamento de candidato inexistente;
- possui estrutura separada para testes de integração da conexão.

As próximas funcionalidades do desafio são receber JSON via `POST`, validar os dados, cadastrar candidatos e empresas e documentar a exploração da comunicação com o frontend.
