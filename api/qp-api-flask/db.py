from flask import jsonify
from decimal import Decimal
import pymysql
import base64
import datetime

"""args_tuple is used to avoid syntax errors while inserting BLOB data. A tuple of arguments is sent to the 
keyword argument 'args_tuple'. If args_tuple is not None, the query function is given 
the tuple of arguments to use in the query which is a format string.
return_json is True by default , if set to false it returns a list of dictionaries for debugging"""

deafult_host = 'skillup-team-03.cxgok3weok8n.ap-south-1.rds.amazonaws.com'
default_user = 'admin'
default_password = 'coscskillup'
default_db = 'Admin'


def query(querystr, args_tuple=None, return_json=True, connect_db=default_db):

    #create connection object
    connection = pymysql.connect(host=deafult_host,
                                 user=default_user,
                                 password=default_password,
                                 db=connect_db,
                                 cursorclass=pymysql.cursors.DictCursor)

    
    #start connection, create cursor and execute query from cursor
    connection.begin()
    cursor = connection.cursor()

    #if query string is a string to be formatted, we pass args_tuple to insert in the string
    #using args_tuple is useful because syntax errors arise when trying to insert blob directly
    if args_tuple:
        cursor.execute(querystr,args_tuple)
    else:
        cursor.execute(querystr)

    #convert any decimal values to strings using encode function defined at the bottom
    result = encode(cursor.fetchall())
    connection.commit() #commit the changes made
    
    #close the cursor and connection
    cursor.close()
    connection.close()
    
    
    if return_json == True:
        return jsonify(result) #convert the query result to JSON
    else:
        return result #returns non JSON format of the query result for debugging


def getBase64Str(value):
    return base64.b64encode(value).decode('utf-8')

# encode function converts decimals to strings 
# and also converts BLOB files 
# which are in 'bytes' datatype to a base64 encoded string

# encode time and date values so that they are 
# readable and user friendly
def encode(data):
    #iterate through rows
    for row in data:
        for key, value in row.items():
            if isinstance(value, Decimal):
                row[key] = str(value)
            elif isinstance(value, bytes):
                row[key] = getBase64Str(value)
            elif isinstance(value,datetime.timedelta):
                row[key] = str(value)
            elif isinstance(value,datetime.date):
                row[key] = str(value)
                
    return data

def connectToHost(connect_db=default_db):
    
    return pymysql.connect(host=deafult_host,
                            user=default_user,
                            password=default_password,
                            db=connect_db,
                            cursorclass=pymysql.cursors.DictCursor)

"""
if result of a query stored in r, which is non json, and if its a single row, then to obtain 
use the following code
val = list(r[0].values())[0]
print(val)
"""
