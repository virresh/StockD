from kivy.app import App
from kivy.uix.widget import Widget
from kivy.lang import Builder
from kivy.uix.screenmanager import ScreenManager, Screen
from widgets.flat_button import SImageButton
from widgets.datepicker import DatePicker
import webbrowser

class MainWin(Widget):
    pass

class StockdApp(App):
    def build(self):
        return MainWin()

if __name__ == '__main__':
    StockdApp().run()
