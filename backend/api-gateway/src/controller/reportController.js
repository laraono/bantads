const reportService = require('../service/reportService');

class ReportController {

    async getReport(req, res) {
        try {
            const jobId = await reportService.getReport();
            return res.status(202).json(jobId);
        } catch (err) {
            console.log(err.message)
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }
}

module.exports = new ReportController();