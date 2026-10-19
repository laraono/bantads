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

    async getManager(req, res) {
        try {
            const result = await managerService.getManager(req.params.id);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async updateManager(req, res) {
        try {
            const result = await managerService.updateManager(req.params.id, req.body, req.headers);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async deleteManager(req, res) {
        // R15: o gerente não pode remover a si mesmo (checado antes de iniciar a SAGA)
        if (req.headers['x-user-cpf'] === req.params.id) {
            return res.status(403).json({ error: 'Não é possível remover a si mesmo' });
        }
        try {
            const jobId = await managerService.deleteManager(req.params.id);
            return res.status(202).json({ jobId });
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

    async createManager(req, res) {
        try {
            const jobId = await managerService.createManager(req.body);
            return res.status(202).json({ jobId });
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro no servidor' });
        }
    }

}

module.exports = new ManagerController();
