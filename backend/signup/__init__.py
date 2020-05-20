import json
import logging
import os
import azure.functions as func

from pymongo import MongoClient

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

def signup(name, email, phone, password):
    logging.info(f'Sign up: {name} {phone}')
    return users.insert_one({
        'name': name, 
        'email': email,
        'phone': phone,
        'password': password
        }).inserted_id
    
def main(req: func.HttpRequest) -> func.HttpResponse:
    name = req.params.get('name')
    email = req.params.get('email')
    phone = req.params.get('phone')
    password = req.params.get('password')
    
    user_exists = get_user_by(phone)
    if user_exists:
        return response(ALREADY_EXISTS, 309)

    logging.info(f'New user created: {signup(name, email, phone, password)}')
    return response()
