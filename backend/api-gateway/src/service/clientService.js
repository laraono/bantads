const axios = require('axios');
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';

class ClientService {
    async listClients(busca) {
        const clients = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients`, {
            params: { busca }
        })
        return clients.data._embedded?.clientList ?? []
    }

    async getClient(cpf) {
        const response = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`)
     
        return {
            cpf: response.data.cpf,
            nome: response.data.name,
            email: response.data.email,
            telefone: response.data.phone,
            salario: response.data.salary,
            endereco: {
                cidade: response.data.address.city,
                estado: {
                    uf: response.data.address.state.uf
                }
            },
        }
    }
}

module.exports = new ClientService();
