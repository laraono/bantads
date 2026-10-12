const redis = require('../config/redis');

class JobService {

    async getStatus(id) {
        return await redis.get(id)
    }

    async getResult(id) {
        const job = await redis.get(id)

        return job.resultado
    }

}

module.exports = new JobService();
