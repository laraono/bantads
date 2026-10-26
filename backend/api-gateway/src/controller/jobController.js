const jobService = require('../service/jobService');

class JobController {

    // mantém os jobs do relatório de clientes (R16)
    async getStatus(req, res, next) {
        try {
            const job = await jobService.getStatus(req.params.id);
            if (!job) return next();
            return res.status(200).json(job);
        } catch (err) {
            console.log(err.message)
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
            console.log(err.message)
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }
}

module.exports = new JobController();
