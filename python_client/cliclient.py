import json
import collections
import requests
from sseclient import SSEClient
from threading import Thread
from bs4 import BeautifulSoup
from blessed import Terminal
import urllib.parse

def threaded_sselistener(domain, terminal):
    sse = SSEClient(domain + "/stream")
    for event in sse:
        if event.event == 'message' and event.data and len(event.data) > 0:
            if event.data == 'stop':
                return
            print(terminal.magenta + event.data + terminal.normal)
        elif event.event == 'progress':
            if event.data == '-1':
                print(terminal.magenta + "Couldn't download data!" + terminal.normal)
            else:
                print("{}Progress: {}{}%{}".format(terminal.orange, terminal.green, event.data, terminal.normal))
        elif event.event == 'log':
            print(event.data)

def fetch_hierarchy(tree: dict, searchkey, final_val: dict, outkey):
    if not isinstance(tree, collections.abc.Mapping):
        return False

    if searchkey in tree.keys():
        final_val[outkey] = {searchkey: tree[searchkey]}
        return True

    for key in tree.keys():
        if fetch_hierarchy(tree[key], searchkey, final_val, outkey):
            final_val[outkey] = {key: final_val[outkey]}
            return True

    return False

def merge_overrides(overrides: list):
    output = {}
    for dictionary in overrides:
        for entry in dictionary:
            output[entry] = dictionary[entry]
    return output

class CliClient:
    def __init__(self, arguments, port):
        self.args = arguments
        self.port = port
        self.domain = 'http://localhost:' + str(port)
        self.rsession = requests.Session()
        if self.args.quiet:
            self.terminal = Terminal(force_styling=None)
        else:
            self.terminal = Terminal()
        self.overrides = None

    def print_news(self):
        resp = self.rsession.get(self.domain + "/news")
        soup = BeautifulSoup(resp.content, 'html.parser')
        all_links = soup.find_all('a')
        for link in all_links:
            link.extract()
        print(self.terminal.green2 + soup.get_text().strip() + self.terminal.normal)
        for link in all_links:
            text = link.get_text().strip()
            if len(text) == 0 and link.find('img', alt=True) is not None:
                text = link.find('img', alt=True)['alt']
            print(self.terminal.link(link.get('href'), text, text))
        print()

    def print_version(self):
        resp = self.rsession.get(self.domain + "/version")
        print(self.terminal.cyan + "Your StockD Version --> " + self.terminal.magenta + resp.text + self.terminal.normal)

    def print_config(self, config, overrides):
        if not overrides:
            resp = self.rsession.get(self.domain + "/getConfig")
        else:
            resp = self.rsession.post(self.domain + "/getConfig", data={"auxConfig": json.dumps(overrides)})
        if config:
            fval = {}
            outkey = "output"
            if fetch_hierarchy(resp.json(), config, fval, outkey):
                if self.args.print_config_oneline:
                    print(json.dumps(fval[outkey]).replace("\"", "\\\""))
                else:
                    print(json.dumps(fval[outkey], indent=3))
            else:
                print(self.terminal.red + "No entry for '{}' found in main config. Note that the keys are case sensitive.".format(config) + self.terminal.normal)
        else:
            print(json.dumps(resp.json(), indent=3))

    def download(self):
        download_payload = {
            "fromDate": self.args.from_date.strftime("%Y-%m-%d"),
            "toDate": self.args.to_date.strftime("%Y-%m-%d")
        }
        if self.overrides:
            download_payload["auxConfig"] = json.dumps(self.overrides)
        resp = self.rsession.post(self.domain + "/download", data=download_payload)
        return self.terminal.cyan + resp.text + self.terminal.normal

    def add_to_q(self, message):
        self.rsession.get(self.domain + "/addToQueue/{}".format(urllib.parse.quote(message)))

    def run(self):
        final_output = "Processing Complete."
        self.SSEListen()
        if not self.args.skip_version_info:
            self.print_version()
        if not self.args.skip_news:
            self.print_news()
        if self.args.override_setting:
            self.overrides = merge_overrides(self.args.override_setting)
        if self.args.print_config is not None:
            self.print_config(self.args.print_config, self.overrides)
        if self.args.from_date or self.args.to_date:
            if not self.args.from_date:
                print(self.terminal.red + "From date is required for downloading." + self.terminal.normal)
                return
            if not self.args.to_date:
                print(self.terminal.red + "To date is required for downloading." + self.terminal.normal)
                return
            final_output = self.download()
        self.add_to_q("stop")
        self.thread.join()
        print(final_output)

    def SSEListen(self):
        self.thread = Thread(target = threaded_sselistener, args=(self.domain, self.terminal,))
        self.thread.daemon = True
        self.thread.start()
