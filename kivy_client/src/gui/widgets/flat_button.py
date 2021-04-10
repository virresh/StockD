from kivy.uix.image import Image
from kivy.uix.behaviors import ButtonBehavior
from kivy.properties import ObjectProperty
from kivy.uix.layout import BoxLayout
import webbrowser

class SImageButton(ButtonBehavior, Image):
    link = ObjectProperty(None)

    def on_press(self):
        webbrowser.open(self.link)

class SDatePicker(BoxLayout):
    
