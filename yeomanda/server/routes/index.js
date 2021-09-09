const express = require('express');
const router = express.Router();
const verifyToken = require('../middlewares/verifyToken');

router.use('/user', require('./user'));
router.use('/travelers', verifyToken.checkToken, require('./travelers'));
router.use('/markup', verifyToken.checkToken, require('./markup'));

router.get('/', function(req, res, next) {
    res.render('index', { title: ' yeomanda start !!! ' });
})


module.exports = router;
