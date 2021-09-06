const express = require('express');
const router = express.Router();
//const verifyToken = require('../middlewares/verifyToken');

router.use('/user', require('./user'));
router.use('/travelers', require('./travelers'));
router.use('/markup', require('./markup'));

module.exports = router;