from flask_restful import Resource, reqparse
from db import query
import base64
import pymysql
from flask_jwt_extended import jwt_required


# this resource is to obtain th branch_name and sem_no for a user of uname
class GetUnameInfo(Resource):
    
    @jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('uname', type=str, help="uname cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" 
        select rno, branch_name, sem_no
        from users where uname = '{ data['uname'] }' LIMIT 1;
        """
        try:
            return query(qstr, connect_db='User')
        except Exception as e:
            return {
                "message" : "There was an error connecting to the database while retrieving." + str(e)
            }, 500
