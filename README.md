# BANTADS

Lara Ono Glufke Reis
GRR 20230943

Emanuelle Rodrigues Silveira
GRR 20242389

Gabriela Morais Gandine
GRR 20234968

Cauã Henrique Dias Barbosa
GRR 20230951

Ender Efrain Ruiz Carrosco 
GRR 20250037

## Sobre

Sistema bancário baseado em microsserviços (Spring Boot), com front-end em Vue 3 e API Gateway em Node.js. Os serviços se comunicam por RabbitMQ e o serviço de orquestração usa Redis.

## Pré-requisitos

- Docker e Docker Compose v2 (`docker compose`)
- Portas livres: 80, 3000, 5432, 5672, 6379, 8081-8086, 15672 e 27017
- ~3 GB de RAM livre para os containers
- Node.js 22.18+ ou 24.12+ (apenas para rodar o front fora do Docker)

## Configuração

Crie um arquivo `.env` na raiz com as variáveis abaixo (valores à sua escolha):

```
POSTGRES_USER=  POSTGRES_PASS=
MONGO_USER=     MONGO_PASS=
RABBITMQ_USER=  RABBITMQ_PASS=
MS_AUTH_HOST=ms-auth    MS_AUTH_PORT=8081
SMTP_USER=  SMTP_PASS=
```

`SMTP_USER`/`SMTP_PASS` são usados pelo `ms-email` (servidor `smtp.gmail.com:465`, defina uma senha de app do Gmail).

## Executando

```bash
./start.sh                 # build das imagens + sobe todos os containers
docker compose ps          # confere se os serviços estão healthy
docker compose logs -f ms-client   # acompanha os logs de um serviço
docker compose down        # para tudo (adicione -v para apagar os dados)
```

## Endereços

| Serviço            | URL                           |
|--------------------|-------------------------------|
| Front-end          | http://localhost              |
| API Gateway        | http://localhost:3000         |
| RabbitMQ (painel)  | http://localhost:15672        |
| PostgreSQL         | localhost:5432                |

Os bancos PostgreSQL (`client_db`, etc.) são criados automaticamente por `database/init_db.sh` na primeira subida. Para recriá-los, rode `docker compose down -v`.

## Desenvolvimento do front-end

```bash
cd front
npm install
npm run dev        # servidor Vite com hot reload
npm run build      # type-check + build de produção
```
