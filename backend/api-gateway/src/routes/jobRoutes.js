const { Router } = require('express');
const {authGatewayFilter} = require('../middlewares/authGatewayMiddleware');
const jobController = require('../controller/jobController')

const router = Router();

router.get('/:id/status',  authGatewayFilter(), jobController.getStatus);

router.get('/:id/result',  authGatewayFilter(), jobController.getResult);

module.exports = router;
