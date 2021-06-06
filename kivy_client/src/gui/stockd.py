from kivy.clock import Clock
from kivy.config import Config
Config.set('input', 'mouse', 'mouse,multitouch_on_demand')

from api.api_provider import StockdAPIObj
from kivymd.app import MDApp
from kivymd.uix.picker import MDDatePicker
from datetime import date
from kivymd_extensions.sweetalert import SweetAlert

# import SweetAlert kivymd_extensions.sweetalert.SweetAlert

def event_observable():
    print("Observable called")

class StockdApp(MDApp):
    from_date = date.today()
    to_date = date.today()

    def set_api_obj(self, obj):
        self.api_obj = obj
    
    def on_save_from(self, instance, value, date_range):
        if value:
            self.from_date = value
            self.root.ids.from_date.text = value.strftime("%d-%b-%Y")
    
    def on_save_to(self, instance, value, date_range):
        if value:
            self.to_date = value
            self.root.ids.to_date.text = value.strftime("%d-%b-%Y")
    
    def do_nothing(self, instance, value):
        pass

    def show_datepicker_dialog(self, should_trigger, type):
        if should_trigger:
            date_dialog = MDDatePicker()
            if type == "from":
                date_dialog.bind(on_save=self.on_save_from, on_cancel=self.do_nothing)
            elif type == "to":
                date_dialog.bind(on_save=self.on_save_to, on_cancel=self.do_nothing)
            date_dialog.open()
    
    def hit_download(self):
        self.root.ids.btn_stop.disabled = False
        self.root.ids.btn_download.disabled = True
        SweetAlert().fire(
            'Hit Download!',
            type='success',
        )

    def hit_stop(self):
        self.root.ids.btn_stop.disabled = True
        self.root.ids.btn_download.disabled = False
        SweetAlert().fire(
            'Stop Request Sent!',
            type='info',
        )
    
    def check_updates(self, *args):
        m = self.api_obj.check_for_updates()
        if m:
            SweetAlert().fire(text=m, type="info")


if __name__ == '__main__':
    load_path = "E:\GIT\StockD\StockD\kivy_client\src"
    x = StockdApp()
    x.set_api_obj(StockdAPIObj(load_path, event_observable))
    Clock.schedule_once(x.check_updates)
    x.run()
