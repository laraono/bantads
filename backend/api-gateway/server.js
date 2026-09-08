const express = require('express');
const cors = require('cors');
const { createProxyMiddleware } = require('http-proxy-middleware');
require('dotenv').config();
const authController = require('./src/controller/authController');
const accountRoutes = require('./src/routes/accountRoutes');
const rebootRoutes = require('./src/routes/rebootRoutes');
const { authGatewayFilter } = require('./src/middlewares/authGatewayMiddleware');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const MS_MANAGER_HOST = process.env.MS_MANAGER_HOST || 'ms-manager';
const MS_MANAGER_PORT = process.env.MS_MANAGER_PORT || '8084';

app.use('', rebootRoutes);

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

app.use('/contas', accountRoutes);

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

app.post('/login', (req, res) => authController.login(req, res));
app.post('/logout', (req, res) => authController.logout(req, res));

app.get('/health', (req, res) => {
  res.status(200).json({status: 'UP', service: 'API Gateway está estável'})
});

app.post('/reboot', (req, res) => {
  res.status(200).json({status: 'OK', message: 'API Gateway foi reiniciado'})
});

app.listen(PORT, () => console.log(`API Gateway rodando na porta ${PORT}`));
