from flask_restful import Resource, reqparse
from db import query
import pymysql
from flask_jwt_extended import jwt_required


# This resource is to retrieve timetable 
# from b_id, sem_no, exam_type, subtype, s_code, year fields.
class AdminGetTimeTable(Resource):
    
    #@jwt_required
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('b_id', type=int, required=True, help="b_id cannot be left blank!")
        parser.add_argument('sem_no', type=int, required=True, help="sem_no cannot be left blank!")
        parser.add_argument('exam_type', type=str, required=True, help="exam_type cannot be left blank!")
        parser.add_argument('subtype', type=str, required=True, help="request_no cannot be left blank!")
        parser.add_argument('s_code', type=str, required=True, help="s_code cannot be left blank!")
        parser.add_argument('year', type=int, required=True, help="year cannot be left blank!")

        
        data = parser.parse_args()
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
        try:
            result = query(qstr, return_json=False)
            req_no = list(result[0].values())[0]

            qstr = f"""
            select request_no, b_id, t.s_code, exam_type, subtype, start_at, end_at, date, year, sem_no
            from timetable t 
            inner join details d on (t.d_id = d.d_id)
            where request_no = { req_no };
            """

            return query(qstr)
        except IndexError:
            return {
                "message" : "No data present."
            }, 400

        except Exception as e:
            return {
                "message" : "There was an error connecting to the subject table while retrieving."+str(e)
            }, 500
