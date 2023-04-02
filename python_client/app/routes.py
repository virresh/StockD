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
import logging
import re
from multiprocessing.managers import BaseManager

SECURE_FLAG = True
TIMEOUT_DURATION = 10
STOP_FLAG = False
eventQ = queue.Queue(maxsize=100)
logging.basicConfig(filename='stockd_debuglog.txt',
                    filemode='w',
                    format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                    datefmt='%H:%M:%S',
                    level=logging.DEBUG)
nse_client_hints = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36', 
    'sec-ch-ua': '"Not?A_Brand";v="8", "Chromium";v="108", "Google Chrome";v="108"',
    'sec-ch-ua-mobile': '?0',
    'sec-ch-ua-platform': "Windows",
    'dnt': '1',
    'upgrade-insecure-requests': '1',
    'accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9',
    'sec-fetch-site': 'same-site',
    'sec-fetch-mode': 'navigate',
    'sec-fetch-user': '?1',
    'sec-fetch-dest': 'document',
    'referer': 'https://www.nseindia.com/',
    'accept-encoding': 'gzip, deflate, br',
    'accept-language': 'en-US,en;q=0.9,hi;q=0.8'
}
session = requests.Session()
session.get('https://www.nseindia.com/', headers = nse_client_hints)
logging.info("Running StockD")
logging.info("StockD Secure Flag == " + str(SECURE_FLAG))
logger = logging.getLogger('StockD')

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

def get_date(dString):
    # Check if incoming date has any alphabet
    # So far, only two date formats were found in given csv
    # so this might just work
    type1 = r"%d-%b-%Y"
    type2 = r"%d-%m-%Y"
    if re.search('[a-zA-Z]', dString):
        return datetime.datetime.strptime(dString, type1)
    else:
        return datetime.datetime.strptime(dString, type2)


def getQ():
    # if not hasattr(g, 'eventQ'):
    #     # manager = BaseManager(('', 37844), b'password')
    #     # manager.register('get_Q')
    #     # manager.connect()
    #     g.eventQ = queue.Queue(maxsize=100)
    return eventQ
    
def getLogger():
    return logger

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
        # print(item)
        getLogger().info(str(item))
        yield "event: {}\ndata: {}\n\n".format(item['event'], item['data'])

def get_csv(weblink):
    global SECURE_FLAG
    global TIMEOUT_DURATION

    r = session.get(weblink, headers=nse_client_hints, verify=SECURE_FLAG, timeout=TIMEOUT_DURATION)
    if r.status_code != 200:
        return None

    if 'zip' not in r.headers.get('Content-Type', ''):
        csvBytes = r.content
    else:
        z = zipfile.ZipFile(io.BytesIO(r.content))
        csvBytes = z.read(z.namelist()[0])

    df = pd.read_csv(io.BytesIO(csvBytes), dtype=str)
    # strip any spaces if required
    df.columns = df.columns.str.strip()
    for col in df.columns:
        df[col] = df[col].str.strip()
    return df

