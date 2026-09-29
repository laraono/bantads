const { Router } = require('express');
const managerController = require('../controller/managerController');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');

const router = Router();

router.get('/', authGatewayFilter(), managerController.list);

router.get('/:id', authGatewayFilter(), managerController.getManager);

router.put('/:id', authGatewayFilter(), managerController.updateManager);

module.exports = router;
