const { Router } = require('express');
const managerController = require('../controller/managerController');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');

const router = Router();

router.get('/', authGatewayFilter('GERENTE'), managerController.list);

router.post('/', authGatewayFilter('GERENTE'), managerController.createManager);

router.get('/:id', authGatewayFilter('GERENTE'), managerController.getManager);

router.put('/:id', authGatewayFilter('GERENTE'), managerController.updateManager);

router.delete('/:id', authGatewayFilter('GERENTE'), managerController.deleteManager);
module.exports = router;
