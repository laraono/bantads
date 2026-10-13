const { Router } = require('express');
const managerController = require('../controller/managerController');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');

const router = Router();

router.get('/', authGatewayFilter(), managerController.list);

router.post('/', authGatewayFilter(), managerController.createManager);

router.get('/:id', authGatewayFilter(), managerController.getManager);

router.put('/:id', authGatewayFilter(), managerController.updateManager);

router.delete('/:id', authGatewayFilter(), managerController.deleteManager);
module.exports = router;
