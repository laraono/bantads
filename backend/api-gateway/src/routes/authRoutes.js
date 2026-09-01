const { Router } = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');

const authController = require('../controllers/authController');

const router = Router();
const MS_AUTH_HOST = process.env.MS_AUTH_HOST || 'ms-auth';
const MS_AUTH_PORT = process.env.MS_AUTH_PORT || '8081';
const JWT_SECRET = process.env.JWT_SECRET || 'sua_chave_secreta';

router.post("/login", authController.login);

router.post("/logout", authController.logout); 

router.use('/', createProxyMiddleware({
    target: `http://${MS_AUTH_HOST}:${MS_AUTH_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/api/auth': '/auth' }
}));

module.exports = router;