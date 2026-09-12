const accountService = require('../service/accountService');

class AccountController {
    async transfer(req, res) {
        try {
            const result = await accountService.transfer(req.params.id, req.body, req.headers);
            return res.status(201).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }

    async withdraw(req, res) {
        try {
            const result = await accountService.withdraw(req.params.id, req.body, req.headers);
            return res.status(201).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }


    async deposit(req, res) {
        try {
            const result = await accountService.deposit(req.params.id, req.body, req.headers);
            return res.status(201).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }

    async getAccount(req, res) {
        try {
            const result = await accountService.getAccount(req.params.id, req.headers);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }

    async getExtract(req, res) {
        try {
            const result = await accountService.getExtract(req.params.id, req.query, req.headers);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }


}

module.exports = new AccountController();