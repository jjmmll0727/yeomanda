const express = require('express');
const router = express.Router();
const menuBarController = require('../../controller/menuBarController');

router.get('/showFavoriteTeamName', menuBarController.showFavoriteTeamName);
router.get('/deleteFavorite/:team_no', menuBarController.deleteFavorite);
router.get('/finishTravel', menuBarController.finishTravel);
router.get('/showFavoritesDetail/:teamName', menuBarController.showFavoritesDetail)

module.exports = router;