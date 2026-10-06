const axios = require('axios');

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || 'ms-manager';
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || '8084';

class MangerService {
    async list() {
        const managers = await axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`)
        return managers.data
    }

    async getManager(id) {
        const managerAnswer = await axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`)

        return managerAnswer.data
    }

    async updateManager(id, body, headers) {
        try {
            const updateAnswer = await axios.put(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${id}`,
                body,
                { headers: { 'x-user-cpf': headers['x-user-cpf']} }
            )
            return updateAnswer.data
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
