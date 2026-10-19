const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const reportController = require('../controller/reportController')

const router = Router();

router.get('/clientes',  authGatewayFilter(), reportController.getReport);

module.exports = router;
