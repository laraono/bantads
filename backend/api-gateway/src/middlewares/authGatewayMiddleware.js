const express = require('express');
const jwt = require('jsonwebtoken');
const Redis = require('ioredis');

const app = express();
const JWT_SECRET = process.env.JWT_SECRET; 

const redis = new Redis({
    host: process.env.REDIS_HOST || 'redis-app',
    port: Number(process.env.REDIS_PORT) || 6379
});

function authGatewayFilter(requiredRole = null) {
    return async (req, res, next) => {
        const authHeader = req.headers['authorization'];
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({ message: 'Usuário não autorizado' });
        }

        const token = authHeader.split(' ')[1];

        try {
            const decoded = jwt.verify(token, JWT_SECRET);
            
            const jti = decoded.jti;

            const sessionKey = `sessao:${jti}`;
            const sessionExists = await redis.exists(sessionKey);

            if (!sessionExists) {
                return res.status(401).json({ message: 'Usuário não autorizado' });
            }

            await redis.expire(sessionKey, 1800);

            if (requiredRole && decoded.tipo !== requiredRole) {
                return res.status(403).json({ erro: 'Acesso negado para este perfil' });
            }

            req.headers['x-user-cpf'] = decoded.cpf;
            req.headers['x-user-tipo'] = decoded.tipo;

            next();
        } catch (err) {
            return res.status(403).json({ error: 'Token inválido' });
        }
    };
}

app.listen(3000, () => console.log('Gateway rodando!'));