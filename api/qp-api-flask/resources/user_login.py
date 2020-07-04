from flask_restful import Resource, reqparse
from db import query
from flask_jwt_extended import create_access_token, jwt_required
from werkzeug.security import safe_str_cmp

# this parameter is given globally in this module so that the userdb is changed all over the module if 
# changed at one place (here). userdb is set so that while testing locally, 
# the local database could have userdb different from 'User'. 
# In the database employed for this utility, userdb is 'User'
userdb = 'User'

#User class is used create a User object and also use class methods to
#execute queries and return a User object for it 
class User():
    def __init__(self, uname, password, rno, branch_name, sem_no):
        self.uname = uname
        self.password = password

        #testing features
        self.rno = rno
        self.branch_name = branch_name
        self.sem_no = sem_no

    @classmethod
    def getUserByUname(cls,uname):
        result=query(f"""SELECT uname,password,rno,branch_name,sem_no FROM users WHERE uname='{uname}'""", 
        return_json=False, 
        connect_db=userdb)
        
        if len(result)>0: return User(result[0]['uname'],result[0]['password'],
        result[0]['rno'],result[0]['branch_name'],result[0]['sem_no'])
        return None

# This resource is defined for the user to login.
class UserLogin(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('uname', type=str, required=True,
                            help="uname cannot be left blank!")
        parser.add_argument('password', type=str, required=True,
                            help="password cannot be left blank!")
        data=parser.parse_args()
        user=User.getUserByUname(data['uname'])
        if User and safe_str_cmp(user.password,data['password']):
            access_token=create_access_token(identity=user.uname,expires_delta=False)
            return {'access_token':access_token,
            'uname':user.uname,
            'rno':user.rno,
            'branch_name':user.branch_name,
            'sem_no':user.sem_no},200
        return {"message":"Invalid Credentials!"}, 401