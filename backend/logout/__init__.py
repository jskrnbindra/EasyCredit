import json
import logging
import os
import azure.functions as func

from pymongo import MongoClient
from bson.objectid import ObjectId

# COMMON UTILS 

env = os.environ
if 'MONGO_CONN_STR' not in env:
    logging.error('Mongo connection string missing in environment')

MONGO_CONN_STR = env['MONGO_CONN_STR']
SUCCESS={'message':'success'}
NOT_FOUND = {'message':'Invalid email or password.'}
ALREADY_EXISTS = {'message':'User with that phone number exists already.'}

mongo = MongoClient(MONGO_CONN_STR)                       # Server
db = mongo.easycredit                                     # Database
users, sessions = db.users, db.sessions                   # Collections

def response(body=SUCCESS, status_code=200):
    return func.HttpResponse(json.dumps(body), status_code=status_code)

def logged_in(user):
    active_session = sessions.find_one({'userId': user['id']})
    if active_session:
        logging.info(f"Already logged in: {user['id']}")
        return str(active_session['_id'])
    else:
        logging.info(f"Not logged in: {user['id']}")
        return False

def get_user_by(phone):
    logging.info(f'Getting user by phone: {phone}')
    return users.find_one({'phone': phone})


# THIS FUNCTION

def logout(session_id):
    sessions.delete_one({'_id': ObjectId(session_id)})
    logging.info(f'Logged out {session_id}')

def main(req: func.HttpRequest) -> func.HttpResponse:
    session_id = req.params.get('session_id')
    logout(session_id)
    return response()
