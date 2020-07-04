from flask_restful import Resource, reqparse
from db import connectToHost
import base64
import pymysql
from flask_jwt_extended import jwt_required

def convertToBlob(value):
    return base64.b64decode(value.encode('utf-8'))

# this resource is for the user to delete a question paper uploaded
class QpDelete(Resource):

    #@jwt_required
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('request_no', type=int, required=True, help="request_no cannot be left blank!")
        parser.add_argument('uname', type=str, required=True, help="uname cannot be left blank!")
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

            #check if any paper got approved before allowing to perform action
            qstr = f"""
            select count(request_no) from requests
            where request_no = { data['request_no'] } and select_status = 1;
            """

            cursor.execute(qstr)
            result = cursor.fetchall()
            approved_count = list(result[0].values())[0]
            
            if approved_count > 0:
                return {
                    "message" : """Cannot delete. Admin accepted some paper already. 
                    The paper you uploaded is either approved or got deleted."""
                }, 400

            #perform deletion if any paper did not get approved
            qstr = f"""
            SELECT r_id FROM User.submissions WHERE 
            request_no = "{ data['request_no'] }" AND 
            uname = "{ data['uname'] }" LIMIT 1;
            """
            print(qstr)
            cursor.execute(qstr)
            result = cursor.fetchall()
            insert_rid = list(result[0].values())[0]    

            qstr = f""" DELETE FROM requests
                    WHERE request_no = '{ data['request_no'] }' AND  
                    r_id = '{ insert_rid }';"""
            
            print(qstr)


            cursor.execute(qstr)
            
            connection.commit() #commit the changes made
    
            #close the cursor and connection
            cursor.close()
            connection.close()       
            
        except IndexError:
            """
            this is to handle tuple index error 
            which is raised if no data could be retrieved and stored
            where data is retrieved in this way
            result = cursor.fetchall()
            req_no = list(result[0].values())[0] 
            """
            return {
                "message" : "Required data not present."
            }, 400

        except (pymysql.err.InternalError, pymysql.err.ProgrammingError, pymysql.err.IntegrityError) as e:
            return {
                "message" : "MySQL error: " + str(e)
            }, 500
        except Exception as e:
            return {
                "message" : "There was an error connecting to the requests table while deleting." + str(e)
            }, 500
        
        return {
            "message" : "Succesfully deleted."
        }, 200