const jobService = require('../service/jobService');

class JobController {

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
            const result = await jobService.getResult(req.params.id);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }
}

module.exports = new JobController();
