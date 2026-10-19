const axios = require('axios');

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const ORCHESTRATION_HOST = process.env.ORCHESTRATION_HOST || 'orchestration';
const ORCHESTRATION_PORT = process.env.ORCHESTRATION_PORT || '8085';

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

    async createClient(targetCpf, headers) {
        try {
            const response = await axios.post(`http://${ORCHESTRATION_HOST}:${ORCHESTRATION_PORT}/orchestration/create-account`,
                { cpfCliente: targetCpf },
                { headers: { 'x-user-cpf': headers['x-user-cpf']} }
            )
            return response.data
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

module.exports = new ClientService();
