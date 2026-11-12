# Spring Security JWT (flat)

[![Licença](https://img.shields.io/github/license/santosediego/spring-security-jwt-flat?style=for-the-badge)](https://github.com/santosediego/spring-security-jwt-flat/blob/main/LICENSE)

![Java](https://img.shields.io/badge/java_25-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring_boot_3.5-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/spring_security-%236DB33F.svg?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![Maven](https://img.shields.io/badge/maven-%23C71A36.svg?style=for-the-badge&logo=apache-maven&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-%23406BA5.svg?style=for-the-badge&logo=h2-database&logoColor=white)

## Sobre o projeto

API REST de autenticação e gerenciamento de usuários, feita com **Spring Boot**, **Spring Security** e **JWT**. É um projeto de estudo com foco na autenticação por token, como alternativa mais enxuta ao OAuth2.

O nome "flat" vem dos papéis: existem apenas **ADMIN** e **USER**, fixos e sem hierarquia configurável. A versão com hierarquia de papéis é o projeto `spring-security-jwt-role-hierarchy`.

## Como funciona o acesso

- **Login:** `POST /auth/login` recebe usuário e senha e devolve um token JWT, que deve ser enviado no header `Authorization: Bearer <token>`.
- **ADMIN** (`ADM`) gerencia todos os usuários: lista, cadastra, edita, exclui e altera papel e situação (ativo ou inativo).
- **USER** (`USR`) só consulta e edita **o próprio cadastro**, e apenas nome e e-mail.
- O token carrega só o nome de usuário. O papel é lido do banco **a cada requisição**, então uma mudança de papel vale na hora.
- Usuário **inativo perde o acesso imediatamente**: o token dele passa a ser recusado, mesmo ainda dentro da validade.
- Sempre deve existir **ao menos um administrador ativo**. Rebaixar, desativar ou excluir o último admin ativo é recusado.
- Se um USER enviar `role` ou `active` na edição, os campos são **ignorados** (a resposta é 200 e nada muda).

## Endpoints

| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| POST | `/auth/login` | Autentica e devolve o token | Público |
| GET | `/users` | Lista usuários com filtros e paginação | ADMIN |
| GET | `/users/{id}` | Busca um usuário | ADMIN ou o próprio usuário |
| POST | `/users` | Cadastra um usuário | ADMIN |
| PUT | `/users/{id}` | Atualiza um usuário | ADMIN ou o próprio usuário |
| DELETE | `/users/{id}` | Exclui um usuário | ADMIN |

**Filtros da listagem** (`GET /users`): `name`, `username`, `email`, `active`, `role`, além de `page`, `size`, `direction` e `orderBy`.

**Valores de `role`:** `ADM` (administrador) e `USR` (usuário comum).

**Cadastro (`POST /users`):** `name` (2 a 120 caracteres), `username` (2 a 120 caracteres, único), `password`, `role` e, opcionalmente, `email` (único) e `active` (padrão `true`).

**Atualização (`PUT /users/{id}`):** `name` é obrigatório. `email`, `role` e `active` são opcionais, e o campo que não for enviado **mantém o valor atual**.

### Respostas de erro

| Status | Quando acontece |
|--------|-----------------|
| 400 | Corpo da requisição malformado (JSON inválido) |
| 401 | Sem token, token inválido ou expirado, ou usuário inativo |
| 403 | Autenticado, mas sem permissão para a operação |
| 404 | Usuário não encontrado |
| 422 | Dados inválidos ou regra de negócio violada (e-mail repetido, último admin etc.) |

## Tecnologias utilizadas

- Java 25
- Spring Boot 3.5 (Web, Data JPA, Validation)
- Spring Security
- JWT com a biblioteca `java-jwt` (auth0)
- Liquibase (migrações do banco)
- PostgreSQL (perfil `dev`)
- H2 em memória (perfil `test`)
- Maven

## Como executar o projeto

Pré-requisito: JDK 25 (versão definida no `pom.xml`).

```bash
# clonar repositório
git clone https://github.com/santosediego/spring-security-jwt-flat.git

# entrar na pasta do projeto
cd spring-security-jwt-flat
```

### Perfil `test` (padrão): H2 em memória, sem instalar nada

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco é recriado a cada execução com os usuários de exemplo do arquivo `data.sql`, e o console do H2 fica em `http://localhost:8080/h2-console`.

### Perfil `dev`: PostgreSQL

Pré-requisito: PostgreSQL rodando em `localhost:5432` com um banco chamado `tbl_teste` (usuário e senha `postgres`, conforme o `application-dev.properties`). O Liquibase cria o schema, a tabela e o usuário administrador na primeira execução.

```bash
APP_PROFILE=dev ./mvnw spring-boot:run
```

### Variáveis de ambiente

| Variável | O que faz | Padrão |
|----------|-----------|--------|
| `APP_PROFILE` | Perfil ativo (`test` ou `dev`) | `test` |
| `JWT_SECRET` | Chave que assina os tokens | `MY-JWT-SECRET` |
| `JWT_DURATION` | Validade do token, em segundos | `86400` (24 h) |
| `JWT_ISSUER` | Emissor gravado no token | `cobaia-api` |
| `CORS_ORIGINS` | Origens permitidas, separadas por vírgula | `localhost` nas portas 8080, 3000 e 5173 |

> O valor padrão do `JWT_SECRET` serve só para estudo. Em qualquer ambiente real, defina uma chave própria e longa.

### Usuários de exemplo

Todos usam a senha `123456`.

| Usuário | Papel | Situação | Onde existe |
|---------|-------|----------|-------------|
| `admin` | ADMIN | Ativo | `test` e `dev` |
| `john.connor` | USER | Ativo | `test` |
| `anne.lewis` | USER | Inativo | `test` |

## Exemplos de uso

```bash
# login: devolve {"access_token": "..."}
curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "123456"}'

# listar usuários (ADMIN)
curl http://localhost:8080/users \
     -H "Authorization: Bearer SEU_TOKEN"

# o próprio usuário atualiza o nome
curl -X PUT http://localhost:8080/users/2 \
     -H "Authorization: Bearer SEU_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"name": "John Connor"}'
```

No Postman, o token pode ser guardado automaticamente em uma variável de ambiente, no script de *Post-response* da requisição de login:

```javascript
pm.environment.set("token", pm.response.json().access_token);
```

E usado nas outras requisições como `Bearer {{token}}`.

## Estrutura do projeto

```
src/main/java/com/santosediego/cobaia_test/
├── config/          # Segurança: filtro JWT, geração do token e regras de acesso
├── dto/             # Objetos de entrada e saída da API
├── entities/        # Entidade User e enum Role
├── repositories/    # Acesso ao banco (Spring Data JPA)
├── resources/       # Controllers REST e tratamento de erros
└── services/        # Regras de negócio, validações e exceções
src/main/resources/
├── db/changelog/    # Migrações do Liquibase
├── data.sql         # Dados de exemplo do perfil test
└── application*.properties
```

## Limitações conhecidas

- Papéis fixos, sem hierarquia. Para algo mais flexível, veja o projeto `spring-security-jwt-role-hierarchy`.
- Não há troca nem recuperação de senha. A senha é definida no cadastro.
- Não há *refresh token* nem logout: o token vale até expirar. A exceção é o usuário inativado, que perde o acesso na hora.
- O `JWT_SECRET` tem um valor padrão e o perfil padrão é o `test` (com console do H2 habilitado). Em um deploy real, defina o segredo e o perfil explicitamente.
- A regra do último administrador é checada na aplicação. Duas alterações simultâneas poderiam burlá-la.
- É um projeto de estudo. As senhas de exemplo estão no repositório de propósito e não devem ser usadas em ambiente real.

## Autor

[Diego Santos](https://www.linkedin.com/in/santosediego/ "Perfil Linkedin Diego Santos")
