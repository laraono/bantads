const { Router } = require('express');
const jobController = require('../controller/jobController')

// autenticação é aplicada em server.js, antes deste router e do proxy do MS Gerente
const router = Router();

router.get('/:id/status', jobController.getStatus);

router.get('/:id/result', jobController.getResult);

module.exports = router;
