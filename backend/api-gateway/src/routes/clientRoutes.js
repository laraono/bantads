const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const clientController = require('../controller/clientController');
const accountController = require('../controller/accountController');

const router = Router();

router.get('/', authGatewayFilter('GERENTE'), clientController.listClients);

router.get('/:cpf', authGatewayFilter(), clientController.getClient);

router.get('/:cpf/conta', authGatewayFilter(), accountController.getAccountByCpf);

module.exports = router;
