from flask_restful import Resource, reqparse
import pymysql
import db
from db import connectToHost
from flask_jwt_extended import jwt_required

# This resource is for the admin to create a timetable (data inserted in multiple tables based on inputs)
class AdminTimeTableCreate(Resource):
    
    # @jwt_required
    # def get(self):
    #     parser = reqparse.RequestParser()
    #     parser.add_argument('request_no', type=int, help="request_no cannot be left blank!")
    #     data = parser.parse_args()
    #     #create query string
    #     qstr = f""" SELECT * FROM timetable where request_no = { data['request_no'] };"""
    #     try:
    #         return db.query(qstr)
    #     except:
    #         return {
    #             "message" : "There was an error connecting to the timetable table while retrieving."
    #         }, 500

    @jwt_required
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('b_id', type=int, required=True, help="b_id cannot be left blank!")
        #parser.add_argument('d_id', type=int, required=True, help="d_id cannot be left blank!")
        parser.add_argument('s_code', type=str, required=True, help="s_code cannot be left blank!")
        parser.add_argument('exam_type', type=str, required=True, help="exam_type cannot be left blank!")
        parser.add_argument('subtype', type=str, required=True, help="subtype cannot be left blank!")

        parser.add_argument('start_at', type=str, required=True, help="start_at cannot be left blank!")
        parser.add_argument('end_at', type=str, required=True, help="end_at cannot be left blank!")
        parser.add_argument('date', type=str, required=True, help="date cannot be left blank!")
        parser.add_argument('year', type=int, required=True, help="year cannot be left blank!")
        parser.add_argument('sem_no', type=int, required=True, help="sem_no cannot be left blank!")
        # parser.add_argument('subject_name', type=str, required=True, help="subject_name cannot be left blank!")
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

            #insert into details if not present already
            qstr = f""" 
            INSERT INTO details (start_at, end_at, date, year, sem_no)
            SELECT * FROM (SELECT '{data['start_at']}' as st, 
            '{data['end_at']}' as en, 
            '{data['date']}' as da, 
            '{data['year']}' as ye , 
            '{data['sem_no']}' as se) as temp
            WHERE NOT EXISTS (
                SELECT d_id FROM details WHERE 
                start_at = '{data['start_at']}' AND
                end_at = '{data['end_at']}' AND
                date = '{data['date']}' AND
                year = '{data['year']}' AND
                sem_no = '{data['sem_no']}'
            ) LIMIT 1;
            """

            cursor.execute(qstr)

            # obtain d_id of the timings given, the query returns the old value of d_id if timings and sem no
            # didnt change and returns new value of d_id if timings are updated
            qstr = f"""
            SELECT d_id FROM details WHERE 
                        start_at = '{data['start_at']}' AND
                        end_at = '{data['end_at']}' AND
                        date = '{data['date']}' AND
                        year = '{data['year']}' AND
                        sem_no = '{data['sem_no']}';
            """

            cursor.execute(qstr)
            result = cursor.fetchall()
            did = list(result[0].values())[0]
            
            # insert the timetable into the timetable table if its not already present.
            qstr = f""" 
            INSERT INTO timetable (b_id, d_id, s_code, exam_type, subtype) 
            SELECT * FROM (
            SELECT '{data['b_id']}' as b, 
            '{did}' as d, 
            '{data['s_code']}' as s, 
            '{data['exam_type']}' as ex, 
            '{data['subtype']}' as su) AS TEMP
            WHERE NOT EXISTS (
                SELECT request_no FROM timetable WHERE 
                b_id = '{data['b_id']}' AND 
                d_id = '{did}' AND
                s_code = '{data['s_code']}' AND
                exam_type = '{data['exam_type']}' AND
                subtype = '{data['subtype']}'
            ) LIMIT 1;
            """

            cursor.execute(qstr)

            #for active_exams table
            qstr = f"""SELECT subject_name FROM subject 
            WHERE s_code = '{ data['s_code'] }';"""

            cursor.execute(qstr)
            result = cursor.fetchall()
            sname = list(result[0].values())[0]

            qstr = f"""SELECT branch_name FROM branch 
            WHERE b_id = '{ data['b_id'] }';"""

            cursor.execute(qstr)
            result = cursor.fetchall()
            bname = list(result[0].values())[0]
            
            qstr = f"""SELECT request_no FROM timetable WHERE 
            b_id = '{ data['b_id'] }' AND 
            s_code = '{ data['s_code'] }' AND 
            exam_type = '{ data['exam_type'] }' AND 
            subtype = '{ data['subtype'] }' AND 
            d_id = '{ did }' ;"""
            
            cursor.execute(qstr)
            result = cursor.fetchall()
            reqno = list(result[0].values())[0]
            
            qstr = f"""
            INSERT INTO active_exams
            SELECT * FROM (
            SELECT '{ reqno }' as r, 
            '{ bname }' as b, 
            '{ sname }' as s, 
            '{ data['exam_type'] }' as e, 
            '{ data['subtype'] }' as su, 
            '{ data['end_at'] }' as en, 
            '{ data['date'] }' as d, 
            '{ data['sem_no'] }' as se) AS TEMP
            WHERE NOT EXISTS (
                SELECT request_no FROM active_exams WHERE 
                branch_name = '{ bname }' AND 
                subject_name = '{ sname }' AND 
                exam_type = '{ data['exam_type'] }' AND 
                subtype = '{ data['subtype'] }' AND 
                end_at = '{ data['end_at'] }' AND 
                date = '{ data['date'] }' AND 
                sem_no = '{ data['sem_no'] }'
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