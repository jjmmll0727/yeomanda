const express = require('express');
const router = express.Router();
const menuBarController = require('../../controller/menuBarController');
const { upload } = require('../../middlewares/uploadS3')


router.get('/showFavoriteTeamName', menuBarController.showFavoriteTeamName);
router.get('/deleteFavorite/:team_no', menuBarController.deleteFavorite);
router.get('/finishTravel', menuBarController.finishTravel);
router.get('/showFavoritesDetail/:teamName', menuBarController.showFavoritesDetail);
router.get('/getProfile', menuBarController.getProfile);
router.post('/updateProfile', upload.array('files'), menuBarController.updateProfile);
//router.post('/updateProfile', menuBarController.updateProfile);


module.exports = router;