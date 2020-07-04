from app import app
from flask import render_template, abort, jsonify
from flask import request, Response
import json
import time
import os
import queue

eventQ = queue.Queue(maxsize=100)
stopDownload = False

def attachToStream():
    while True:
        item = eventQ.get()
        print(item)
        yield "event: {}\ndata: {}\n\n".format(item['event'], item['data'])

@app.route('/')
@app.route('/index')
def index():
    return render_template('index.html')

@app.route('/version')
def version():
    return "4.1"

@app.route('/test', methods=['POST'])
def test():
    d = request.form
    print(d)
    return json.dumps(d)

@app.route('/addToQueue/<datapackage>', methods=['GET'])
def qadder(datapackage):
    eventQ.put({'event': 'message', 'data': datapackage})
    return "Currently " + str(eventQ.qsize()) + " events."

@app.route('/getConfig', methods=['GET'])
def getConfig():
    if not os.path.exists('./default_config.json'):
        abort(404)

    with open('./default_config.json', 'r') as f:
        main_config = json.load(f)

    if os.path.exists('./generate_config.json'):
        with open('./generate_config.json', 'r') as f:
            aux_config = json.load(f)
            main_config.update(aux_config)

    return jsonify(main_config)

@app.route('/setConfig', methods=['POST'])
def saveConfig():
    if not os.path.exists('./default_config.json'):
        abort(404)

    with open('./default_config.json', 'r') as f:
        main_config = json.load(f)

    if os.path.exists('./generate_config.json'):
        with open('./generate_config.json', 'r') as f:
            aux_config = json.load(f)
            main_config.update(aux_config)

    if(not request.is_json):
        d = request.form
        print(d)

        for key in main_config['SETTINGS']:
            if key in d:
                main_config['SETTINGS'][key]['value'] = d[key]
    else:
        d = request.get_json()
        print(d)

        if "BASELINK" in d:
            main_config["BASELINK"].update(d["BASELINK"])

        if "LINKS" in d:
            main_config["LINKS"].update(d["LINKS"])

    with open('./generate_config.json', 'w') as f:
        json.dump(main_config, f)
    
    return 'Setting Update Succesful'

@app.route('/stream')
def getstream():
    return Response(attachToStream(),
                    mimetype='text/event-stream')
