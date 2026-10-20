const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";

class RequestService {
  mapLinks(cpf, status, gatewayUrl = GATEWAY_URL) {
    const base = `${gatewayUrl}/solicitacoes`;
    const links = {
      self: { href: `${base}/${cpf}` },
    };

    if (status === "PENDENTE") {
      links.aprovacao = { href: `${base}/${cpf}/aprovacao` };
      links.rejeicao = { href: `${base}/${cpf}/rejeicao` };
    }

    return links;
  }

  async listRequests() {
    const requestsList = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`,
    );

    const response = response.data._embedded?.requestsList ?? [];

    return {
      solicitacoes: requests.map((c) => ({
        _links: this.mapLinks(response.data.cpf, response.data.status),
        status: response.data.status,
        id: response.data.requestId,
        motivo: response.data.rejectionReason,
        aprovadoEm: response.data.approvedAt,
        nome: response.data.name,
        email: response.data.email,
        cpf: response.data.cpf,
        telefone: response.data.phone,
        salario: response.data.salary,
        cep: response.data.cep,
        cidade: response.data.city,
        uf: response.data.uf,
        endereco: {
          logradouro: response.data.street,
          numero: response.data.number,
          complemento: response.data.additionalInfo,
        },
        cliente: {
          nome: response.data.client?.name,
          email: response.data.client?.email,
          cpf: response.data.client?.cpf,
          telefone: response.data.client?.phone,
          endereco: {
            logradouro: response.data.client?.street,
            numero: response.data.client?.number,
            complemento: response.data.client?.additionalInfo,
          },
        },
      })),
    };
  }

  async getRequest(cpf) {
    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}`,
    );

    return {
      _links: this.mapLinks(response.data.cpf, response.data.status),
      status: response.data.status,
      id: response.data.requestId,
      motivo: response.data.rejectionReason,
      aprovadoEm: response.data.approvedAt,
      nome: response.data.name,
      email: response.data.email,
      cpf: response.data.cpf,
      telefone: response.data.phone,
      salario: response.data.salary,
      cep: response.data.cep,
      cidade: response.data.city,
      uf: response.data.uf,
      endereco: {
        logradouro: response.data.street,
        numero: response.data.number,
        complemento: response.data.additionalInfo,
      },
      cliente: {
        nome: response.data.client?.name,
        email: response.data.client?.email,
        cpf: response.data.client?.cpf,
        telefone: response.data.client?.phone,
        endereco: {
          logradouro: response.data.client?.street,
          numero: response.data.client?.number,
          complemento: response.data.client?.additionalInfo,
        },
      },
    };
  }

  async createRequest(body) {
    try {
      const response = await axios.post(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`,
        body,
      );
      return response.data;
    } catch (err) {
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
  }

  // HATEOAS pras jobs
  async approveRequest(cpf) {
    const payload = {
      idSolicitacao: cpf,
      type: "CRIACAO_CONTA",
    };

    const job = {
      status: "PENDENTE",
      resultType: "resource",
      dominio: "clientes",
      resourceId: cpf,
      erro: null,
    };

    return await rabbitConnector.startSaga(payload, job);
  }

  async rejectRequest(cpf, body) {
    const response = await axios.post(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}/reject`,
      body,
    );

    return response.data;
  }
}

module.exports = new RequestService();
