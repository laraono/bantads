const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");
const accountService = require("./accountService");

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || "ms-manager";
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || "8084";

function rethrow(err) {
  if (err.response) {
    const data = err.response.data;
    const message = (typeof data === "object" && (data?.detail || data?.message)) || err.message;
    const error = new Error(message);
    error.status = err.response.status;
    throw error;
  }
  throw err;
}

class MangerService {
  // API Composition (R12): gerentes ativos do MS Gerente + quantidade de
  // clientes de cada um, contada nas contas do MS Conta (query)
  async list() {
    try {
      const [managersAnswer, accounts] = await Promise.all([
        axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`),
        accountService.listAccounts(),
      ]);

      const clientesPorGerente = new Map();
      for (const conta of accounts ?? []) {
        clientesPorGerente.set(conta.managerCPF, (clientesPorGerente.get(conta.managerCPF) ?? 0) + 1);
      }

      const gerentes = (managersAnswer.data?.gerentes ?? [])
        .map((g) => ({
          ...g,
          quantidadeClientes: clientesPorGerente.get(g.cpf) ?? 0,
        }))
        .sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }));

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

      return managerAnswer.data;
    } catch (err) {
      rethrow(err);
    }
  }

  // R14: e-mail e CPF não podem ser alterados, então só nome e telefone seguem
  async updateManager(id, body, headers) {
    try {
      const updateAnswer = await axios.put(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
        { nome: body.nome, telefone: body.telefone, ativo: true },
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );
      return updateAnswer.data;
    } catch (err) {
      rethrow(err);
    }
  }

  async deleteManager(id) {
    const payload = {
      cpfGerente: id,
      type: "DELETAR_GERENTE",
    };

    const job = {
      status: "PENDENTE",
      resultType: "inline",
      dominio: "gerentes",
      resourceId: id,
      erro: null,
    };

    return await rabbitConnector.startSaga(payload, job);
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
