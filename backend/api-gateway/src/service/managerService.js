const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || "ms-manager";
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || "8084";
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
    const response = await axios.get(
      `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`,
    );


    const managers = response.data._embedded?.managersList ?? [];

    return managers
      .map((c) => ({
        _links: this.mapLinks(c.cpf, true),
        id: c.id,
        cpf: c.cpf,
        nome: c.name,
        email: c.email,
        telefone: c.phone,
        ativo: c.ativo
      }))
      .sort((a, b) =>
        a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }),
      );

  }

  async getManager(id) {
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
  }

  async updateManager(id, body, headers) {
    try {
      const managerAnswer = await axios.put(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
        body,
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
      if (err.response) {
        const error = new Error(err.response.data?.message || err.message);
        error.status = err.response.status;
        throw error;
      }
      throw err;
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
    try {
      const payload = {
        ...gerente,
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
    } catch (err) {
      if (err.response) {
        const error = new Error(err.response.data?.message || err.message);
        error.status = err.response.status;
        throw error;
      }
      throw err;
    }
  }
}

module.exports = new MangerService();
