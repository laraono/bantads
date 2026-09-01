const { Router } = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const authGatewayFilter = require('../middlewares/authGatewayMiddleware');

const router = Router();

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const target = `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}`;

router.post('/', createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/': '/clients/' }
}));

router.get('/', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/': '/clients' }
}));

router.get('/:id', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path) => `/clients${path}`
}));

router.get('/:id/request', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: (path) => `/clients${path}`
}));

router.post('/requests', authGatewayFilter(), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/requests': '/requests' }
}));

router.post('/requests/:id/approve', authGatewayFilter('GERENTE'), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/requests': '/requests' }
}));

router.post('/requests/:id/cancel', authGatewayFilter('GERENTE'), createProxyMiddleware({
    target,
    changeOrigin: true,
    pathRewrite: { '^/requests': '/requests' }
}));

module.exports = router;
