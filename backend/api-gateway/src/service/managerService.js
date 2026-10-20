const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");
const accountService = require("./accountService");

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

class MangerService {
  mapLinks(cpf, gatewayUrl = GATEWAY_URL) {
    const base = `${gatewayUrl}/gerentes`;
    const links = {
      self: { href: `${base}/${cpf}` },
    };
    return links;
  }

  async list() {
    try {
      const [managersAnswer, accounts] = await Promise.all([
        axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`),
        accountService.listAccounts(),
      ]);

      const clientesPorGerente = new Map();
      for (const conta of accounts ?? []) {
        clientesPorGerente.set(
          conta.managerCPF,
          (clientesPorGerente.get(conta.managerCPF) ?? 0) + 1,
        );
      }

      const gerentes = (managersAnswer.data?.gerentes ?? [])
        .map((g) => ({
          _links: this.mapLinks(c.cpf, true),
          id: c.id,
          cpf: c.cpf,
          nome: c.name,
          email: c.email,
          telefone: c.phone,
          ativo: c.ativo,
          quantidadeClientes: clientesPorGerente.get(g.cpf) ?? 0,
        }))
        .sort((a, b) =>
          a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }),
        );

      return { gerentes };
    } catch (err) {
      rethrow(err);
    }
  }

  async getManager(id) {
    try {
      const managerAnswer = await axios.get(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
      );

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

  // R14: e-mail e CPF não podem ser alterados, então só nome e telefone seguem
  async updateManager(id, body, headers) {
    try {
      const managerAnswer = await axios.put(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
        { nome: body.nome, telefone: body.telefone, ativo: true },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );
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

  // HATEOAS pelas jobs
  async deleteManager(id, headers) {
    try {
      const payload = {
        cpfGerente: id,
        cpfSolicitante: headers["x-user-cpf"],
        type: "DELETAR_GERENTE",
      };

      const job = {
        status: "PENDENTE",
        resultType: "resource",
        dominio: "gerentes",
        resourceId: id,
        erro: null,
      };

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
    const payload = {
      gerente: {
        nome: gerente.nome,
        email: gerente.email,
        cpf: gerente.cpf,
        telefone: gerente.telefone,
        senha: gerente.senha,
      },
      type: "INSERIR_GERENTE",
    };

    const job = {
      status: "PENDENTE",
      resultType: "resource",
      dominio: "gerentes",
      resourceId: gerente.cpf,
      erro: null,
    };

    return await rabbitConnector.startSaga(payload, job);
  }
}

module.exports = new MangerService();
