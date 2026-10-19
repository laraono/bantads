const axios = require("axios");
const rabbitConnector = require("../connector/rabbitConnector");

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || "ms-manager";
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || "8084";
const ORCHESTRATION_HOST = process.env.ORCHESTRATION_HOST || "orchestration";
const ORCHESTRATION_PORT = process.env.ORCHESTRATION_PORT || "8085";

class MangerService {
  async list() {
    const managers = await axios.get(
      `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`,
    );
    return managers.data;
  }

  async getManager(id) {
    const managerAnswer = await axios.get(
      `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`
    )

    return managerAnswer.data;
  }

  async updateManager(id, body, headers) {
    try {
      const updateAnswer = await axios.put(
        `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
        body,
        { headers: { "x-user-cpf": headers["x-user-cpf"] } },
      );
      return updateAnswer.data;
    } catch (err) {
      if (err.response) {
        const error = new Error(err.response.data?.message || err.message);
        error.status = err.response.status;
        throw error;
      }
      throw err;
    }
  }

  async deleteManager(id) {
    const payload = {
      cpfGerente: id,
      type: 'DELETAR_GERENTE'
    }

    const job = {
      status: "PENDENTE",
      resultType: "inline",
      dominio: null,
      resourceId: id,
      erro: null
    }
      
    return await rabbitConnector.startSaga(payload, job)
    
  }

  
  async createManager(gerente) {
    try {
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
