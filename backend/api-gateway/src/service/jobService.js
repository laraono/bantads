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
        return await this.getJob(id)
    }

    async getResult(id) {
        const job = await this.getJob(id)
        if (!job) return null

        return job.resultado ?? null
    }

}

module.exports = new JobService();
