from flask_restful import Resource, reqparse
from db import connectToHost
import pymysql
from flask_jwt_extended import jwt_required


""" 
The resource in this module is used to delete all the entries(requests) in the requests table 
having a particular 'request_no' with a 'select_status' = 0, 
other than the one with the same 'request_no' having a particular 'r_id'. After that the 
for the undeleted entry with the r_id, select_status is set to 1 
"""

class AdminDeleteReq(Resource):
    
    @jwt_required
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('request_no', type=int, required=True, help="request_no cannot be left blank!")
        parser.add_argument('r_id', type=int, required=True, help="r_id cannot be left blank!")
        data = parser.parse_args()


        # a transaction is made, so not using query function from db module
        # we use connectToHost function from db module and commit explicitly
        # the query function from db module commits for each query which is not desirable in 
        # a transaction sequence as follows.
        # here we execute several queries then commit.
        try:
            connection = connectToHost()
            
            #start connection, create cursor and execute query from cursor
            connection.begin()
            cursor = connection.cursor()
            qstr = f""" DELETE FROM requests
            WHERE request_no = { data['request_no'] } AND 
            r_id <> { data['r_id'] } AND 
            select_status = 0; """
            cursor.execute(qstr)
            
            qstr = f""" UPDATE requests
            SET select_status = 1
            WHERE r_id = { data['r_id'] };
            """
            cursor.execute(qstr)
            
            
            # delete the corresponding entry from active_exams if paper is accepted.
            qstr = f""" DELETE FROM active_exams
            WHERE request_no = { data['request_no'] }; """
            cursor.execute(qstr)

            # for deleting user info from submissions table
            qstr = f""" DELETE FROM User.submissions
            WHERE request_no = { data['request_no'] }; 
            """
            cursor.execute(qstr)

            connection.commit() #commit the changes made
            #close the cursor and connection
            cursor.close()
            connection.close()

        except (pymysql.err.InternalError, pymysql.err.ProgrammingError, pymysql.err.IntegrityError) as e:
            return {
                "message" : "MySQL error: " + str(e)
            }, 500
        except Exception as e:
            return {
                "message" : "There was an error connecting to the requests table while inserting." + str(e)
            }, 500
        
        return {
            "message" : "Succesfully deleted."
        }, 200