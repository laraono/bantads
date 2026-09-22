const { Router } = require('express');
const managerController = require('../controller/managerController');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');

const router = Router();

router.get('/', authGatewayFilter(), managerController.list);

module.exports = router;
