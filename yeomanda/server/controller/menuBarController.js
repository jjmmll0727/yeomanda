const util = require('../modules/util');
const fs = require('fs')
const path = require('path');
const responseMessage = require('../modules/responseMessage');
const statusCode = require('../modules/statusCode');
const stream = require('stream')

// dynamodb
const AWS = require('aws-sdk')
const favoriteConfig = require('../config/aws/Favorite')
const userConfig = require('../config/aws/User')

// rds mysql
const mysql = require("mysql2/promise");
const conn = require('../config/aws/Travelers');

// s3 getObject
const s3 = new AWS.S3({
    accessKeyId: userConfig.aws_iam_info.accessKeyId,
    secretAccessKey: userConfig.aws_iam_info.secretAccessKey,
    region : 'ap-northEast-2'
});

/**
 * 즐겨찾기한 팀들의 팀 이름들을 보여줌.
 */
const showFavoriteTeamName = async(req, res) => {
    try{
        const connection = await mysql.createConnection(conn.db_info);
        const userEmail = req.decoded.email
        AWS.config.update(favoriteConfig.aws_iam_info);
        const docClient = new AWS.DynamoDB.DocumentClient();
        const result = []  //  최종 결과

        /**
         * 1. find email in FAVORITES table 
         */

         const params_find_favorites = {
            TableName : favoriteConfig.aws_table_name,
            KeyConditionExpression: 'email = :i',
            ExpressionAttributeValues: {
                ':i' : userEmail
            }   
        };
        const checkEmail_from_favorite = await docClient.query(params_find_favorites).promise()
        const Favorites_no = checkEmail_from_favorite.Items[0] // 이용자가 즐겨찾기 한 팀들의 번호 리스트
        // 1. 즐겨찾기 한 팀들이 없을 경우
        if(!Favorites_no){
            return res.status(statusCode.OK).send(util.success(statusCode.fail, responseMessage.READ_USER_FAIL, result))                                        
        }
        /**
         * 2. 즐겨찾기 한 팀들이 있을 경우
         * 즐겨찾기 한 팀들의 번호 리스트를 가지고 travel_with 테이블에서 팀 이름들을 보내준다.
         */

        const Favorites = checkEmail_from_favorite.Items[0].favorite_team_no// 이용자가 즐겨찾기 한 팀들의 번호 리스트

        for(var i in Favorites){
            const sql = `select * from travel_plan where team_no = '${Favorites[i]}';`
            const data = await connection.query(sql)
                
            /**
             * filter 로 이메일 데이터만 뽑아서 따로 저장
             * async 하는 동안 for 문 다 돌았어 -> 그래서 마지막 팀이 계속 배열로 들어간다...
             */
            
            const teamNameList = []
            
            data[0].filter( e => { // data 에는 하나의 팀에 해당하는 여러 여행객들의 정보가 담겨져 있다. 
                teamNameList.push(e.team_name)
            })
            result.push(teamNameList[0])
            if(result.length === Favorites.length){
                return res.status(statusCode.OK).send(util.success(statusCode.OK, responseMessage.QUERY_SUCCESS, result ))                                        
            }
        }
    }catch(err){
        console.log(err)
        return res.send(util.fail(statusCode.INTERNAL_SERVER_ERROR, responseMessage.TRY_CATCH_ERROR, "tryCatchError"))
    }
}

/**
 * 즐겨찾기 취소하기 - 즐겨찾기 리스트 보고 나서 취소할 수 있어.
 */
