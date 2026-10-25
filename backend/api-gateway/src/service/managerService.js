const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");
const accountService = require("./accountService");
const redis = require("../config/redis");
const { cacheObject, getByCache, removeCache } = require("../utils/cacheUtils");

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || "ms-manager";
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || "8084";

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
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:3000";
const CACHE_TTL = 60 * 5;

class MangerService {
  mapLinks(cpf, gatewayUrl = GATEWAY_URL) {
    const base = `${gatewayUrl}/gerentes`;
    const links = {
      self: { href: `${base}/${cpf}` },
    };
    return links;
  }

  toManager(data) {
    return {
      id: data.id,
      nome: data.nome,
      email: data.email,
      cpf: data.cpf,
      telefone: data.telefone,
      ativo: data.ativo,
      _links: this.mapLinks(data.cpf),
    };
  }

  async list() {
    try {
      const [managersAnswer, accounts] = await Promise.all([
        axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`),
        accountService.listAccounts(),
      ]);

      const clientesPorGerente = new Map();
      for (const conta of accounts?._embedded?.getAccountDTOList ?? []) {
        clientesPorGerente.set(
          conta.cpfGerente,
          (clientesPorGerente.get(conta.cpfGerente) ?? 0) + 1,
        );
      }

      const gerentes = (managersAnswer.data?.gerentes ?? [])
        .map((g) => {
          const manager = this.toManager(g);
          return {
            ...manager,
            quantidadeClientes: clientesPorGerente.get(g.cpf) ?? 0,
          };
        })
        .sort((a, b) =>
          a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }),
        );

      return { "gerentes": gerentes };
    } catch (err) {
      rethrow(err);
    }
  }

  async getManager(id) {
    try {
      const cached = await getByCache("gerente", id);

      if (cached) {
        return cached;
      }
      const managerAnswer = await axios.get(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
      );

      const returnData = this.toManager(managerAnswer.data);

      cacheObject("gerente", returnData);

      return returnData;
    } catch (err) {
      rethrow(err);
    }
  }

  // R14: e-mail e CPF não podem ser alterados, então só nome e telefone seguem
  async updateManager(id, body, headers) {
    try {
      if (body.email) {
        const error = new Error("Não é possível alterar o email");
        error.status = 400;
        throw error;
      }

      if (body.cpf) {
        const error = new Error("Não é possível alterar o CPF");
        error.status = 400;
        throw error;
      }

      const managerAnswer = await axios.put(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
        { nome: body.nome, telefone: body.telefone, ativo: true },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );

      removeCache("gerente", managerAnswer.data.cpf);

      return {
        id: managerAnswer.data.id,
        nome: managerAnswer.data.nome,
        email: managerAnswer.data.email,
        cpf: managerAnswer.data.cpf,
        telefone: managerAnswer.data.telefone,
        ativo: managerAnswer.data.ativo,
        _links: this.mapLinks(managerAnswer.data.cpf),
      };
    } catch (err) {
      rethrow(err);
    }
  }

  async deleteManager(id, headers) {
    try {
      const payload = {
        cpfGerente: id,
        cpfSolicitante: headers["x-user-cpf"],
        type: "DELETAR_GERENTE",
      };

      const job = {
        status: "PENDENTE",
        resultType: "inline",
        dominio: null,
        resourceId: id,
        erro: null,
      };

      removeCache("gerente", id);

      return await rabbitConnector.startSaga(payload, job);
    } catch (err) {
      if (err.response) {
        const error = new Error(err.response.data?.message || err.message);
        error.status = err.response.status;
        throw error;
      }
      throw err;
    }
  }

  async createManager(gerente) {

    if (!gerente.nome || !gerente.cpf || !gerente.email || !gerente.telefone || !gerente.senha) {
      const error = new Error("Dados obrigatórios não informados");
      error.status = 400;
      throw error;
    }

    const payload = {
      gerente,
      type: "INSERIR_GERENTE",
    };

    const job = {
      status: "PENDENTE",
      resultType: "resource",
      dominio: "gerentes",
      resourceId: gerente.cpf,
      erro: null,
    };

    removeCache("gerente", gerente.cpf);

    return await rabbitConnector.startSaga(payload, job);
  }
}

module.exports = new MangerService();
