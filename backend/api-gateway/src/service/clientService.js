const axios = require("axios");
const { cacheObject, getByCache, removeCache } = require("../utils/cacheUtils");
const linksUtils = require("../utils/linksUtils");
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";

class ClientService {
  async listClients(busca) {
    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients`,
      {
        params: { busca },
      },
    );

    const clients = response.data.clientes ?? [];

    return clients
      .map((c) => ({
        _links: linksUtils.rewriteLinks(c.links),
        cpf: c.cpf,
        nome: c.nome,
        email: c.email,
        telefone: c.telefone,
        salario: c.salario,
        cidade: c.endereco?.cidade,
        uf: c.endereco?.uf,
      }))
      .sort((a, b) =>
        a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }),
      );
  }

  async getClient(cpf, headers) {
    const cached = await getByCache("cliente", cpf);
    if (cached) {
      return cached;
    }
    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`,
      { headers: { "x-user-tipo": headers["x-user-tipo"] } },
    );

    const client = {
      cpf: response.data.cpf,
      nome: response.data.nome,
      email: response.data.email,
      telefone: response.data.telefone,
      // valores monetários trafegam como string (seção 5.1)
      salario:
        response.data.salario != null ? String(response.data.salario) : null,
      endereco: {
        cidade: response.data.endereco?.cidade,
        estado: {
          uf: response.data.endereco?.uf,
        },
      },
      _links: linksUtils.rewriteLinks(response.data._links),
    };

    cacheObject("cliente", client);

    return client;
  }
}

module.exports = new ClientService();
