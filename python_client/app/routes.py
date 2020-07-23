from app import app
from flask import render_template, abort, jsonify
from flask import request, Response, g, url_for
import json
import time
import os
import queue
import requests
import io
import zipfile
import pandas as pd
import datetime
import traceback
import webview
import collections.abc
from multiprocessing.managers import BaseManager

eventQ = queue.Queue(maxsize=100)

class dWrapper:
    def __init__(self, date):
        self.date = date

    def __format__(self, spec):
        caps = False
        if '^' in spec:
            caps = True
            spec = spec.replace('^', '')
        out = self.date.strftime(spec)
        if caps:
            out = out.upper()
        return out

    def __getattr__(self, key):
        return getattr(self.date, key)

def parse(d, s):
    return s.format(dWrapper(d))

def getQ():
    # if not hasattr(g, 'eventQ'):
    #     # manager = BaseManager(('', 37844), b'password')
    #     # manager.register('get_Q')
    #     # manager.connect()
    #     g.eventQ = queue.Queue(maxsize=100)
    return eventQ

def update(d, u):
    for k, v in u.items():
        if isinstance(v, collections.abc.Mapping):
            d[k] = update(d.get(k, {}), v)
        else:
            d[k] = v
    return d

def attachToStream():
    while True:
        item = getQ().get()
        print(item)
        yield "event: {}\ndata: {}\n\n".format(item['event'], item['data'])

def get_csv(weblink):
    headers = {
        'user-agent': 'Python Client'
    }
    r = requests.get(weblink, headers=headers)
    if r.status_code != 200:
        return None

    if 'zip' not in r.headers.get('Content-Type', ''):
        csvBytes = r.content
    else:
        z = zipfile.ZipFile(io.BytesIO(r.content))
        csvBytes = z.read(z.namelist()[0])

    df = pd.read_csv(io.BytesIO(csvBytes), dtype=str)
    return df

def process_eq(weblink, saveloc, d):
    df = get_csv(weblink)
    df = df[df['SERIES'].isin(['EQ', 'BE'])]
    cname_map = {
        'TOTTRDQTY': 'VOLUME'
    }
    df = df.rename(columns=cname_map)
    df['DATE'] = [parse(d, '{0:%Y}{0:%m}{0:%d}')] * len(df)
    df['OI'] = ['0'] * len(df)
    df = df[['SYMBOL', 'DATE', 'OPEN', 'HIGH', 'LOW', 'CLOSE', 'VOLUME', 'OI']]
    df.to_csv(saveloc, header=None, index=None)
    return df

def process_fu(weblink, saveloc, d, asPrefix=False):
    df = get_csv(weblink)
    df = df[df['INSTRUMENT'].isin(['FUTIDX', 'FUTSTK'])]

    last_symbol = None
    prefix = 'I'
    for i, row in df.iterrows():
        if last_symbol is None:
            last_symbol = row['SYMBOL']
        elif last_symbol == row['SYMBOL']:
            prefix += 'I'
        else:
            last_symbol = row['SYMBOL']
            prefix = 'I'
        if asPrefix:
            row['SYMBOL'] = prefix + '-' + row['SYMBOL']
        else:
            row['SYMBOL'] = row['SYMBOL'] + '-' + prefix

    cname_map = {
        'CONTRACTS': 'VOLUME',
        'OPEN_INT': 'OI'
    }
    df = df.rename(columns=cname_map)
    df['DATE'] = [parse(d, '{0:%Y}{0:%m}{0:%d}')] * len(df)
    df = df[['SYMBOL', 'DATE', 'OPEN', 'HIGH', 'LOW', 'CLOSE', 'VOLUME', 'OI']]
    df.to_csv(saveloc, header=None, index=None)
    return df

def process_in(weblink, saveloc, d, index_mapping={}, keeplist=set(), keepall='false'):
    df = get_csv(weblink)
    df = df.replace('-', '0')

    cname_map = {
        'Index Name': 'SYMBOL',
        'Open Index Value': 'OPEN',
        'High Index Value': 'HIGH',
        'Low Index Value': 'LOW',
        'Closing Index Value': 'CLOSE',
        'Volume': 'VOLUME'
    }
    df = df.rename(columns=cname_map)
    df['DATE'] = [parse(d, '{0:%Y}{0:%m}{0:%d}')] * len(df)
    df['OI'] = ['0'] * len(df)
    df = df[['SYMBOL', 'DATE', 'OPEN', 'HIGH', 'LOW', 'CLOSE', 'VOLUME', 'OI']]
    df['SYMBOL'] = df['SYMBOL'].apply(lambda x: x.upper())
    def _rename(item):
        if item in index_mapping and index_mapping[item] in keeplist:
            return index_mapping[item]
        elif item in index_mapping and keepall != 'false':
            return index_mapping[item]
        elif keepall != 'false':
            return item.replace('NIFTY', 'NSE').replace(' ', '')
        else:
            return None
    # df['SYMBOL'] = df['SYMBOL'].apply(lambda x: x.replace(' ', '_'))
    df['SYMBOL'] = df['SYMBOL'].apply(_rename)
    df = df.dropna()
    df.to_csv(saveloc, header=None, index=None)
    return df

