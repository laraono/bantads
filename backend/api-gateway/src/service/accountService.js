const axios = require("axios");
const clientService = require("../service/clientService");
const linksUtils = require("../utils/linksUtils");
const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || "ms-account";
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || "8083";

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
      rethrow(err);
    }
  }

  async transfer(id, body, headers) {
    try {
      if (!body?.contaDestino || !body.valor) {
        const error = new Error("Conta de destino não informada");
        error.status = 400;
        throw error;
      }

      const msBody = { contaDestino: body.contaDestino, valor: body.valor };

      const destinationCPF = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${msBody.contaDestino}/cpf`,
        {
          headers: { "x-user-cpf": headers["x-user-cpf"] },
        },
      );

      const cpf = destinationCPF.data;

      if (!cpf) {
        const error = new Error("Conta destino não existe");
        error.status = 422;
        throw error;
      }

      const originClient = await clientService.getClient(
        headers["x-user-cpf"],
        headers,
      );

      if (!originClient.nome || !originClient.cpf) {
        const error = new Error("Usuário de origem não encontrado");
        error.status = 404;
        throw error;
      }

      const destinationClient = await clientService.getClient(cpf, headers);

      if (!destinationClient.nome) {
        const error = new Error("Usuário não existe");
        error.status = 422;
        throw error;
      }

      const payload = {
        destino: {
          conta: msBody.contaDestino,
          nome: destinationClient.nome,
          cpf,
        },
        origem: {
          nome: originClient.nome,
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
      rethrow(err);
    }
  }

  async withdraw(id, body, headers) {
    try {
      await axios.post(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/withdraw`,
        { payload: body },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      return;
    } catch (err) {
      rethrow(err);
    }
  }

  async deposit(id, body, headers) {
    try {
      if (
        !body ||
        !body.valor ||
        typeof body.valor !== "string" ||
        !/^\d+(\.\d{1,2})?$/.test(body.valor) // filtra para strings em forma de dinheiro
      ) {
        const error = new Error("Valor do depósito inválido");
        error.status = 400;
        throw error;
      }

      const cpf = headers["x-user-cpf"];
      const client = await clientService.getClient(cpf, headers);

      const balance = await axios.post(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/events/${id}/deposit`,
        {
          payload: {
            valor: body.valor,
            nome: client.nome,
            cpfCliente: cpf,
          },
        },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      return balance.data;
    } catch (err) {
      rethrow(err);
    }
  }

  async getAccount(id, headers) {
    try {
      const accountAnswer = await axios.get(
        `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}/accounts/${id}`,
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
        _links: linksUtils.rewriteLinks(accountAnswer.data._links),
      };

      return result;
    } catch (err) {
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
        _links: linksUtils.rewriteLinks(accountAnswer.data._links),
      };

      return result;
    } catch (err) {
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
      rethrow(err);
    }
  }
}

module.exports = new AccountService();
