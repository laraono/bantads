const jobService = require('../service/jobService');

class JobController {

    // se o job não está no Redis, segue (next) para o proxy do MS Gerente, que
    // mantém os jobs do relatório de clientes (R16)
    async getStatus(req, res, next) {
        try {
            const result = await jobService.getStatus(req.params.id);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }

    async getResult(req, res, next) {
        try {
            const job = await jobService.getStatus(req.params.id);
            if (!job) return next();
            return res.status(200).json(job.resultado ?? null);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }
}

module.exports = new JobController();
