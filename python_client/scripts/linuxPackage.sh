#!/bin/sh
pyinstaller runner.py --add-data "app/static:static" --add-data "app/templates:templates" --onefile --name "StockD_x64_Linux" --icon "./app/static/img/favicon.ico"
