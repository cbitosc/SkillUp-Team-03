from flask_restful import Resource, reqparse
from db import query
from flask_jwt_extended import create_access_token, jwt_required
from werkzeug.security import safe_str_cmp
import pymysql

# this parameter is given globally in this module so that the userdb is changed all over the module if 
# changed at one place (here). userdb is set so that while testing locally, 
# the local database could have userdb different from 'User'. 
# In the database employed for this utility, userdb is 'User'
userdb = 'User'

# this resource is defined for the user to register
class UserRegister(Resource):

    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('uname', type=str, required=True, help="uname cannot be left blank!")
        parser.add_argument('password', type=str, required=True, help="password cannot be left blank!")
        parser.add_argument('rno', type=str, required=True, help="rno cannot be left blank!")
        parser.add_argument('branch_name', type=str, required=True, help="branch_name cannot be left blank!")
        parser.add_argument('sem_no', type=str, required=True, help="sem_no cannot be left blank!")
        data = parser.parse_args()
        
        try:
            qstr = f""" 
            SELECT uname from users where uname = "{ data['uname'] }";
            """
            usersWithUname = query(qstr, return_json=False, connect_db=userdb)
            
            qstr = f""" 
            SELECT uname from users where rno = "{ data['rno'] }";
            """
            usersWithRoll = query(qstr, return_json=False, connect_db=userdb)
        
        except Exception as e:
            return {
                "message" : "There was an error connecting to the Users table while checking for an existing user."  + str(e)
            }, 500

        if len(usersWithUname)>0:
            return {
                "message" : "A user with the same username exists."
            }, 400

        if len(usersWithRoll)>0:
            return {
                "message" : "A user with the same roll number exists."
            }, 400


        qstr = f""" INSERT INTO users values("{ data['uname'] }", 
        "{ data['password'] }", 
        "{ data['rno'] }", 
        "{ data['branch_name'] }", 
        "{ data['sem_no'] }" ); """

        try:
            query(qstr, connect_db=userdb)
        # except (pymysql.err.InternalError, pymysql.err.ProgrammingError, pymysql.err.IntegrityError) as e:
        #     return {
        #         "message" : "MySQL error: " + str(e)
        #     }, 500
        # except Exception as e:
        #     return {
        #         "message" : "Cannot create a user." + str(e)
        #     }, 500
        except:
            return {
                "message" : "Cannot create the user."
            }, 500
        
        return {
            "message" : "Succesfully registered."
        }, 200