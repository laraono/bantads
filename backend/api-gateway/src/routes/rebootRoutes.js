const { Router } = require('express');
const rebootController = require('../controller/rebootController');

const router = Router();

router.post('/reboot', rebootController.reboot);

module.exports = router;
