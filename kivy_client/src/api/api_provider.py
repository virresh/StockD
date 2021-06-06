import requests
import logging
import os
import io
import json
import api.utility as utility
import zipfile
import pandas as pd

class StockdAPIObj(object):
    SECURE_FLAG = True
    TIMEOUT_DURATION = 10
    STOP_FLAG = False

    def __init__(self, load_path, text_change_observable):
        self.load_path = load_path
        logging.basicConfig(
            filename='stockd_debuglog.txt',
            filemode='w',
            format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
            datefmt='%H:%M:%S',
            level=logging.DEBUG)
        self.logger = logging.getLogger('StockD')
        self.main_config = {}
        self.load_config_from_disk()
        self.logger.info("StockD secure Flag == " + str(self.SECURE_FLAG))
        self.eventQ = text_change_observable
        self.default_headers = {'user-agent': 'Python Client'}
        self.session = requests.Session()
        self.session.get('https://www.nseindia.com/', headers = self.default_headers)
    
    def version(self):
        return "5.0"

    def load_config_from_disk(self):
        self.logger.info('Attempting configuration load')
        with open(os.path.join(self.load_path, 'default_config.json'),
                'r') as f:
            self.main_config = json.load(f)

        if os.path.exists('./generate_config.json'):
            with open('./generate_config.json', 'r') as f:
                aux_config = json.load(f)
                self.main_config = utility.update(self.main_config, aux_config)

        states = self.main_config['INDICES']
        defaultstate = self.main_config['SETTINGS']['inKeepOthersCheck']['value']
        index_state_map = {}
        for k, v in self.main_config['index_map'].items():
            index_state_map[v] = {"type": "checkbox", "value": defaultstate}
            if v in states:
                index_state_map[v] = utility.update(index_state_map[v], states[v])

        if 'insecureMode' in self.main_config['SETTINGS']:
            if self.main_config['SETTINGS']['insecureMode']['value'] == 'true':
                self.SECURE_FLAG = False
            else:
                self.SECURE_FLAG = True
        
        if 'rTimeout' in self.main_config['SETTINGS']:
            self.TIMEOUT_DURATION = int(self.main_config['SETTINGS']['rTimeout']['value'])

        self.main_config['INDICES'] = index_state_map
        self.logger.info('Configuration loading success')
        return self.main_config

    def save_config_to_disk(self, main_config):
        self.logger.info('Attempting Configuration save')
        with open('./generate_config.json', 'w') as f:
            json.dump(main_config, f)
            self.logger.info('Configuration save success')

    def get_csv(self, weblink):
        headers = {
            'user-agent': 'Python Client'
        }
        r = self.session.get(weblink, headers=headers, verify=self.SECURE_FLAG, timeout=self.TIMEOUT_DURATION)
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
    
    def send_stop(self):
        self.STOP_FLAG = True
    
    def check_for_updates(self):
        vlink = self.main_config["LINKS"]["version"]['link']
        try:
            r = self.session.get(vlink, verify=self.SECURE_FLAG, timeout=self.TIMEOUT_DURATION)
        except Exception as ex:
            self.logger.error("Exception while connecting to Server.", exc_info=True)
            return ""

        if r.status_code != 200:
            m = "Cannot connect to internet! StockD requires internet to function! If you are sure you have internet connectivity, then report this and proceed with download."
            self.logger.info('Status recieved: ' + r.status_code)
            self.logger.info('Response Message: ' + r.message)
        else:
            latest_v = float(r.content.decode('UTF-8-sig'))
            cur_v = float(self.version())
            self.logger.info('Internet Test success!')
            if cur_v < latest_v:
                m = "An update is available! Please update to latest version for best performance."
            else:
                m = ""
        return m

    
