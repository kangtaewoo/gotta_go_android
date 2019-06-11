#-*- coding:utf-8 -*-

import firebase_admin
import google.cloud
from firebase_admin import credentials, firestore
import json

cred = credentials.Certificate("./gottago-241113-firebase-adminsdk-zyhac-34003f3bc3.json")
app = firebase_admin.initialize_app(cred)

store = firestore.client()
doc_ref = store.collection(u'log_info')


with open('info.json', 'r', encoding='utf8') as json_file:
    json_data = json.load(json_file)
for distro  in json_data:
    if distro is None:
        print("done")
        break
    hnr_nam = distro["hnr_nam"]
    gu_nm = distro["gu_nm"]
    slaveno = distro["slaveno"]
    masterno = distro["masterno"]
    lng = distro["lng"]
    # creat_de = distro["creat_de"]
    neadres_nm = distro["neadres_nm"]
    objectid = distro["objectid"]
    mtc_at = distro["mtc_at"]
    lat = distro["lat"]

    doc_ref.add({u'hnr_nam': u''+hnr_nam+'',
                 u'gu_nm': u''+gu_nm+'',
                 u'slaveno': u''+slaveno+'',
                 u'masterno': u''+masterno+'',
                 # u'creat_de': u''+creat_de+'',
                 u'neadres_nm':u''+neadres_nm+'',
                 u'objectid': objectid,
                 u'mtc_at': u''+mtc_at+'',
                 u'lat': float(lat),
                 u'lng': float(lng)
                 })

