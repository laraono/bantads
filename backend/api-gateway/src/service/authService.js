const axios = require('axios');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const redis = require('../config/redis');

const MS_AUTH_HOST = process.env.MS_AUTH_HOST || 'ms-auth';
const MS_AUTH_PORT = process.env.MS_AUTH_PORT || '8081';
const JWT_SECRET = process.env.JWT_SECRET || 'sua_chave_secreta';

class AuthService {
    async authenticate(body) {
        const msBody = { login: body.email, password: body.senha };
        const authResponse = await axios.post(`http://${MS_AUTH_HOST}:${MS_AUTH_PORT}/auth/login`, msBody);
        const data = authResponse.data;

        if (!data || !data.auth) {
            const error = new Error('Usuário não autorizado');
            error.status = 401;
            throw error;
        }

        const jti = crypto.randomUUID();
        const payload = { jti, cpf: data.cpf, tipo: data.tipo };
        const token = jwt.sign(payload, JWT_SECRET, { expiresIn: '7d' });

        await redis.set(`sessao:${jti}`, 'ativo', 'EX', 1800);

        return { auth: true, token, tipo: data.tipo, usuario: { cpf: data.cpf, login: data.login } };
    }

    async invalidateSession(tokenValue) {
        if (!tokenValue) {
            const error = new Error('Usuário não autorizado');
            error.status = 401;
            throw error;
        }

        const decoded = jwt.decode(tokenValue);

        if (decoded && decoded.jti) {
            await redis.del(`sessao:${decoded.jti}`);
        }
    }
}

module.exports = new AuthService();
