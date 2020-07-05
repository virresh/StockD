#!/bin/sh
pyinstaller runner.py --add-data "app/static:static" --add-data "app/templates:templates" --onefile --name "StockD" --icon "./app/static/img/favicon.ico"
