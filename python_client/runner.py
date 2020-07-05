import logging
import webview
import multiprocessing
import sys
import random
import socket
from contextlib import redirect_stdout
from io import StringIO
from app import app as server
from werkzeug.serving import make_server

logger = logging.getLogger(__name__)

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


if __name__ == '__main__':

    stream = StringIO()
    with redirect_stdout(stream):
        p = _get_random_port()
        srv = make_server('localhost', p, server, threaded=True)
        x = multiprocessing.Process(target=srv.serve_forever)
        logger.warning("Running thread on {}".format(p))
        x.start()
        window = webview.create_window(
            'StockD', 'http://localhost:{}'.format(p))
        webview.start(debug=True)
        x.terminate()
        sys.exit(0)
