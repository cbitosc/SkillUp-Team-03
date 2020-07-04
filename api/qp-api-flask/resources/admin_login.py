from flask_restful import Resource, reqparse
from db import query
from flask_jwt_extended import create_access_token, jwt_required
from werkzeug.security import safe_str_cmp

#Admin class is used create a Admin object and also use class methods to
#execute queries and return a Admin object for it 
class Admin():
    def __init__(self,uname,password):
        self.uname=uname
        self.password=password

    @classmethod
    def getAdminByUname(cls,uname):
        result=query(f"""SELECT uname,password FROM admins WHERE uname='{uname}'""",return_json=False)
        if len(result)>0: return Admin(result[0]['uname'],result[0]['password'])
        return None

# This resource is for the admin to login
class AdminLogin(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('uname', type=str, required=True,
                            help="uname cannot be left blank!")
        parser.add_argument('password', type=str, required=True,
                            help="password cannot be left blank!")
        data=parser.parse_args()
        admin=Admin.getAdminByUname(data['uname'])
        if admin and safe_str_cmp(admin.password,data['password']):
            access_token=create_access_token(identity=admin.uname,expires_delta=False)
            return {'access_token':access_token},200
        return {"message":"Invalid Credentials!"}, 401