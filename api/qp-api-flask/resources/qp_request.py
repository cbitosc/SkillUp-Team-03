from flask_restful import Resource, reqparse
from db import query, connectToHost
import base64
import pymysql
from flask_jwt_extended import jwt_required

def convertToBlob(value):
    return base64.b64decode(value.encode('utf-8'))

# this resource is for the user to upload a question paper
class QpRequest(Resource):
    
    # get method is used for displaying paper of a particular exam to the user.
    #@jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('request_no', type=int, help="request_no cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" SELECT * FROM requests where request_no = { data['request_no'] } AND select_status = 1;"""
        try:
            return query(qstr)
        except:
            return {
                "message" : "There was an error connecting to the requests table while retrieving."
            }, 500

    # post method is for the user to upload an image for an exam
    # the user provides the request_no, image(base64 string) and uname(username)
    #@jwt_required
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('request_no', type=int, required=True, help="request_no cannot be left blank!")
        parser.add_argument('image', type=str, required=True, help="image cannot be left blank!")
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
                    "message" : "Cannot perform action. Admin accepted some other paper already."
                }, 400

            #perform upload if any paper did not get approved

            #creating a tuple of values to be inserted because a formatted string is used
            #here its useful to avoid SQL syntax errors while inserting BLOB value into table
            vals_tuple = (data['request_no'], convertToBlob(data['image']))
            #convertToBlob is used to convert base64 string to BLOB data

            qstr = f""" INSERT INTO requests (request_no, image)
                    values (%s, %s); """
            cursor.execute(qstr, vals_tuple)

            qstr = f"""SELECT LAST_INSERT_ID();"""
            cursor.execute(qstr)
            result = cursor.fetchall()
            insert_rid = list(result[0].values())[0]      

            qstr = f"""
            INSERT into User.submissions (r_id, request_no, uname)
            SELECT * FROM (SELECT '{ insert_rid }' as i, '{ data['request_no'] }' as r, '{ data['uname'] }' as u)
            AS TEMP
            WHERE NOT EXISTS(
                SELECT r_id FROM User.submissions
                where r_id = '{ insert_rid }' AND
                request_no = '{ data['request_no'] }' AND 
                uname = '{ data['uname'] }'
            ) LIMIT 1;
            """

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
                "message" : "There was an error connecting to the requests table while inserting." + str(e)
            }, 500
        
        return {
            "message" : "Succesfully inserted"
        }, 200