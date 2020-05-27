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
BOTH_USERS_MUST_EXIST = {'message':'Both participatingusers must exist.'}

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

def stringify_id(obj):
    obj_id = str(obj['_id'])
    obj['id'] = obj_id
    del obj['_id']
    return obj

# THIS FUNCTION

def get_user(id):
    logging.info(f'Getting user by id: {id}')
    usr = users.find_one({'_id': ObjectId(id)})
    logging.info(f'found {usr}')
    return stringify_id(usr)

def create_transaction(from_user, to_user, amount, receipt, link_id):
    logging.info(f'Starting new transaction receipt -> {receipt}')
    import time
    timestamp = int(time.time())
    
    # adding under to_user
    usr_txns = to_user['transactions'] if 'transactions' in to_user else []

    usr_id = from_user['id']
    usr_name = from_user['name']
  
    new_txn = {
        'id': f'{usr_id}{usr_name}{amount}{timestamp}'.replace(' ', '-'),
        'amount': amount, 
        'timestamp': timestamp, 
        'beneficiaryId': usr_id,
        'receipt': receipt,
        'linkId': link_id,
        'beneficiaryName': usr_name,
        'status': 'STARTED'
    }
    usr_txns.append(new_txn)
    users.update_one({'_id': ObjectId(to_user['id'])}, {'$set': {'transactions': usr_txns}})

    # adding under from_user
    usr_txns = from_user['transactions'] if 'transactions' in from_user else []

    usr_id = to_user['id']
    usr_name = to_user['name']
  
    new_txn = {
        'id': f'{usr_id}{usr_name}{amount}{timestamp}'.replace(' ', '-'),
        'amount': -amount, 
        'timestamp': timestamp, 
        'beneficiaryId': usr_id,
        'receipt': receipt,
        'linkId': link_id,
        'beneficiaryName': usr_name,
        'status': 'STARTED'
    }
    usr_txns.append(new_txn)
    users.update_one({'_id': ObjectId(from_user['id'])}, {'$set': {'transactions': usr_txns}})

    return new_txn

def main(req: func.HttpRequest) -> func.HttpResponse:
    from_user_id = req.params.get('from_user')
    to_user_id = req.params.get('to_user')
    amount = req.params.get('amount')
    receipt = req.params.get('receipt')
    link_id = req.params.get('linkId')
    
    from_user = get_user(from_user_id)
    to_user = get_user(to_user_id)

    if not (from_user and to_user):
        return response(BOTH_USERS_MUST_EXIST, 400)
    
    txn = create_transaction(from_user, to_user, int(amount), receipt, link_id)
    return response(txn)
