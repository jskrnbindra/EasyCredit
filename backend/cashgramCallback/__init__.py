import json
import logging
import datetime
import os
import azure.functions as func

from urllib import parse
from pymongo import MongoClient
from bson.objectid import ObjectId
from cashfree_sdk.payouts import Payouts
from cashfree_sdk import verification
from twilio.rest import Client

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
if 'TWILIO_ACCOUNT_SID' not in env:
    logging.error('TWILIO_ACCOUNT_SID string missing in environment')
if 'TWILIO_AUTH_TOKEN' not in env:
    logging.error('TWILIO_AUTH_TOKEN string missing in environment')
if 'TWILIO_NUMBER' not in env:
    logging.error('TWILIO_NUMBER string missing in environment')

MONGO_CONN_STR = env['MONGO_CONN_STR']
CF_CLIENT_ID = env['CF_CLIENT_ID']
CF_CLIENT_SECRET = env['CF_CLIENT_SECRET']
CF_ACCOUNT = env['CF_ACCOUNT']
TWILIO_ACCOUNT_SID = env['TWILIO_ACCOUNT_SID']
TWILIO_AUTH_TOKEN = env['TWILIO_AUTH_TOKEN']
TWILIO_NUMBER = env['TWILIO_NUMBER']

SUCCESS={'message':'success'}
INVALID_CREDS = {'message':'Invalid phone or password.'}
NOT_FOUND = {'message':'Resource not found.'}
ALREADY_EXISTS = {'message':'User with that phone number exists already.'}
BOTH_USERS_MUST_EXIST = {'message':'Both participatingusers must exist.'}
SOMETHING_IS_WRONG = {'message':"Something's wrong."}
SIGNATURE_VALIDATION_FAILED = {'message': 'Signature validation failed.'}
ACK_SMS_BODY = 'Transaction acknowledged! %s has received Rs. %s.00 sent by you via EasyCredit.'
DUMMY_BANK_SMS_BODY = 'Dear Customer, Your a/c no. XXXXXXXX4436 is credited by Rs.%s.00 on %s. (Ref no 12345678903).'

mongo = MongoClient(MONGO_CONN_STR)                       # Server
db = mongo.easycredit                                     # Database
users, sessions = db.users, db.sessions                   # Collections

with open('cf_public_key.pem') as pemfile:
    CF_PUBLIC_KEY = pemfile.read()

Payouts.init(CF_CLIENT_ID, CF_CLIENT_SECRET, CF_ACCOUNT, public_key=CF_PUBLIC_KEY)
Twilio = Client(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)

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

def update_transaction_status(user, receipt, new_status):
    usr_txns = user['transactions']
    assert usr_txns
    for txn in usr_txns:
        if txn['receipt'] == receipt:
            txn['status'] = new_status
            break
    users.update_one({'_id': ObjectId(user['id'])}, {'$set': {'transactions': usr_txns}})

def send_ack_sms(from_user, to_user, receipt):
    usr_txns = to_user['transactions']
    amount = [txn for txn in filter(lambda txn: txn['receipt'] == receipt, usr_txns)][0]['amount']
    sms_body = ACK_SMS_BODY % (to_user['name'], amount)
    sms_msg = Twilio.messages.create(body=sms_body, from_=TWILIO_NUMBER, to=f"+91{from_user['phone']}")
    logging.info(f"SMS sent to {from_user['phone']}")
    assert not sms_msg.error_code, 'SMS sending failed!'

def send_dummy_bank_sms(from_user, to_user, receipt):
    usr_txns = to_user['transactions']
    amount = [txn for txn in filter(lambda txn: txn['receipt'] == receipt, usr_txns)][0]['amount']
    timestamp = datetime.datetime.now().strftime('%d-%b-%Y %H:%M:%S')
    sms_body = DUMMY_BANK_SMS_BODY % (amount, timestamp)
    sms_msg = Twilio.messages.create(body=sms_body, from_=TWILIO_NUMBER, to=f"+91{to_user['phone']}")
    logging.info(f"SMS sent to {to_user['phone']}")
    assert not sms_msg.error_code, 'SMS sending failed!'

def cashgram_redeemed(body):
    cashgram_id = body['cashgramId']
    reference_id = body['referenceId']
    
    logging.info(f'CashgramId -> {cashgram_id}')
    logging.info(f'referenceId -> {reference_id}')

    from_user_phone, to_user_phone, timestamp = cashgram_id.split('-')

    from_user = get_user_by(from_user_phone)
    to_user = get_user_by(to_user_phone)

    if not (from_user and to_user):
        logging.error('This is not expected. User in the receipt does not exist.')
        return response(SOMETHING_IS_WRONG, 500)

    update_transaction_status(from_user, cashgram_id, 'DONE')
    update_transaction_status(to_user, cashgram_id, 'DONE')
    
    send_dummy_bank_sms(from_user, to_user, cashgram_id)
    send_ack_sms(from_user, to_user, cashgram_id)
    return response()

def main(req: func.HttpRequest) -> func.HttpResponse:
    payload = req.get_body().decode()
    if not verification.verify_webhook(payload, payload_type='FORM'):
        return response(SIGNATURE_VALIDATION_FAILED, 401)
    
    payload = dict((k, v if len(v) > 1 else v[0]) for k, v in parse.parse_qs(payload).items())
    logging.info(f'got info -> {payload}')
    event = payload['event']

    if event == 'CASHGRAM_REDEEMED':
        return cashgram_redeemed(payload)
    logging.info(f'Not interested in this {event} event yet.')
    return response()