def process_eq(weblink, saveloc, d, get_delivery=None):
    df = get_csv(weblink)
    df = df.replace('-', '0')
    df = df[df['SERIES'].isin(['EQ', 'BE'])]
    dataDate = None

    if 'DATE1' in df.columns:
        dataDate = get_date(df['DATE1'].iloc[0])
    elif 'TIMESTAMP' in df.columns:
        dataDate = get_date(df['TIMESTAMP'].iloc[0])

    if not (parse(dataDate, '{0:%Y}{0:%m}{0:%d}') == parse(d, '{0:%Y}{0:%m}{0:%d}')):
        getLogger().error("Date Integrity check failed. Found date {} but expected {}. Skipping.".format(dataDate, d))
        getQ().put({'event': 'log', 'data': parse(d, 'Equity Bhavcopy Date Mismatch. Skipping Equity Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
        raise Exception("Date Mismatch Error")

    cname_map = {
        'TOTTRDQTY': 'VOLUME',
        'TTL_TRD_QNTY': 'VOLUME',
        'OPEN_PRICE': 'OPEN',
        'HIGH_PRICE': 'HIGH',
        'LOW_PRICE': 'LOW',
        'CLOSE_PRICE': 'CLOSE',
        'DELIV_QTY': 'DELIVERY'
    }
    df = df.rename(columns=cname_map)
    df['DATE'] = [parse(d, '{0:%Y}{0:%m}{0:%d}')] * len(df)
    df['OI'] = ['0'] * len(df)
    if get_delivery is not None:
        try:
            df['OI'] = df['DELIVERY']
        except Exception as ex:
            getQ().put({'event': 'log', 'data': 'Delivery data unavailable on selected server.'})
            getLogger().info(str(ex))
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
            logger.error("Cannot find a symbol for {}. Enable keep others to keep the symbol.".format(item))
            return None
    # df['SYMBOL'] = df['SYMBOL'].apply(lambda x: x.replace(' ', '_'))
    df['SYMBOL'] = df['SYMBOL'].apply(_rename)
    df = df.dropna()
    df.to_csv(saveloc, header=None, index=None)
    return df

def process_day(configs, date):
    getLogger().info('Processing date ' + str(date))
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
                getLogger().info('Trying Equity Bhavcopy from ' + eqlink)
                eqlocation = os.path.join(configs['SETTINGS']['eqDir']['value'], parse(date, 'EQ_{0:%Y}{0:%^b}{0:%d}.txt'))
                getDelivery = None
                if not os.path.exists(configs['SETTINGS']['eqDir']['value']):
                    os.makedirs(configs['SETTINGS']['eqDir']['value'], exist_ok=True)
                if configs['SETTINGS']['eqDeliveryDataCheck']['value'] == 'true':
                    getDelivery = True
                eqdf = process_eq(eqlink, eqlocation, date, get_delivery=getDelivery)
                getLogger().info('EQ Bhavcopy success')
                getQ().put({'event': 'log', 'data': parse(date, 'Convert Equity Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
            except Exception as e:
                getLogger().info('EQ Bhavcopy failed')
                getLogger().info(str(e), exc_info=e)
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find EQ Bhavcopy on selected Server for {0:%Y}-{0:%b}-{0:%d}')})

        if configs['SETTINGS']['fuCheck']['value'] == 'true':
            try:
                fulink = parse(date, configs['LINKS']['fuBhav']['link'])
                getLogger().info('Trying Futures Bhavcopy from ' + fulink)
                fulocation = os.path.join(configs['SETTINGS']['fuDir']['value'], parse(date, 'FU_{0:%Y}{0:%^b}{0:%d}.txt'))
                if not os.path.exists(configs['SETTINGS']['fuDir']['value']):
                    os.makedirs(configs['SETTINGS']['fuDir']['value'], exist_ok=True)
                fudf = process_fu(fulink, fulocation, date)
                getQ().put({'event': 'log', 'data': parse(date, 'Convert Futures Bhavcopy for {0:%Y}-{0:%b}-{0:%d}')})
            except Exception as e:
                getLogger().info('FU Bhavcopy failed')
                getLogger().info(str(e))
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find FU Bhavcopy on selected Server for {0:%Y}-{0:%b}-{0:%d}')})

        if configs['SETTINGS']['inCheck']['value'] == 'true':
            try:
                inlink = parse(date, configs['LINKS']['indall']['link'])
                getLogger().info('Trying Index Bhavcopy from ' + inlink)
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
            except Exception as e:
                getLogger().info('IN Bhavcopy failed')
                getLogger().info(str(e))
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot Find IN Bhavcopy on selected Server for {0:%Y}-{0:%b}-{0:%d}')})

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
                getLogger().info('ALL Bhavcopy failed')
                getLogger().info(str(err))
                getQ().put({'event': 'log', 'data': parse(date, 'Cannot consolidate for {0:%Y}-{0:%b}-{0:%d}')})
    getQ().put({'event': 'log', 'data': parse(date, 'Done with {0:%Y}-{0:%b}-{0:%d}')})
    if eqdf is None and fudf is None and indf is None:
        return 0
    else:
        return 1

def loadConfigFromDisk():
    global SECURE_FLAG
    global TIMEOUT_DURATION
    getLogger().info('Attempting configuration load')
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

    if 'insecureMode' in main_config['SETTINGS']:
        if main_config['SETTINGS']['insecureMode']['value'] == 'true':
            SECURE_FLAG = False
        else:
            SECURE_FLAG = True
    
    if 'rTimeout' in main_config['SETTINGS']:
        TIMEOUT_DURATION = int(main_config['SETTINGS']['rTimeout']['value'])

    main_config['INDICES'] = index_state_map
    getLogger().info('Configuration loading success')
    return main_config

def saveConfigToDisk(main_config):
    getLogger().info('Attempting Configuration save')
    with open('./generate_config.json', 'w') as f:
        json.dump(main_config, f)
        getLogger().info('Configuration save success')

def process_aux_config(form_dict, main_config):
    if 'auxConfig' in form_dict:
        aux_config = json.loads(request.form['auxConfig'])
        getLogger().info('Overriding saved config with ' + request.form['auxConfig'])
        getQ().put({'event': 'log', 'data': 'Downloading with temporarily overriden config.'})
        main_config = update(main_config, aux_config)
    return main_config

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

@app.route('/stop', methods=['GET'])
def send_stop():
    global STOP_FLAG
    STOP_FLAG = True

@app.route('/download', methods=['POST'])
def process_range():
    global TIMEOUT_DURATION
    global STOP_FLAG
    done_days = 0
    total_days = 0
    message = ""
    try:
        start = datetime.datetime.strptime(request.form['fromDate'], '%Y-%m-%d')
        end = datetime.datetime.strptime(request.form['toDate'], '%Y-%m-%d')
        print(start, end)
        getLogger().info('Processing ' + str(start) + ' till ' + str(end))
        getLogger().info('Using a timout of ' + str(TIMEOUT_DURATION) + " s")
        if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
            getQ().put({'event': 'progress', 'data': '-1'})
            return

        main_config = loadConfigFromDisk()
        main_config = process_aux_config(request.form, main_config)

        getQ().put({'event': 'log', 'data': '##### Using link Profile {} #####'.format(main_config['BASELINK']['stock_TYPE'])})
        getQ().put({'event': 'log', 'data': '======= Starting Downlad ======='})
        getQ().put({'event': 'progress', 'data': '0'})
        delta = datetime.timedelta(1)
        total_range = end - start + delta
        cur_day = start
        for day in range(0, total_range.days):
            if STOP_FLAG == True:
                STOP_FLAG = False
                message = " Stopped via Stop Button."
                break
            done_days += process_day(main_config, cur_day)
            cur_day = cur_day + delta
            getQ().put({'event': 'progress', 'data': str(int(((day+1) / total_range.days) * 100))})
            total_days += 1
    except Exception as ex:
        print(ex)
        message = " Encountered an Error."
        getQ().put({'event': 'progress', 'data': '-1'})

    STOP_FLAG = False
    getQ().put({'event': 'log', 'data': '======= Downlad End ======='})
    return "Downloaded {}/{} days.{}".format(done_days, total_days, message)

@app.route('/')
@app.route('/index')
def index():
    getLogger().info("Loaded Main Page.")
    return render_template('index.html')

@app.route('/version')
def version():
    return "4.8"

@app.route('/test', methods=['POST'])
def test():
    d = request.form
    print(d)
    return json.dumps(d)

@app.route('/addToQueue/<datapackage>', methods=['GET'])
def qadder(datapackage):
    getQ().put({'event': 'message', 'data': datapackage})
    return "Currently " + str(getQ().qsize()) + " events."

@app.route('/getConfig', methods=['GET', 'POST'])
def getConfig():
    if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
        abort(404)

    main_config = loadConfigFromDisk()
    main_config = process_aux_config(request.form, main_config)

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
    getLogger().info("recieved config -- " + str(d))

    return 'Setting Update Succesful'


"""
Stream is supposed provide three events:
- message
- log
- progress
"""
@app.route('/stream')
def getstream():
    global SECURE_FLAG
    global TIMEOUT_DURATION
    m = ""
    main_config = None
    if not os.path.exists(os.path.join(app.static_folder, 'default_config.json')):
        m = "Configuration files missing!"
        getLogger().info('Could not find configuration files!')
    else:
        with open(os.path.join(app.static_folder, 'default_config.json'), 'r') as f:
            main_config = json.load(f)
        getLogger().info('Main Configuration loaded')
        if os.path.exists('./generate_config.json'):
            with open('./generate_config.json', 'r') as f:
                aux_config = json.load(f)
                main_config.update(aux_config)
                getLogger().info('Auxiliary configuration loaded!')
        else:
            getLogger().info('No Auxiliary configuration!')

    if main_config is not None:
        vlink = main_config["LINKS"]["version"]['link']
        r = session.get(vlink, verify=SECURE_FLAG, timeout=TIMEOUT_DURATION)
        if r.status_code != 200:
            m = "Cannot connect to internet! StockD requires internet to function! If you are sure you have internet connectivity, then report this and proceed with download."
            getLogger().info('Status recieved: ' + r.status_code)
            getLogger().info('Response Message: ' + r.message)
        else:
            latest_v = float(r.content.decode('UTF-8-sig'))
            cur_v = float(version())
            getLogger().info('Internet Test success!')
            if cur_v < latest_v:
                m = "An update is available! Please update to latest version for best performance."
            else:
                m = ""
    getQ().put({'event': 'message', 'data': m})
    return Response(attachToStream(),
                    mimetype='text/event-stream')

@app.route('/news')
def getnews():
    global SECURE_FLAG
    global TIMEOUT_DURATION
    r = session.get("https://docs.google.com/document/export?format=txt&id=1-SIzNgaFaCC-Ohmdg55-ksL2aIaM0k8O1QBzTOD3zvA&includes_info_params=true&inspectorResult=%7B%22pc%22%3A1%2C%22lplc%22%3A1%7D", verify=SECURE_FLAG, timeout=TIMEOUT_DURATION)
    if r.status_code != 200:
        getLogger().info('News load failed!')
        getLogger().info('Server says ' + r.status_code)
        getLogger().info('Content: ' + r.message)
        abort(404)
    else:
        getLogger().info('News Load success')
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
