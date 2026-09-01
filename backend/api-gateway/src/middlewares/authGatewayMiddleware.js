const jwt = require('jsonwebtoken');
const redis = require('../config/redis');

const JWT_SECRET = process.env.JWT_SECRET || 'sua_chave_secreta';

function authGatewayFilter(requiredRole = null) {
    return async (req, res, next) => {
        const token = req.headers['x-access-token'];
        if (!token) {
            return res.status(401).json({ auth: false, message: 'Usuário não autorizado' });
        }

        try {
            const decoded = jwt.verify(token, JWT_SECRET);

            const jti = decoded.jti;

            const sessionKey = `sessao:${jti}`;
            const sessionExists = await redis.exists(sessionKey);

            if (!sessionExists) {
                return res.status(401).json({ auth: false, message: 'Usuário não autorizado' });
            }

            await redis.expire(sessionKey, 1800);

            if (requiredRole && decoded.tipo !== requiredRole) {
                return res.status(403).json({ auth: false, message: 'Acesso negado para este perfil' });
            }

            req.headers['x-user-cpf'] = decoded.cpf;
            req.headers['x-user-tipo'] = decoded.tipo;

            next();
        } catch (err) {
            return res.status(401).json({ auth: false, message: 'Token inválido' });
        }
    };
}

module.exports = { authGatewayFilter };
