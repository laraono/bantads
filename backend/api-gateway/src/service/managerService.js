const axios = require('axios');

const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || 'ms-manager';
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || '8084';

class MangerService {
    async list() {
        const managers = await axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers`)

        return managers.data
    }
}

module.exports = new MangerService();
