import json
import logging
import os
import azure.functions as func

from pymongo import MongoClient


env = os.environ
if 'MONGO_CONN_STR' not in env:
    logging.error('Mongo connection string missing in environment')

MONGO_CONN_STR = env['MONGO_CONN_STR']
NOT_FOUND = {'message':'Invalid email or password.'}

mongo = MongoClient(MONGO_CONN_STR)                       # Server
db = mongo.easycredit                                     # Database
users, sessions = db.users, db.sessions                   # Collections
 

def response(body, status_code=200):
    return func.HttpResponse(json.dumps(body), status_code=status_code)

def get_user(email, password):
    logging.info(f'Login request {email}:{password}')
    
    from_db = users.find_one({'email': email, 'password': password})
    if not from_db:
        return None
    return {
        'id': str(from_db['_id']), 
        'email': from_db['email']
        }

def login(user):
    print(f"Logging in user: {user['id']}")
    active_session = sessions.find_one({'userId': user['id']})
    if active_session:
        print(f"Already logged in: {user['id']}")
        return str(active_session['_id'])
    else:
        session = sessions.insert_one({'userId': user['id']})
        print(f"Logged in: {user['id']}")
        return session.inserted_id

def main(req: func.HttpRequest) -> func.HttpResponse:
    email = req.params.get('email')
    password = req.params.get('password')
    
    user = get_user(email, password)
    if not user:
        return response(NOT_FOUND, 404)

    user['session_id'] = login(user)
    return response(user)
