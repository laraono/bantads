const { Router } = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const axios = require('axios');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const redis = require('../config/redis');

const router = Router();
const MS_AUTH_HOST = process.env.MS_AUTH_HOST || 'ms-auth';
const MS_AUTH_PORT = process.env.MS_AUTH_PORT || '8080';
const JWT_SECRET = process.env.JWT_SECRET || 'sua_chave_secreta';

router.post("/login", async (req, res) => {
    try{
        const authResponse = axios.post(`http://${MS_AUTH_HOST}:${MS_AUTH_PORT}/login`, req.body)
        const data = authResponse.data

        if(!data || !data.auth){
            return res.status(401).json({message: 'Usuário não autorizado'})
        }

        const jti = crypto.randomUUID();
        const payload = { jti, cpf: data.cpf, tipo: data.tipo };
        const token = jwt.sign(payload, JWT_SECRET, {expiresIn: '7d'})

        await redis.set(`sessao:${jti}`, 'ativo', 'EX', 1800);

        return res.status(200).json({ accessToken: token });
    } catch(err){
        return res.status(401).json({ erro: 'Falha na autenticação' });
    }
})

router.post("/logout", async (req, res) => {
    try{
        const authHeader = req.headers['authorization'];
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
                return res.status(401).json({ message: 'Usuário não autorizado' });
            }

        const token = authHeader.split(' ')[1];
        const decoded = jwt.decode(authHeader);

        if(decoded && decoded.jti) {
                redis.del(`sessao:${decoded.jti}`)
            }
        return res.status(200).json({ mensagem: 'Logout realizado com sucesso' });
    } 
    catch (err) {
        return res.status(500).json({ erro: 'Erro ao realizar logout' });
    }
})

router.use('/', createProxyMiddleware({
    target: `http://${MS_AUTH_HOST}:${MS_AUTH_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/api/auth': '/auth' }
}));

module.exports = router;