const deleteFavorite = async(req, res) => {
    try{
        const connection = await mysql.createConnection(conn.db_info);

        const deletedTeam = req.params.team_no
        const deleter = req.decoded.email
        AWS.config.update(favoriteConfig.aws_iam_info);
        const docClient = new AWS.DynamoDB.DocumentClient();

        /**
         * 1. find email in FAVORITES table and delete favorite team_no
         */
        const params_findFromUser = {
            TableName : favoriteConfig.aws_table_name,
            KeyConditionExpression: 'email = :i',
            ExpressionAttributeValues: {
                ':i' : deleter
            }   
        };
        const checkEmail_from_favorite = await docClient.query(params_findFromUser).promise()
        const newFavorites = checkEmail_from_favorite.Items[0].favorite_team_no // favorite list after delete
        
        for(var i in newFavorites){
            if(newFavorites[i].toString() === deletedTeam){
                if (i > -1) {
                    newFavorites.splice(i, 1); 
                }               
            }
        }
        /**
         * 2. delete row of user email
         */
        const params_to_delete_favorite = { 
            TableName : favoriteConfig.aws_table_name, 
            Key: {
                "email" : deleter 
            },
        };

        docClient.delete(params_to_delete_favorite, function(err, data){
            if(err){
                return res.status(statusCode.OK).send(util.fail(statusCode.INTERNAL_SERVER_ERROR, responseMessage.QUERY_ERROR))
            } else {
                /**
                 * 3. delete success & push new favorite list that deleted a team_no
                 */
                const params_to_push_favorite = {
                    TableName : favoriteConfig.aws_table_name,
                    Item : {
                        email : deleter,
                        favorite_team_no : newFavorites
                    }
                };  
                docClient.put(params_to_push_favorite, function(err, data){
                    if(err){
                        return res.status(statusCode.INTERNAL_SERVER_ERROR).send(util.fail(statusCode.INTERNAL_SERVER_ERROR, responseMessage.QUERY_ERROR))
                    } else {
                        return res.status(statusCode.OK).send(util.success(statusCode.OK, responseMessage.DELETE_FAVORITE))
                    }
                })
            }
        })
    }catch(err){
        return res.send(util.fail(statusCode.INTERNAL_SERVER_ERROR, responseMessage.TRY_CATCH_ERROR, "tryCatchError"))
    }
    
}

/**
 * 여행 취소하기
 */
const finishTravel = async(req, res) => {
    try{
        const connection = await mysql.createConnection(conn.db_info);

        const finishTraveler = req.decoded.email
        /**
         * 1. find email in database that field 'isfinished=0' 
         * 2. change isfinished filed to 1
         */
        const sql = `select team_no from travel_plan where email='${finishTraveler}' and isfinished = '0';` 
        const result = await connection.query(sql)
             
        
        const finishTeam = result[0][0].team_no
        const sql_to_finish = `update travel_plan set isfinished = '1' where team_no = '${finishTeam}'`
        const data = await connection.query(sql_to_finish)   
        return res.status(statusCode.OK).send(util.success(statusCode.OK, responseMessage.QUERY_SUCCESS, 
            "success to update isfinished to 1")) 

    }catch(err){
        console.log(err)
        return res.send(util.fail(statusCode.INTERNAL_SERVER_ERROR, responseMessage.TRY_CATCH_ERROR, "tryCatchError"))
    }
}


const showFavoritesDetail = async(req, res) => {
    const connection = await mysql.createConnection(conn.db_info);

    const teamName = req.params.teamName
    const sql = `select email from travel_plan where team_name = '${teamName}';`
    const data = await connection.query(sql)
       
    const result = []
    data[0].filter( async(d) => {
        const user = d.email
        AWS.config.update(userConfig.aws_iam_info);
        const docClient = new AWS.DynamoDB.DocumentClient();

        /**
         * 1. find email in FAVORITES table and delete favorite team_no
         */
        const params_findFromUser = {
            TableName : userConfig.aws_table_name,
            KeyConditionExpression: 'email = :i',
            ExpressionAttributeValues: {
                ':i' : user
            }   
        };
        const checkEmail = await docClient.query(params_findFromUser).promise()
        const file_root = 'https://yeomanda-userface.s3.ap-northeast-2.amazonaws.com/'
        const fileList = []
        for(var i in checkEmail.Items[0].files){
            fileList.push(file_root + checkEmail.Items[0].files[i])
        }
        const userInfo = {
            'email' : checkEmail.Items[0].email,
            'name' : checkEmail.Items[0].name,
            'sex' : checkEmail.Items[0].sex,
            'birth' : checkEmail.Items[0].birth,
            'files' : fileList
        }
        result.push(userInfo)
        if(result.length === data[0].length){
            return res.status(statusCode.OK).send(util.success(statusCode.OK, responseMessage.QUERY_SUCCESS, 
                result)) 
        }
    })
}
const getMyProfile = async(req, res) => {
    const userEmail = req.decoded.email


}
const updateProfile = async(req, res) => {
    const userEmail = req.decoded.email
}

module.exports = {
    showFavoriteTeamName,
    deleteFavorite,
    finishTravel,
    showFavoritesDetail,
    updateProfile,
    getMyProfile
}