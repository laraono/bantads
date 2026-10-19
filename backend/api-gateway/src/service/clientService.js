const axios = require("axios");

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || "ms-client";
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || "8082";
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:3000";

class ClientService {
  rewriteLinks(links = {}, gatewayUrl = GATEWAY_URL) {
    // converte _link do controller para _links do gateway
    return Object.fromEntries(
      Object.entries(links).map(([rel, link]) => [
        rel,
        { ...link, href: link.href.replace(/^https?:\/\/[^/]+/, gatewayUrl) },
      ]),
    );
  }

  async listClients(busca) {
    const clients = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes`,
      {
        params: { busca },
      },
    );
    return clients.data._embedded?.clientList ?? [];
  }

  async getClient(cpf, headers) {
    const response = await axios.get(
      `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clientes/${cpf}`, { headers: { "x-user-tipo": headers["x-user-tipo"] } },
    );

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
      _links: this.rewriteLinks(response.data._links, GATEWAY_URL),
    };
  }
}

module.exports = new ClientService();
