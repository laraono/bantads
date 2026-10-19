const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const requestController = require('../controller/requestController');

const router = Router();

router.get('/', authGatewayFilter('GERENTE'), requestController.listRequests);

// autocadastro (R1) é público
router.post('/', requestController.createRequest);

router.get('/:id', authGatewayFilter('GERENTE'), requestController.getRequest);

router.post('/:id/aprovacao', authGatewayFilter('GERENTE'), requestController.approveRequest);

router.post('/:id/rejeicao', authGatewayFilter('GERENTE'), requestController.rejectRequest);

module.exports = router;
