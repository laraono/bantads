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
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes`,
      {
        params: { busca },
      },
    );

  const clients = response.data._embedded?.clientList ?? [];

    return clients
      .map((c) => ({
        _links: this.mapLinks(c.cpf, true),
        cpf: c.cpf,
        nome: c.name,
        email: c.email,
        telefone: c.phone,
        salario: c.salary,
        cidade: c.address?.city,
        uf: c.address?.state?.uf,
      }))
      .sort((a, b) =>
        a.nome.localeCompare(b.nome, "pt-BR", { sensitivity: "base" }),
      );
  }

  async getClient(cpf, headers) {
    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${cpf}`,
      { headers: { "x-user-tipo": headers["x-user-tipo"] } },
    );

    const isGerente = headers["x-user-tipo"] === "GERENTE";

    return {
      cpf: response.data.cpf,
      nome: response.data.name,
      email: response.data.email,
      telefone: response.data.phone,
      salario: response.data.salary,
      endereco: {
        cidade: response.data.address.city,
        estado: {
          uf: response.data.address.state.uf,
        },
      },
      _links: this.mapLinks(response.data.cpf, isGerente),
    };
  }
}

module.exports = new ClientService();
