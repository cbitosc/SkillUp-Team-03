from flask_restful import Resource, reqparse
from db import query
import pymysql
from flask_jwt_extended import jwt_required

# this resource is for the user to get info about the entries in active_exams.
class GetActiveExams(Resource):
    
    @jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('branch_name', type=str, help="branch_name cannot be left blank!")
        parser.add_argument('sem_no', type=int, help="sem_no cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" 
        SELECT * FROM active_exams
        WHERE branch_name = '{ data['branch_name'] }' AND 
        sem_no = '{ data['sem_no'] }' ;
        """
        try:
            return query(qstr)
        except Exception as e:
            return {
                "message" : "There was an error connecting to the database while retrieving." + str(e)
            }, 500
