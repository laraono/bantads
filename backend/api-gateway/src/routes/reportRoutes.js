const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const reportController = require('../controller/reportController')

const router = Router();

router.get('/clientes',  authGatewayFilter('GERENTE'), reportController.getReport);

module.exports = router;
