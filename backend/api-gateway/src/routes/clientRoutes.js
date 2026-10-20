const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const clientController = require('../controller/clientController');
const accountController = require('../controller/accountController');

const router = Router();

const MS_CLIENT_HOST = process.env.MS_CLIENT_HOST || 'ms-client';
const MS_CLIENT_PORT = process.env.MS_CLIENT_PORT || '8082';
const target = `http://${MS_CLIENT_HOST}:${MS_CLIENT_PORT}`;

router.get('/', authGatewayFilter('GERENTE'), clientController.listClients);

router.get('/:cpf', authGatewayFilter(), clientController.getClient);

router.get('/:cpf/conta', authGatewayFilter(), accountController.getAccountByCpf);

module.exports = router;
