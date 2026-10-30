const redis = require('../config/redis');

class JobService {

    async getStatus(id) {
        const job = await redis.get(id)

        if(!job) {
            console.warn(`job ${id} não encontrada no Redis (expirada ou Redis reiniciado)`);
            const error = new Error('Job não existe');
            error.status = 404;
            throw error;
        }

        const parsedJob = JSON.parse(job)
        console.log('get job status', parsedJob)
        return parsedJob
    }

    async getResult(id) {
        const job = JSON.parse(await redis.get(id))

        if(!job) {
            const error = new Error('Job não existe');
            error.status = 404;
            throw error;
        }

        return job.resultado ?? null
    }

}

module.exports = new JobService();
