from concurrent.futures import ThreadPoolExecutor
import requests
import time
import sys
import os
# import csv
from random import randrange


project_dir = os.path.dirname(__file__)
input_file_name = 'input.csv'
input_file = os.path.join(project_dir, input_file_name)


def post(request):
    url = 'http://localhost:8080/paymentd'
    headers = {
        'Content-Type': 'application/json',
        'accept': 'application/json'
    }
    sequence, body = request

    try:
        t1 = time.perf_counter()
        response = requests.post(url, headers=headers, json=body, timeout=None)

        t2 = time.perf_counter()
        print(f"Request {sequence} took {t2 - t1:0.4f} seconds")

    except Exception as e:
        print(e)
        sys.exit(1)
    finally:
        time.sleep(1)


t = time.perf_counter()

# payments = []
# with open(input_file, 'r', encoding='utf-8') as f:
#     reader = csv.DictReader(f)
#     for row in reader:
#         payment = row['paymentId'].rstrip().replace('\n','').replace('\r','').replace(' ','')
#         payments.append(row)


payment_requests = []
for i in range(100000):
    body = {'id': '{0}', 'amount': '{1}', 'customerId': '{2}',
            'transactionId': '{3}', 'status': 0}
    body['id'] = i
    body['amount'] = randrange(15)
    body['customerId'] = randrange(10)
    body['transactionId'] = randrange(20)
    payment_requests.append(body)

    if i % 32 == 0:
        print(f"MOD add item", i)
        payment_requests.append(body)


with ThreadPoolExecutor(max_workers=10) as executor:
    executor.map(post, enumerate(payment_requests))
    executor.shutdown()


print(f"Total time: {time.perf_counter() - t:0.4f} seconds")
