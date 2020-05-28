import json
import logging
import os
import datetime
import azure.functions as func

from pymongo import MongoClient
from bson.objectid import ObjectId
from cashfree_sdk.payouts import Payouts
from cashfree_sdk import verification
from cashfree_sdk.payouts.cashgram import Cashgram
from cashfree_sdk.exceptions.exceptions import AlreadyExistError

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

def update_transaction_status(user, link_id, new_status):
    usr_txns = user['transactions']
    assert usr_txns
    for txn in usr_txns:
        if txn['linkId'] == link_id:
            txn['status'] = new_status
            break
    users.update_one({'_id': ObjectId(user['id'])}, {'$set': {'transactions': usr_txns}})

def add_reference_id(user, receipt, reference_id):
    if not reference_id:
        logging.error(f'Unexpected reference_id: {reference_id}. Not updating status.')
        return
    usr_txns = user['transactions']
    assert usr_txns
    for txn in usr_txns:
        if txn['receipt'] == receipt:
            assert 'referenceId' not in txn, 'ReferenceId should not already exist'
            txn['referenceId'] = reference_id
            break
    users.update_one({'_id': ObjectId(user['id'])}, {'$set': {'transactions': usr_txns}})

def create_cashgram(user, receipt):
    usr_txns = user['transactions']
    assert usr_txns, 'Transaction should have existed before reaching here'
    amount = [txn for txn in filter(lambda txn: txn['receipt'] == receipt, usr_txns)][0]['amount']
    assert amount > 0, 'This amount can not be negative' 
    expiry = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime('%Y/%m/%d')
    try:
        created_cashgram = Cashgram.create_cashgram(
            cashgramId = receipt,
            amount = str(amount),
            name = user['name'],
            email = user['email'],
            phone = user['phone'],
            linkExpiry = expiry,
            remarks = "EasyCredit Cashgram remark",
            notifyCustomer = 1
        )
    except AlreadyExistError as e:
        logging.error(f'Cashgram already exists with id: {receipt}. This is unexpected.')
    
    logging.info(f"Cashgram create response -> {created_cashgram.status_code}")
    created_cashgram = created_cashgram.json()
    assert created_cashgram['subCode'] == '200', 'Cashgram creation failed'
    logging.info(f"Cashgram created -> {created_cashgram['data']['cashgramLink']}")
    
    return created_cashgram['data']['referenceId']

def send_cashgram(from_user, to_user, receipt):
    ref_id = create_cashgram(to_user, receipt)
    add_reference_id(to_user, receipt, ref_id)
    add_reference_id(from_user, receipt, ref_id)
 
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
    
    send_cashgram(from_user, to_user, receipt)

    return response()
