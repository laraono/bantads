const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");
const { cacheObject, getByCache, removeCache } = require("../utils/cacheUtils");
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";

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

function toIso(date) {
  return date ? new Date(date).toISOString() : null;
}

// entidade Request do MS Cliente -> DTO da solicitação para o front (R8)
function toRequestDTO(request) {
  return {
    cpf: request.cpf,
    nome: request.name,
    email: request.email,
    salario: request.salary != null ? String(request.salary) : null,
    status: request.status,
    motivo: request.motivo ?? null,
    dataDecisao: toIso(request.approvedAt ?? request.rejectionAt),
    _links: request._links ?? request.links,
  };
}

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
    try {
      const response = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`,
      );
      const solicitacoes = response.data?.solicitacoes ?? [];
      return { solicitacoes: solicitacoes.map(toRequestDTO) };
    } catch (err) {
      rethrow(err);
    }
  }

  async getRequest(cpf) {
    try {
      const cached = getByCache("solicitacoes", cpf);
      if (cached) {
        return cached;
      }
      const response = await axios.get(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}`,
      );
      const request = toRequestDTO(response.data);

      cacheObject("solicitacoes", request);

      return request;
    } catch (err) {
      rethrow(err);
    }
  }

  async createRequest(body) {
    try {
      const response = await axios.post(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`,
        body,
      );

      removeCache("solicitacoes", body.cpf);

      return response.data;
    } catch (err) {
      rethrow(err);
    }
  }

  async rejectRequest(cpf, body) {
    try {
      const response = await axios.post(
        `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}/rejeicao`,
        {
          motivo: body?.motivo,
        },
      );

      removeCache("solicitacoes", cpf);

      return toRequestDTO(response.data);
    } catch (err) {
      rethrow(err);
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

    removeCache("solicitacoes", cpf);

    return response.data;
  }
}

module.exports = new RequestService();
