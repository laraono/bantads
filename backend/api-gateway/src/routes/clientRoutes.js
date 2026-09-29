const { Router } = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const apiCompositionController = require('../controller/apiCompositionController');
const clientController = require('../controller/clientController');
const accountController = require('../controller/accountController');

const router = Router();

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const target = `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}`;

router.get('/', authGatewayFilter('GERENTE'), clientController.listClients);

router.post('/', createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/': '/clients/' }
}));

router.get('/:cpf', authGatewayFilter(), clientController.getClient);

router.get('/:cpf/conta', authGatewayFilter(), accountController.getAccountByCpf);

module.exports = router;
