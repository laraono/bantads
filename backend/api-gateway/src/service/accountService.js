const axios = require("axios");

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || "ms-account";
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || "8083";
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:3000";

// monta os _links com os paths públicos do gateway (/contas/...), não os do ms-account
function mapLinks(numero, gatewayUrl = GATEWAY_URL) {
  const base = `${gatewayUrl}/contas/${numero}`;
  return {
    self: { href: base },
    extrato: { href: `${base}/extrato` },
    depositar: { href: `${base}/deposito` },
    sacar: { href: `${base}/saque` },
    transferir: { href: `${base}/transferencia` },
  };
}

function rethrow(err) {
  if (err.response) {
    const data = err.response.data;
    const message =
      (typeof data === "object" && (data?.detail || data?.message)) ||
      err.message;
    const error = new Error(message);
    error.status = err.response.status;
    throw error;
  }
  throw err;
}

class AccountService {
  async listAccounts(cpf) {
    try {
      const params = new URLSearchParams();
      if (cpf) params.append("cpf", cpf);
      const query = params.toString() ? `?${params}` : "";
      const accounts = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts${query}`,
      );
      return accounts.data;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

  async transfer(id, body, headers) {
    try {
      const msBody = { contaDestino: body.contaDestino, valor: body.valor };

      const destinationCPF = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${msBody.contaDestino}/cpf`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
        },
      );

      const cpf = destinationCPF.data;

      if (!cpf) {
        const error = new Error("Conta não existe");
        error.status = 422;
        throw error;
      }

      const originClient = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${headers["x-user-cpf"]}`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
        },
      );

      if (!originClient.data?.name) {
        const error = new Error("Usuário não encontrado");
        error.status = 404;
        throw error;
      }

      const destinationClient = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${cpf}`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
        },
      );

      if (!destinationClient.data?.name) {
        const error = new Error("Usuário não existe");
        error.status = 422;
        throw error;
      }

      const payload = {
        destino: {
          conta: msBody.contaDestino,
          nome: destinationClient.data.name,
          cpf,
        },
        origem: {
          nome: originClient.data.name,
          valor: msBody.valor,
        },
      };

      const transferAnswer = await axios.post(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/transfer`,
        {
          payload,
        },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      if (!transferAnswer.data) {
        const error = new Error("Transferência não finalizada");
        error.status = 500;
        throw error;
      }

      return transferAnswer.data;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

  async withdraw(id, body, headers) {
    try {
      const withdrawAnswer = await axios.post(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/withdraw`,
        { payload: body },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      return withdrawAnswer.data;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

  async deposit(id, body, headers) {
    try {
      if (
        typeof body.valor !== "string" ||
        !/^\d+(\.\d{1,2})?$/.test(body.valor) // filtra para strings em forma de dinheiro
      ) {
        const error = new Error("Valor inválido");
        error.status = 400;
        throw error;
      }

      // o ms-account exige nome e cpf de quem deposita (vão para o histórico)
      const cpf = headers["x-user-cpf"];
      const client = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${cpf}`,
      );

      const depositAnswer = await axios.post(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/deposit`,
        {
          payload: {
            valor: body.valor,
            nome: client.data.name,
            cpfCliente: cpf,
          },
        },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      return depositAnswer.data;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

    async getAccount(id, headers) {
        try {
            const accountAnswer = await axios.get(`http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}`, {
                headers: { 'x-user-cpf': headers['x-user-cpf']}
            })

            if(!accountAnswer.data) {
                const error = new Error('Conta não encontrado');
                error.status = 404;
                throw error;
            }

      if (
        !accountAnswer.data.numero ||
        !accountAnswer.data.saldo ||
        !accountAnswer.data.cpfCliente
      ) {
        const error = new Error("Erro no servidor");
        error.status = 500;
        throw error;
      }

      const result = {
        numero: accountAnswer.data.numero,
        cpfCliente: accountAnswer.data.cpfCliente,
        saldo: accountAnswer.data.saldo,
        cpfGerente: accountAnswer.data.cpfGerente,
        _links: mapLinks(accountAnswer.data.numero),
      };

      return result;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

  async getAccountByCpf(cpf, headers) {
    try {
      const accountAnswer = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/cpf/${cpf}`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
        },
      );

      if (!accountAnswer.data) {
        const error = new Error("Conta não encontrado");
        error.status = 404;
        throw error;
      }

      if (
        !accountAnswer.data.numero ||
        !accountAnswer.data.saldo ||
        !accountAnswer.data.cpfCliente
      ) {
        const error = new Error("Erro no servidor");
        error.status = 500;
        throw error;
      }

      const result = {
        numero: accountAnswer.data.numero,
        cpfCliente: accountAnswer.data.cpfCliente,
        saldo: accountAnswer.data.saldo,
        cpfGerente: accountAnswer.data.cpfGerente,
        _links: mapLinks(accountAnswer.data.numero),
      };

      return result;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }

  async getExtract(id, query, headers) {
    try {
      const extractAnswer = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}/extract`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
          params: query,
        },
      );

      if (!extractAnswer.data) {
        const error = new Error("Conta não encontrado");
        error.status = 404;
        throw error;
      }

      if (
        !extractAnswer.data.saldoAbertura ||
        !extractAnswer.data.movimentacoes
      ) {
        const error = new Error("Erro no servidor");
        error.status = 500;
        throw error;
      }

      return extractAnswer.data;
    } catch (err) {
      console.log(err.message)
      rethrow(err);
    }
  }
}

module.exports = new AccountService();
