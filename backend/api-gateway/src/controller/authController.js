const authService = require('../service/authService');

class AuthController {
    async login(req, res) {
        try {
            const result = await authService.authenticate(req.body);
            return res.status(200).json(result);
        } catch (err) {
            const status = err.status || 401;
            return res.status(status).json({ error: err.message || 'Falha na autenticação' });
        }
    }

    async logout(req, res) {
        try {
            const authHeader = req.headers['x-access-token'];
            await authService.invalidateSession(authHeader);
            return res.status(204).json({ message: 'Logout realizado com sucesso' });
        } catch (err) {
            const status = err.status || 500;
            return res.status(status).json({ error: err.message || 'Erro ao realizar logout' });
        }
    }
}

module.exports = new AuthController();