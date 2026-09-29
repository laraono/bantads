const managerService = require('../service/managerService');

class ManagerController {
    async list(req, res) {
        try {
            const result = await managerService.list();
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }
}

module.exports = new ManagerController();