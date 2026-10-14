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

Disciplina:
DAC

2026
UFPR

## Configuração

Crie um arquivo `.env` na raiz com as variáveis abaixo (valores à sua escolha):

```
POSTGRES_USER=  POSTGRES_PASS=
MONGO_USER=     MONGO_PASS=
RABBITMQ_USER=  RABBITMQ_PASS=
MS_AUTH_HOST=ms-auth    MS_AUTH_PORT=8081
SMTP_USER=  SMTP_PASS=
```

`SMTP_USER`/`SMTP_PASS` são usados pelo `ms-email` (servidor `smtp.gmail.com:465`, a ser fornecido posteriormente).


## Executando

```bash
./start.sh                 # build das imagens + sobe todos os containers

docker compose ps          # confere se os serviços estão healthy

docker compose logs -f ms-client   # acompanha os logs de um serviço

docker compose down        # para tudo (adicione -v para apagar os dados)
```
