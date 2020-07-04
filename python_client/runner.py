import logging
import webview

from contextlib import redirect_stdout
from io import StringIO
from app import app as server

logger = logging.getLogger(__name__)


if __name__ == '__main__':

    stream = StringIO()
    with redirect_stdout(stream):
        window = webview.create_window(
            'StockD', server)
        webview.start(debug=True)
