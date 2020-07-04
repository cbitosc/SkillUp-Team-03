from flask_restful import Resource, reqparse
from db import query
import pymysql
from flask_jwt_extended import jwt_required

# this resource is for the user to get info about the uploads user made
class GetUploads(Resource):
    
    @jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('uname', type=str, help="uname cannot be left blank!")
        parser.add_argument('request_no', type=int, help="request_no cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" 
        SELECT * FROM User.submissions
        WHERE uname = '{ data['uname'] }' AND 
        request_no = '{ data['request_no'] }' ;
        """
        try:
            return query(qstr, connect_db='User')
        except Exception as e:
            return {
                "message" : "There was an error connecting to the database while retrieving." + str(e)
            }, 500
