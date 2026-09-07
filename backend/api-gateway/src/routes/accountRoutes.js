const { Router } = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const accountController = require('../controller/accountController');

const router = Router();

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const target = `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}`;

router.get('/:id', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path) => `/accounts${path}`
}));

router.get('/:id/extrato', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) => { return`/accounts/${req.params.id}/extract/${req.query}` }
}));

router.post('/:id/deposito', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) =>{ return`/accounts/${req.params.id}/deposit` }
}));

router.post('/:id/saque', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path, req) =>{ return `/accounts/${req.params.id}/withdraw` }
}));

router.post('/:id/transferencia', accountController.transfer);

module.exports = router;
