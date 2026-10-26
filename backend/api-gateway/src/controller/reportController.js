const reportService = require('../service/reportService');

class ReportController {

    async getReport(req, res) {
        try {
            const jobId = await reportService.getReport();
            return res.status(200).json(jobId);
        } catch (err) {
            console.log(err.message)
            const status = err.status || 401;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }
}

module.exports = new ReportController();