def process_day(configs, date):
    eqdf = None
    fudf = None
    indf = None
    getQ().put({'event': 'log', 'data': parse(date, 'Processing {0:%Y}-{0:%b}-{0:%d}')})
    if configs['SETTINGS']['advSkipWeekend']['value'] == 'true' and date.weekday() >= 5:
        getQ().put({'event': 'log', 'data': parse(date, 'Skipping Weekend {0:%Y}-{0:%b}-{0:%d}')})
        return 0
    else:
        if configs['SETTINGS']['eqCheck']['value'] == 'true':
            try:
                eqlink = parse(date, configs['LINKS']['eqBhav']['link'])
                eqlocation = os.path.join(configs['SETTINGS']['eqDir']['value'], parse(date, 'EQ_{0:%Y}{0:%^b}{0:%d}.txt'))
                if not os.path.exists(configs['SETTINGS']['eqDir']['value']):
                    os.makedirs(configs['SETTINGS']['eqDir']['value'], exist_ok=True)
                eqdf = process_eq(eqlink, eqlocation, date)
                getQ().put({'event': 'log', 'data': parse(date, 'Convert Equity Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
            except:
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find EQ Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})

        if configs['SETTINGS']['fuCheck']['value'] == 'true':
            try:
                fulink = parse(date, configs['LINKS']['fuBhav']['link'])
                fulocation = os.path.join(configs['SETTINGS']['fuDir']['value'], parse(date, 'FU_{0:%Y}{0:%^b}{0:%d}.txt'))
                if not os.path.exists(configs['SETTINGS']['fuDir']['value']):
                    os.makedirs(configs['SETTINGS']['fuDir']['value'], exist_ok=True)
                fudf = process_fu(fulink, fulocation, date)
                getQ().put({'event': 'log', 'data': parse(date, 'Convert Futures Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
            except:
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find FU Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})

        if configs['SETTINGS']['inCheck']['value'] == 'true':
            try:
                inlink = parse(date, configs['LINKS']['indall']['link'])
                inlocation = os.path.join(configs['SETTINGS']['inDir']['value'], parse(date, 'IN_{0:%Y}{0:%^b}{0:%d}.txt'))
                if not os.path.exists(configs['SETTINGS']['inDir']['value']):
                    os.makedirs(configs['SETTINGS']['inDir']['value'], exist_ok=True)
                
                index_mapping = configs['index_map']
                keeplist = set()

                for key, val in index_mapping.items():
                    if configs['INDICES'][val]['value'] == 'true':
                        keeplist.add(val)

                indf = process_in(inlink, inlocation, date, index_mapping, keeplist, configs['SETTINGS']['inKeepOthersCheck']['value'])
                getQ().put({'event': 'log', 'data': parse(date, 'Converted Index Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
            except:
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find IN Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})

        if configs['SETTINGS']['allCheck']['value'] == 'true' and not (eqdf is None and fudf is None and indf is None):
            try:
                if configs['SETTINGS']['allIncludeFUCheck']['value'] == 'false':
                    fudf = None
                alllocation = os.path.join(configs['SETTINGS']['allDir']['value'], parse(date, 'ALL_{0:%Y}{0:%^b}{0:%d}.txt'))
                alldf = pd.concat([eqdf, fudf, indf])
                if not os.path.exists(configs['SETTINGS']['allDir']['value']):
                    os.makedirs(configs['SETTINGS']['allDir']['value'], exist_ok=True)
                alldf.to_csv(alllocation, header=False, index=False)
                getQ().put({'event': 'log', 'data': parse(date, 'Consolidated for {0:%Y}-{0:%b}-{0:%d}')})
            except Exception as err:
                traceback.print_exception(type(err), err, err.__traceback__)
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot consolidate for {0:%Y}-{0:%b}-{0:%d}')})
    getQ().put({'event': 'log', 'data': parse(date, 'Done with {0:%Y}-{0:%b}-{0:%d}')})
    if eqdf is None and fudf is None and indf is None:
        return 0
    else:
        return 1

def loadConfigFromDisk():
    with open(os.path.join(app.static_folder, 'default_config.json'),
              'r') as f:
        main_config = json.load(f)

    if os.path.exists('./generate_config.json'):
        with open('./generate_config.json', 'r') as f:
            aux_config = json.load(f)
            main_config = update(main_config, aux_config)

    states = main_config['INDICES']
    defaultstate = main_config['SETTINGS']['inKeepOthersCheck']['value']
    index_state_map = {}
    for k, v in main_config['index_map'].items():
        index_state_map[v] = {"type": "checkbox", "value": defaultstate}
        if v in states:
            index_state_map[v] = update(index_state_map[v], states[v])
    
    main_config['INDICES'] = index_state_map

    return main_config

def saveConfigToDisk(main_config):
    with open('./generate_config.json', 'w') as f:
        json.dump(main_config, f)

@app.route('/choose', methods=['POST'])
def choose_path():
    dirs = app.winreference.create_file_dialog(webview.FOLDER_DIALOG)
    if dirs and len(dirs) > 0:
        directory = dirs[0]
        if isinstance(directory, bytes):
            directory = directory.decode('UTF-8')
        response = {'status': 'ok', 'directory': directory}
    else:
        response = {'status': 'cancel'}
    return jsonify(response)

@app.route('/download', methods=['POST'])
def process_range():
    done_days = 0
    total_days = 0
    try:
        start = datetime.datetime.strptime(request.form['fromDate'], '%Y-%m-%d')
        end = datetime.datetime.strptime(request.form['toDate'], '%Y-%m-%d')
        print(start, end)
        if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
            getQ().put({'event': 'progress', 'data': '-1'})
            return

        main_config = loadConfigFromDisk()

        getQ().put({'event': 'progress', 'data': '0'})
        delta = datetime.timedelta(1)
        total_range = end - start + delta
        cur_day = start
        for day in range(0, total_range.days):
            done_days += process_day(main_config, cur_day)
            cur_day = cur_day + delta
            getQ().put({'event': 'progress', 'data': str(int(((day+1) / total_range.days) * 100))})
            total_days += 1
    except Exception as ex:
        print(ex)
        getQ().put({'event': 'progress', 'data': '-1'})

    return "Downloaded {}/{} days".format(done_days, total_days)

@app.route('/')
@app.route('/index')
def index():
    return render_template('index.html')

@app.route('/version')
def version():
    return "4.3"

@app.route('/test', methods=['POST'])
def test():
    d = request.form
    print(d)
    return json.dumps(d)

@app.route('/addToQueue/<datapackage>', methods=['GET'])
def qadder(datapackage):
    getQ().put({'event': 'message', 'data': datapackage})
    return "Currently " + str(getQ().qsize()) + " events."

@app.route('/getConfig', methods=['GET'])
def getConfig():
    if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
        abort(404)

    main_config = loadConfigFromDisk()

    return jsonify(main_config)

@app.route('/setConfig', methods=['POST'])
def saveConfig():
    if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
        abort(404)

    main_config = loadConfigFromDisk()

    if(not request.is_json):
        d = request.form
        print(d)

        for key in main_config['SETTINGS']:
            if key in d:
                main_config['SETTINGS'][key]['value'] = d[key]
        
        for key in main_config['INDICES']:
            if key in d:
                print(key, d[key])
                main_config['INDICES'][key]['value'] = d[key]
    else:
        d = request.get_json()
        print(d)

        if "BASELINK" in d:
            main_config["BASELINK"] = update(main_config["BASELINK"], d["BASELINK"])

        if "LINKS" in d:
            main_config["LINKS"] = update(main_config["LINKS"], d["LINKS"])

        if "index_map" in d:
            main_config["index_map"] = update(main_config["index_map"], d["index_map"])

    saveConfigToDisk(main_config)

    return 'Setting Update Succesful'


"""
Stream is supposed provide three events:
- message
- log
- progress
"""
@app.route('/stream')
def getstream():
    m = "";
    main_config = None
    if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
        m = "Configuration files missing!"
    else:
        with open(os.path.join(app.static_folder, 'default_config.json'), 'r') as f:
            main_config = json.load(f)

        if os.path.exists('./generate_config.json'):
            with open('./generate_config.json', 'r') as f:
                aux_config = json.load(f)
                main_config.update(aux_config)

    if main_config is not None:
        vlink = main_config["LINKS"]["version"]['link']
        r = requests.get(vlink)
        if r.status_code != 200:
            m = "Cannot connect to internet! StockD requires internet to function! If you are sure you have internet connectivity, then report this and proceed with download."
        else:
            latest_v = float(r.content.decode('UTF-8-sig'))
            cur_v = float(version())
            if cur_v < latest_v:
                m = "An update is available! Please update to latest version for best performance."
            else:
                m = ""
    getQ().put({'event': 'message', 'data': m})
    return Response(attachToStream(),
                    mimetype='text/event-stream')

@app.route('/news')
def getnews():
    r = requests.get("https://docs.google.com/document/export?format=txt&id=1-SIzNgaFaCC-Ohmdg55-ksL2aIaM0k8O1QBzTOD3zvA&includes_info_params=true&inspectorResult=%7B%22pc%22%3A1%2C%22lplc%22%3A1%7D")
    if r.status_code != 200:
        abort(404)
    else:
        return r.content.decode('UTF-8-sig')

@app.route('/getIndexNames')
def getInNmes():
    main_config = loadConfigFromDisk()
    states = main_config['INDICES']
    defaultstate = main_config['SETTINGS']['inKeepOthersCheck']['value']
    index_state_map = {}
    for k, v in main_config['index_map'].items():
        index_state_map[v] = {"type": "checkbox", "value": defaultstate}
        if v in states:
            index_state_map[v].update(states[v])

    return jsonify(index_state_map)
