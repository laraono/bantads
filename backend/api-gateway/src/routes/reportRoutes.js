const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const reportController = require('../controller/reportController')

const router = Router();

router.get('/relatorios/clientes',  authGatewayFilter(), reportController.getReport);

module.exports = router;
