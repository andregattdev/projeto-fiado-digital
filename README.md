# Projeto Fiado Digital

Sistema web para controle de fiado (caderneta digital) em comércios de bairro: cadastro de clientes, vendas a prazo com itens, pagamentos parciais, dashboard e relatórios.

## Estrutura

| Pasta | Descrição |
|-------|-----------|
| `backend/` | API REST — Spring Boot 3.4, Java 21, PostgreSQL |
| `frontend/projeto-fiado-frontend/` | Interface — Angular 21, Angular Material |

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou use `./mvnw` dentro de `backend/`)
- Node.js 20+ e npm
- PostgreSQL com banco `api-fiado`

## Banco de dados

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE "api-fiado";
```

As credenciais padrão estão em `backend/src/main/resources/application.properties`. Ajuste `spring.datasource.*` conforme seu ambiente local.

## Executar o backend

```bash
cd backend
./mvnw spring-boot:run
```

API disponível em `http://localhost:8080`.

## Executar o frontend

```bash
cd frontend/projeto-fiado-frontend
npm install
npm start
```

Aplicação em `http://localhost:4200`.

A URL da API é configurada em `frontend/projeto-fiado-frontend/src/environments/environment.ts` (padrão: `http://localhost:8080/api`).

## Funcionalidades

- **Clientes** — cadastro, busca, edição e exclusão
- **Vendas fiado** — múltiplos itens por venda, cancelamento (soft delete)
- **Pagamentos** — parciais, com validação de saldo devedor
- **Dashboard** — totais, devedores, dívidas vencidas (+30 dias), últimas vendas
- **Relatórios** — fechamento mensal, quem deve e há quanto tempo, vendas por período, impressão

## Endpoints principais

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/dashboard` | Indicadores do painel |
| GET | `/api/relatorios/mensal?mes=&ano=` | Relatório mensal |
| GET | `/api/relatorios/dividas-vencidas?dias=` | Dívidas vencidas |
| GET | `/api/vendas/periodo?inicio=&fim=` | Vendas por período |
| CRUD | `/api/clientes` | Clientes |
| POST | `/api/vendas` | Nova venda |
| POST | `/api/pagamentos` | Registrar pagamento |

## Testes

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend/projeto-fiado-frontend
npm test
```

## Configuração

| Propriedade | Descrição | Padrão |
|-------------|-----------|--------|
| `fiado.dias-vencimento` | Dias sem quitar para considerar dívida vencida | `30` |

# projeto-fiado-digital
Projeto de uma api de cadastramento de devedores
