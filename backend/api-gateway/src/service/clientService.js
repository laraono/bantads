const axios = require("axios");
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:3000";

class ClientService {
  // monta os _links com os paths públicos do gateway e não os do ms-client
  mapLinks(cpf, isGerente, gatewayUrl = GATEWAY_URL) {
    const base = `${gatewayUrl}/clientes`;
    const links = {
      self: { href: `${base}/${cpf}` },
      conta: { href: `${base}/${cpf}/conta` },
    };

    if (isGerente) {
      links.clientes = { href: base };
    }

    return links;
  }
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
        _links: this.mapLinks(c.cpf, true),
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

    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`,
      { headers: { "x-user-tipo": headers["x-user-tipo"] } },
    );

    const isGerente = headers["x-user-tipo"] === "GERENTE";

    const client = {
      cpf: response.data.cpf,
      nome: response.data.nome,
      email: response.data.email,
      telefone: response.data.telefone,
      salario: response.data.salario,
      endereco: {
        cidade: response.data.endereco?.cidade,
        estado: {
          uf: response.data.endereco?.uf,
        },
      },
      _links: this.mapLinks(response.data.cpf, isGerente),
    }
    
    return client;
  }
}

module.exports = new ClientService();
