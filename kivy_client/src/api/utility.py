import collections
import datetime
import re

def update(d, u):
    for k, v in u.items():
        if isinstance(v, collections.abc.Mapping):
            d[k] = update(d.get(k, {}), v)
        else:
            d[k] = v
    return d

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



