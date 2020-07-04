from flask_restful import Resource, reqparse
from db import query, connectToHost
import base64
import pymysql
from flask_jwt_extended import jwt_required

def convertToBlob(value):
    return base64.b64decode(value.encode('utf-8'))

"""
Using the resource in this module, admin can insert an image with select_status = 1.
The admin has to send just the request_no and the image.
All the other papers corresponding to the request_no get deleted 
whether they have a select_status of 1 or not.
The entries in the requests table are deleted for a request_no, admin uploaded image is insered into requests.
Then active_exams and submissions table entries are deleted which have the request_no sent.
"""

class AdminQpRequest(Resource):
    
    @jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('r_id', type=int, help="r_id cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" SELECT * FROM requests where r_id = { data['r_id'] };"""
        try:
            return query(qstr)
        except:
            return {
                "message" : "There was an error connecting to the requests table while retrieving."
            }, 500

    @jwt_required
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('b_id', type=int, required=True, help="b_id cannot be left blank!")
        parser.add_argument('sem_no', type=int, required=True, help="sem_no cannot be left blank!")
        parser.add_argument('exam_type', type=str, required=True, help="exam_type cannot be left blank!")
        parser.add_argument('subtype', type=str, required=True, help="request_no cannot be left blank!")
        parser.add_argument('s_code', type=str, required=True, help="s_code cannot be left blank!")
        parser.add_argument('year', type=int, required=True, help="year cannot be left blank!")
        parser.add_argument('image', type=str, required=True, help="image cannot be left blank!")
        
        #parser.add_argument('select_status', type=int, required=False, default = 0)
        
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

            #obtain request_no from the details provided, store in req_no

            qstr = f"""
            select DISTINCT request_no
            from timetable t 
            inner join details d on (t.d_id = d.d_id)
            WHERE b_id = '{data['b_id']}' AND 
            sem_no = '{data['sem_no']}' AND 
            exam_type = '{data['exam_type']}' AND 
            subtype = '{data['subtype']}' AND 
            year = '{data['year']}' AND
            s_code = '{data['s_code']}'
            LIMIT 1;
            """

            cursor.execute(qstr)
            cursor.execute(qstr)
            result = cursor.fetchall()
            req_no = list(result[0].values())[0]
            
            # delete all the other entries in requests table 
            # with the same request_no, whether selected or not
            qstr = f"""
            delete from requests
            where request_no = {req_no};
            """
            
            cursor.execute(qstr)

            #creating a tuple of values to be inserted because a formatted string is used
            #here its useful to avoid SQL syntax errors while inserting BLOB value into table
            vals_tuple = (req_no, convertToBlob(data['image']), 1 ) #set select status to 1
            #convertToBlob is used to convert base64 string to BLOB data

            qstr = f""" INSERT INTO requests (request_no, image, select_status)
                        values (%s, %s, %s); """

            cursor.execute(qstr, vals_tuple)

            # delete the corresponding entry from active_exams if paper is uploaded.
            qstr = f""" DELETE FROM active_exams
            WHERE request_no = { req_no }; """
            cursor.execute(qstr)

            # for deleting user info from submissions table
            qstr = f""" DELETE FROM User.submissions
            WHERE request_no = { req_no }; 
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