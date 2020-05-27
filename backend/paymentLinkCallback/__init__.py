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
SOMETHING_IS_WRONG = {'message':"Something's wrong."}

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

def update_transaction_status(user, link_id, new_status):
    usr_txns = user['transactions']
    assert usr_txns
    for txn in usr_txns:
        if txn['linkId'] == link_id:
            txn['status'] = new_status
            break
    users.update_one({'_id': ObjectId(user['id'])}, {'$set': {'transactions': usr_txns}})
    
def main(req: func.HttpRequest) -> func.HttpResponse:
    link_id = req.params.get('razorpay_invoice_id')
    receipt = req.params.get('razorpay_invoice_receipt')
    status = req.params.get('razorpay_invoice_status')

    logging.info(f'Payment status -> {status}')
    logging.info(f'razorpay_invoice_id -> {link_id}')
    logging.info(f'razorpay_invoice_receipt -> {receipt}')

    from_user_phone, to_user_phone, timestamp = receipt.split('-')

    from_user = get_user_by(from_user_phone)
    to_user = get_user_by(to_user_phone)

    if not (from_user and to_user):
        logging.error('This is not expected. User in the receipt does not exist.')
        return response(SOMETHING_IS_WRONG, 500)

    update_transaction_status(from_user, link_id, 'MIDWAY')
    update_transaction_status(to_user, link_id, 'MIDWAY')
    
    return response()
