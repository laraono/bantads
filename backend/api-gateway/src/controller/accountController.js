const accountService = require('../service/accountService');

class AccountController {
    async transfer(req, res) {
        try {
            const result = await accountService.transfer(req.params.id, req.body);
            return res.status(201).json(result);
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Falha no servidor' });
        }
    }

}

module.exports = new AccountController();