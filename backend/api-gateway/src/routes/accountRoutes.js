const { Router } = require('express');
const { createProxyMiddleware, fixRequestBody } = require('http-proxy-middleware');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const accountController = require('../controller/accountController');

const router = Router();

const MS_ACCOUNT_HOST = process.env.MS_ACCOUNT_HOST || 'ms-account';
const MS_ACCOUNT_PORT = process.env.MS_ACCOUNT_PORT || '8083';
const target = `http://${MS_ACCOUNT_HOST}:${MS_ACCOUNT_PORT}`;

router.get('/:id', authGatewayFilter(), accountController.getAccount);

router.post('/:id/deposito',  authGatewayFilter(), accountController.deposit);

router.post('/:id/saque', authGatewayFilter(), accountController.withdraw);

router.get('/:id/extrato',  authGatewayFilter(), accountController.getExtract);

router.post('/:id/transferencia',  authGatewayFilter(),  accountController.transfer);

module.exports = router;
