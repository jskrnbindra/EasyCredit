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
INVALID_CREDS = {'message':'Invalid phone or password.'}
NOT_FOUND = {'message':'Resource not found.'}
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

def get_user(phone, password):
    logging.info(f'Login request {phone}:{password}')
    
    from_db = users.find_one({'phone': phone, 'password': password})
    if not from_db:
        return None
    return {
        'id': str(from_db['_id']), 
        'phone': from_db['phone']
        }

def login(user):
    logging.info(f"Logging in user: {user['id']}")
    active_session = logged_in(user)
    if active_session:
        logging.info(f"Already logged in: {user['id']}")
        return active_session
    else:
        session = sessions.insert_one({'userId': user['id']})
        logging.info(f"Logged in: {user['id']}")
        return str(session.inserted_id)

def main(req: func.HttpRequest) -> func.HttpResponse:
    phone = req.params.get('phone')
    password = req.params.get('password')
    
    user = get_user(phone, password)
    if not user:
        return response(INVALID_CREDS, 404)

    user['sessionId'] = login(user)
    return response(user)
