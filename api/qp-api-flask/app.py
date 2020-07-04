from flask import Flask, jsonify
from flask_restful import Api
from flask_jwt_extended import JWTManager

#import resources
from resources.admin_qp_request import AdminQpRequest
from resources.admin_timetable_create import AdminTimeTableCreate
from resources.admin_false_select import AdminFalseSelect
from resources.admin_reqno_details import AdminReqNoDetails 
from resources.admin_delete_req import AdminDeleteReq
from resources.admin_timetable_update import AdminTimeTableUpdate
from resources.admin_timetable_delete import AdminTimeTableDelete
from resources.admin_get_subjects import AdminGetSubjects
from resources.admin_get_timetable import AdminGetTimeTable

#user part
from resources.qp_request import QpRequest
from resources.get_subjects import GetSubjects
from resources.get_yearwise import GetYearwise
from resources.get_active_exams import GetActiveExams
from resources.qp_update import QpUpdate
from resources.qp_delete import QpDelete
from resources.get_uploads import GetUploads
from resources.get_uname_info import GetUnameInfo

#authentication
from resources.admin_login import AdminLogin
from resources.user_login import UserLogin
from resources.user_register import UserRegister

# create flask app instance
app = Flask(__name__)

#set config for jwt
app.config['PROPAGATE_EXCEPTIONS']=True
app.config['JWT_SECRET_KEY'] = 'qp-cbit'

#initialize api
api = Api(app)

api.add_resource(QpRequest, '/qpreq') #for user to upload paper
api.add_resource(AdminQpRequest, '/admin-qpreq')#for admin to upload paper with an optional feature to set select_status=1
api.add_resource(AdminTimeTableCreate, '/admin-timetable-create') #for admin to upload timetable
api.add_resource(AdminFalseSelect, '/admin-false-select') #for admin to retrieve papers with false or 0 select_status
api.add_resource(AdminReqNoDetails, '/admin-reqno-details') #for admin to get details for a particular request_no
# the below one is for admin to delete question papers with select_status=0 having a particular request_no
# other than the ones with same request_number and a particular r_id  
api.add_resource(AdminDeleteReq, '/admin-delete-req')
api.add_resource(AdminTimeTableUpdate, '/admin-timetable-update') #update a timetable, send the same fields provided while uploading
api.add_resource(AdminTimeTableDelete, '/admin-timetable-delete') #delete a timetable, with the request_no
api.add_resource(AdminGetSubjects, '/admin-get-subjects') #retrieve all subjects 
# the below one is to retrieve timetable from from b_id, sem_no, exam_type, subtype, s_code, year fields
api.add_resource(AdminGetTimeTable, '/admin-get-timetable') 

#user endpoints
api.add_resource(GetSubjects, '/get-subjects') #get subjects list for a branch, sem, exam_type and subtype 
api.add_resource(GetYearwise, '/get-yearwise') #get yearwise papers for a subject
api.add_resource(GetActiveExams, '/get-active-exams') #get active exams
api.add_resource(QpUpdate, '/qp-update') #update question paper
api.add_resource(QpDelete, '/qp-delete') #delete question paper
api.add_resource(GetUploads, '/get-uploads') #to get uploads
api.add_resource(GetUnameInfo, '/get-uname-info') #get branch and sem_no of a uname

#authentication endpoints
api.add_resource(AdminLogin, '/admin-login') #admin login
api.add_resource(UserLogin, '/user-login') #user login
api.add_resource(UserRegister, '/user-register') #user register

jwt=JWTManager(app)

#return an error response if JWT is missing
@jwt.unauthorized_loader
def missing_token_callback(error):
    return jsonify({
        'error': 'authorization_required',
        "description": "Request does not contain an access token."
    }), 401

#return an error response if JWT is invalid
@jwt.invalid_token_loader
def invalid_token_callback(error):
    return jsonify({
        'error': 'invalid_token',
        'message': 'Signature verification failed.'
    }), 401

#a welcome route to test if the flask app is working.
@app.route('/')
def home():
    return(f"""<h1 style="font-family: 'Palatino Linotype';">This is an API for the CBIT question paper management utility.</h1>
                <p style="font-size:2em">Developed by Harish Akula</p>""")

# set debug = False while deploying. debug = True is not safe in production environments
if __name__ == '__main__':
    app.run(debug=False)
