const axios = require('axios');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const redis = require('../config/redis');

const MS_AUTH_HOST = process.env.MS_AUTH_HOST || 'ms-auth';
const MS_AUTH_PORT = process.env.MS_AUTH_PORT || '8081';
const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || 'ms-manager';
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || '8084';
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

        // API Composition: o MS Auth não guarda nome/e-mail, então busca no
        // MS Cliente ou MS Gerente (conforme o tipo) pelo CPF
        const usuario = await this.composeUsuario(data.cpf, data.tipo);

        return { auth: true, token, tipo: data.tipo, usuario };
    }

    async composeUsuario(cpf, tipo) {
        try {
            if (tipo?.toUpperCase() === 'GERENTE') {
                const { data } = await axios.get(`http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}/managers/${cpf}`);
                return { cpf: data.cpf, nome: data.name ?? data.nome, email: data.email };
            }
            const { data } = await axios.get(`http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}/clients/${cpf}`);
            return { cpf: data.cpf, nome: data.name ?? data.nome, email: data.email };
        } catch (err) {
            console.log(err.message)
            // sem nome/e-mail, mas o login não deve falhar por causa disso
            return { cpf, nome: '', email: '' };
        }
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
