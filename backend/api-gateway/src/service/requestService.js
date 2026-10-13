const axios = require('axios');
const rabbitConnector = require('../connector/rabbitConnector')

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';


class RequestService {
    async listRequests() {        
        const requests = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`)
        return {solicitacoes: requests.data}
    }

    async getRequest(cpf) {
        const response = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}`)
     
        return response.data
    }

    async createRequest(body) {
        try {
            const response = await axios.post(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`, body)
            return response.data
        } catch (err) {
            if (err.response) {
                const data = err.response.data;
                const message = (typeof data === 'object' && (data?.detail || data?.message)) || err.message;
                const error = new Error(message);
                error.status = err.response.status;
                throw error;
            }
            throw err;
        }
    }

    async approveRequest(cpf) {
        const payload = {
            idSolicitacao: cpf,
            type: 'CRIACAO_CONTA'
        }

        const job = {
            status: "PENDENTE",
            resultType: "resource",
            dominio: "clientes",
            resourceId: cpf,
            erro: null
        }
            
        return await rabbitConnector.startSaga(payload, job)
    }

    async rejectRequest(cpf, body) {
        const response = await axios.post(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}/reject`, body)
        
        return response.data
    }
}

module.exports = new RequestService();
