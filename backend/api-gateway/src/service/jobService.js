const redis = require('../config/redis');

class JobService {

    // jobs de SAGA ficam no Redis em "job-<sagaId>"; retorna null se não existir
    // (ex.: job do relatório, que é mantido pelo MS Gerente)
    async getJob(id) {
        const raw = await redis.get(`job-${id}`)
        if (!raw) return null

        const { ['@class']: _ignored, ...job } = JSON.parse(raw)
        return job
    }

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

        return job.resultado ?? null
    }

}

module.exports = new JobService();
