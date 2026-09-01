const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');
require('dotenv').config();
const authController = require('./src/controller/authController');
const { authGatewayFilter } = require('./src/middlewares/authGatewayMiddleware');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(cors());

app.post('/login', (req, res) => authController.login(req, res));
app.post('/logout', (req, res) => authController.logout(req, res));

app.get('/health', (req, res) => {
  res.status(200).json({status: 'UP', service: 'API Gateway está estável'})
});

app.post('/reboot', (req, res) => {
  res.status(200).json({status: 'OK', message: 'API Gateway foi reiniciado'})
});

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || 'ms-manager';
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || '8084';

app.use('/clientes', authGatewayFilter(), createProxyMiddleware({
    target: `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/clients' }
}));

app.use('/solicitacoes', createProxyMiddleware({
    target: `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/requests' }
}));

app.use('/contas', authGatewayFilter(), createProxyMiddleware({
    target: `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/accounts' }
}));

app.use('/gerentes', authGatewayFilter(), createProxyMiddleware({
    target: `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/managers' }
}));

app.use('/relatorios', authGatewayFilter(), createProxyMiddleware({
    target: `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/relatorios' }
}));

app.use('/jobs', authGatewayFilter(), createProxyMiddleware({
    target: `http://${MS_MANAGER_HOST}:${MS_MANAGER_PORT}`,
    changeOrigin: true,
    pathRewrite: { '^/': '/jobs' }
}));

app.listen(PORT, () => console.log(`API Gateway rodando na porta ${PORT}`));
