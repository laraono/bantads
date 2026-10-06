const axios = require('axios');

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const ORCHESTRATION_HOST= process.env.ORCHESTRATION_HOST || 'orchestration';
const ORCHESTRATION_PORT = process.env.ORCHESTRATION_PORT || '8085';

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
        const response = await axios.post(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests`, body)        
        return response.data
    }

    async approveRequest(cpf) {
        const payload = {idSolicitacao: cpf}
        const response = await axios.post(`http://${ORCHESTRATION_HOST}:${ORCHESTRATION_PORT}/orchestration/create-account`, {
            body: payload
        })
        
        return response.data
    }

    async rejectRequest(cpf, body) {
        const response = await axios.post(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/requests/${cpf}/reject`, body)
        
        return response.data
    }
}

module.exports = new RequestService();
