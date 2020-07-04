from flask_restful import Resource, reqparse
from db import query
import pymysql
from flask_jwt_extended import jwt_required


# this resource is for the users to get papers for a subject yearwise
class GetYearwise(Resource):

    #@jwt_required    
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('branch_name', type=str, help="branch_name cannot be left blank!")
        parser.add_argument('sem_no', type=int, help="sem_no cannot be left blank!")
        parser.add_argument('exam_type', type=str, help="exam_type cannot be left blank!")
        parser.add_argument('subtype', type=str, help="subtype cannot be left blank!")
        parser.add_argument('subject_name', type=str, help="subject_name cannot be left blank!")
        data = parser.parse_args()
        #create query string
        qstr = f""" 
        select request_no , subject_name , date

        from timetable t 
        inner join details d on (t.d_id = d.d_id)
        inner join subject s on (t.s_code = s.s_code)
        inner join branch b on (b.b_id = t.b_id)

        WHERE branch_name = "{ data['branch_name'] }" AND 
        sem_no = "{ data['sem_no'] }" AND 
        exam_type = "{ data['exam_type'] }" AND 
        subtype = "{ data['subtype'] }" AND 
        subject_name = "{ data['subject_name'] }"

        ORDER BY date desc;
        """
        try:
            return query(qstr)
        except Exception as e:
            return {
                "message" : "There was an error connecting to the database while retrieving." + str(e)
            }, 500
