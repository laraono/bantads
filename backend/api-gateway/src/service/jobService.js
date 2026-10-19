const redis = require('../config/redis');

class JobService {

    async getStatus(id) {
        const job = await redis.get(id)

        if(!job) {
            const error = new Error('Job não existe');
            error.status = 404;
            throw error;
        }

        const parsedJob = JSON.parse(job)
        return parsedJob
    }

    async getResult(id) {
        const job = JSON.parse(await redis.get(id))

        if(!job) {
            const error = new Error('Job não existe');
            error.status = 404;
            throw error;
        }

        return job.resultado
    }

}

module.exports = new JobService();
