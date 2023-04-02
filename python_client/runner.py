import argparse
import logging
import webview
import threading
import sys
import random
import json
import socket
import platform
from datetime import datetime
from app import app as server
from werkzeug.serving import make_server
from cliclient import CliClient

logger = logging.getLogger(__name__)
parser = argparse.ArgumentParser()

if sys.platform.lower().startswith("win"):
    import ctypes

    def hideConsole():
        """
        Hides the console window in GUI mode. Necessary for frozen application, because
        this application support both, command line processing AND GUI mode and theirfor
        cannot be run via pythonw.exe.
        """
        whnd = ctypes.windll.kernel32.GetConsoleWindow()
        if whnd != 0:
            ctypes.windll.user32.ShowWindow(whnd, 0)

    def showConsole():
        """Unhides console window"""
        whnd = ctypes.windll.kernel32.GetConsoleWindow()
        if whnd != 0:
            ctypes.windll.user32.ShowWindow(whnd, 1)

class UnbufferedWriter:
    def __init__(self, stream):
        self.stream = stream
        self.quiet = False
        self.file_stream = open("stockd_clilog.txt", "w")

    def write(self, data):
        if not self.quiet:
            self.stream.write(data)
            self.stream.flush()
        self.file_stream.write(data)
        self.file_stream.flush()

    def flush(self):
        self.stream.flush()
        self.file_stream.flush()

def valid_date(date_str):
    try:
        return datetime.strptime(date_str, "%Y-%m-%d")
    except ValueError:
        msg = "not a valid date: {0!r}. Please enter date in YYYY-MM-DD format.".format(date_str)
        raise argparse.ArgumentTypeError(msg)

def _get_random_port():
    while True:
        port = random.randint(1023, 65535)

        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            try:
                sock.bind(('localhost', port))
            except OSError:
                logger.warning('Port %s is in use' % port)
                continue
            else:
                return port

# CLI arguments
parser.add_argument("-s", "--from_date", help="Start date in YYYY-MM-DD format", type=valid_date)
parser.add_argument("-e", "--to_date", help="End date in YYYY-MM-DD format", type=valid_date)
parser.add_argument("--quiet", help="Don't print anything on console. Only prints to file and removes all color formatting.", action='store_true')
parser.add_argument("--print_config", help="Print current configuration for given section. Will print all config, if section not specified", action="store", const='', nargs='?')
parser.add_argument("--print_config_oneline", help="Use together with --print_config. Prints section without formatting", action="store_true")
parser.add_argument("--skip_news", help="Don't print latest news", action="store_true")
parser.add_argument("--override_setting", help="Override some settings temporarily. Edit and Save settings from GUI to for saving permanently. Any key that is shown via the --print_config option can be overridden by specifying the respective json hierarchy.",  nargs='*', type=json.loads)
parser.add_argument("--skip_version_info", help="Don't print current version info", action="store_true")

if __name__ == '__main__':
    sys.stdout = UnbufferedWriter(sys.stdout)
    sys.stderr = sys.stdout

    p = _get_random_port()
    srv = make_server('localhost', p, server, threaded=True)

    x = threading.Thread(target=srv.serve_forever)
    x.daemon = True
    logger.warning("Running thread on {}".format(p))

    x.start()

    if (len(sys.argv) == 1):
        print("Initializing engine. This console will be minimized once loading is complete.")

        if sys.platform.lower().startswith('win'):
            if getattr(sys, 'frozen', False):
                hideConsole()

        window = webview.create_window(
            'StockD', 'http://localhost:{}'.format(p))
        server.winreference = window
        if platform.system() == "Windows":
            webview.start(gui='cef', debug=False)
        else:
            webview.start(debug=False)

    else:

        args = parser.parse_args()
        if args.quiet:
            sys.stdout.quiet = True
        CliClient(args, p).run()

    sys.exit(0)
