import json
import logging
import os
import azure.functions as func

from pymongo import MongoClient
from bson.objectid import ObjectId
from cashfree_sdk.payouts import Payouts
from cashfree_sdk import verification

# COMMON UTILS 

env = os.environ
if 'MONGO_CONN_STR' not in env:
    logging.error('Mongo connection string missing in environment')
if 'CF_CLIENT_ID' not in env:
    logging.error('CF_CLIENT_ID string missing in environment')
if 'CF_CLIENT_SECRET' not in env:
    logging.error('CF_CLIENT_SECRET string missing in environment')
if 'CF_ACCOUNT' not in env:
    logging.error('CF_ACCOUNT string missing in environment')

MONGO_CONN_STR = env['MONGO_CONN_STR']
CF_CLIENT_ID = env['CF_CLIENT_ID']
CF_CLIENT_SECRET = env['CF_CLIENT_SECRET']
CF_ACCOUNT = env['CF_ACCOUNT']

SUCCESS={'message':'success'}
INVALID_CREDS = {'message':'Invalid phone or password.'}
NOT_FOUND = {'message':'Resource not found.'}
ALREADY_EXISTS = {'message':'User with that phone number exists already.'}
BOTH_USERS_MUST_EXIST = {'message':'Both participatingusers must exist.'}
SOMETHING_IS_WRONG = {'message':"Something's wrong."}
SIGNATURE_VALIDATION_FAILED = {'message': 'Signature validation failed.'}

mongo = MongoClient(MONGO_CONN_STR)                       # Server
db = mongo.easycredit                                     # Database
users, sessions = db.users, db.sessions                   # Collections

with open('cf_public_key.pem') as pemfile:
    CF_PUBLIC_KEY = pemfile.read()

Payouts.init(CF_CLIENT_ID, CF_CLIENT_SECRET, CF_ACCOUNT, public_key=CF_PUBLIC_KEY)


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
    usr = users.find_one({'phone': phone})
    return stringify_id(usr)

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

def update_transaction_status(user, referenceId, new_status):
    usr_txns = user['transactions']
    assert usr_txns
    for txn in usr_txns:
        if txn['referenceId'] == referenceId:
            txn['status'] = new_status
            break
    users.update_one({'_id': ObjectId(user['id'])}, {'$set': {'transactions': usr_txns}})

def cashgram_redeemed(body):
    cashgramid = body['cashgramid']
    referenceId = body['referenceId']
    
    logging.info(f'CashgramId -> {cashgramid}')
    logging.info(f'referenceId -> {referenceId}')

    from_user_phone, to_user_phone, timestamp = cashgramid.split('-')

    from_user = get_user_by(from_user_phone)
    to_user = get_user_by(to_user_phone)

    if not (from_user and to_user):
        logging.error('This is not expected. User in the receipt does not exist.')
        return response(SOMETHING_IS_WRONG, 500)

    update_transaction_status(from_user, referenceId, 'DONE')
    update_transaction_status(to_user, referenceId, 'DONE')
    
    return response()

def main(req: func.HttpRequest) -> func.HttpResponse:
    if not verification.verify_webhook(req.get_body(), 'JSON'):
        return response(SIGNATURE_VALIDATION_FAILED, 401)
    
    body = req.get_json()
    logging.info(f"got info -> {body}")
    event = body['event']

    if event == 'CASHGRAM_REDEEMED':
        return cashgram_redeemed(body)
    
    logging.info(f'Not interested in this event -> {event}')
    return response()
