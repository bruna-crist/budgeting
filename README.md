# Budgeting API — Assistente financeiro com Spring AI

API REST para registro e consulta de transações financeiras, desenvolvida em Java com Spring Boot. O projeto integra **Spring AI** para interpretar comandos enviados por áudio, utilizar ferramentas (*Tool Calling*) para acessar os casos de uso e gerar uma resposta em áudio.

Projeto desenvolvido como parte de um desafio da **DIO**, com evolução própria focada na **validação de transações e no tratamento de erros**, acompanhada de testes unitários.

## Funcionalidades

- Cadastrar transações financeiras via API REST.
- Consultar transações por categoria.
- Persistir os dados em MySQL utilizando Spring Data JPA.
- Processar áudio com transcrição, interação com modelo de linguagem e síntese de voz por meio do Spring AI.
- Expor os casos de uso de cadastro e consulta como ferramentas para o assistente de IA (*Tool Calling*).
- Validar descrição, categoria e valor antes de salvar uma transação.
- Retornar **HTTP 400** com mensagem de erro quando uma regra de validação é violada.

## Tecnologias

- Java 25
- Spring Boot 4.0.9
- Spring AI 2.0.0-M4
- Spring Web e Spring Data JPA
- MySQL 9.6
- Docker Compose
- Gradle
- JUnit 5 e Mockito

## Organização do projeto

O código está dividido em camadas:

```text
src/main/java/org/example/budgeting/
├── application/
│   ├── input/
│   ├── output/
│   ├── ListTransactionsByCategoryUseCase.java
│   └── PersistTransactionUseCase.java
├── domain/
│   ├── Category.java
│   ├── Transaction.java
│   ├── TransactionId.java
│   └── TransactionRepository.java
└── infrastructure/
    ├── http/
    │   ├── request/
    │   ├── response/
    │   ├── TransactionController.java
    │   └── TransactionExceptionHandler.java
    └── persistence/
        ├── entity/
        └── repository/
```

- **Domain:** entidades, categorias e contrato do repositório.
- **Application:** casos de uso de cadastro e consulta, incluindo validações.
- **Infrastructure:** endpoints HTTP, integração com Spring AI e implementação da persistência com JPA.

## Pré-requisitos

- JDK 25
- Docker Desktop com Docker Compose
- Uma chave de API válida da OpenAI **para utilizar o endpoint de áudio/IA**

O projeto inclui o Gradle Wrapper, portanto não é necessário instalar o Gradle separadamente.

## Como executar

1. Clone o repositório e abra a pasta do projeto.
2. Inicie o Docker Desktop.
3. Configure a variável de ambiente `OPENAI_API_KEY` com sua chave válida da OpenAI. No PowerShell, para a sessão atual:

   ```powershell
   $env:OPENAI_API_KEY="sua-chave-aqui"
   ```

   **Não adicione a chave ao código ou ao Git.** A aplicação usa a propriedade `spring.ai.openai.api-key=${OPENAI_API_KEY}`.

4. Execute a aplicação no terminal:

   **Windows (PowerShell):**
   ```powershell
   .\gradlew.bat bootRun
   ```

   **Linux/macOS:**
   ```bash
   ./gradlew bootRun
   ```

O Spring Boot possui integração com Docker Compose e utiliza o `compose.yml` do projeto para iniciar o banco MySQL. O banco é publicado na porta **3307** do computador (porta **3306** do contêiner). A API utiliza, por padrão, a porta **8080**.

> As credenciais do MySQL definidas no `compose.yml` são apenas para ambiente de desenvolvimento local. Não as reutilize em produção.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/transactions` | Cadastra uma transação |
| `GET` | `/transactions/{category}` | Lista transações de uma categoria |
| `POST` | `/transactions/ai` | Recebe áudio e devolve uma resposta em MP3, utilizando Spring AI |

### Cadastrar transação

**Requisição**

```http
POST http://localhost:8080/transactions
Content-Type: application/json
```

```json
{
  "description": "Compras no supermercado",
  "category": "GROCERIES",
  "amount": 150
}
```

O cadastro bem-sucedido retorna **HTTP 201 Created**, com os dados da transação, incluindo o identificador gerado.

**Categorias disponíveis:** `GROCERIES`, `PHARMA` e `AUTO`.

**Atenção à unidade do valor:** o campo `amount` é do tipo `long` no domínio. A implementação atual devolve esse número na resposta sem conversão de centavos para reais. Portanto, envie e interprete o valor de maneira consistente com essa implementação; por exemplo, `150` é retornado como `150.0`, não como `1.50`.

### Consultar por categoria

```http
GET http://localhost:8080/transactions/GROCERIES
```

Retorna uma lista JSON de transações da categoria informada.

### Processar áudio com IA

```http
POST http://localhost:8080/transactions/ai
Content-Type: multipart/form-data
```

No Postman, selecione **Body → form-data**, crie o campo `file` do tipo **File** e anexe um arquivo de áudio. A aplicação transcreve o áudio, envia o texto ao assistente configurado com ferramentas de cadastro/consulta e devolve uma resposta como arquivo **MP3**.

Esse endpoint requer uma chave válida da OpenAI e acesso aos modelos configurados em `application.properties`.

## Melhoria implementada — validação de transações

O caso de uso `PersistTransactionUseCase` verifica os dados antes de acionar o repositório:

| Campo | Regra |
|---|---|
| `description` | Não pode ser nula ou vazia |
| `amount` | Deve ser maior que zero |
| `category` | Não pode ser nula |

Como a validação está na camada de aplicação, ela também protege o fluxo de cadastro acionado por *Tool Calling*.

O `TransactionExceptionHandler` trata `IllegalArgumentException` e devolve **HTTP 400 Bad Request** com uma mensagem descritiva. Exemplo de requisição inválida:

```json
{
  "description": "Compras no supermercado",
  "category": "GROCERIES",
  "amount": -150
}
```

**Resposta:**

```json
{
  "error": "Valor da transação deve ser maior que zero"
}
```

## Testes

O projeto inclui testes unitários com **JUnit 5** e **Mockito** para verificar que transações com valor negativo ou descrição vazia são rejeitadas e que o repositório não é acionado nesses casos.

Para executar apenas os testes de validação:

```powershell
.\gradlew.bat test --tests "org.example.budgeting.application.PersistTransactionUseCaseTest"
```

Para executar a suíte de testes configurada no projeto:

```powershell
.\gradlew.bat test
```

> Alguns testes de integração com modelos OpenAI dependem de configuração e credenciais externas; a execução da suíte completa pode exigir ambiente preparado.

## Próximas evoluções possíveis

- Ampliar os cenários de testes automatizados.
- Melhorar a representação de valores monetários na API.
- Documentar os endpoints com OpenAPI/Swagger.

## Contexto

Projeto de estudo e prática de desenvolvimento back-end, persistência de dados e integração de IA generativa com aplicações Java, desenvolvido para um desafio da DIO.
