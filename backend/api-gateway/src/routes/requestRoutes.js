const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const requestController = require('../controller/requestController');

const router = Router();

router.get('/', requestController.listRequests);

router.post('/', requestController.createRequest);

router.get('/:id', requestController.getRequest);

router.post('/:id/aprovacao', requestController.approveRequest);

router.post('/:id/rejeicao', requestController.rejectRequest);

module.exports = router;
