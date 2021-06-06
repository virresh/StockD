from kivy.uix.image import Image
from kivy.uix.behaviors import ButtonBehavior
from kivy.properties import ObjectProperty
from kivy.uix.boxlayout import BoxLayout
from kivymd.uix.behaviors import CircularRippleBehavior
from kivymd.uix.behaviors import HoverBehavior
from kivy.core.window import Window
import webbrowser

class SImageButton(HoverBehavior, CircularRippleBehavior, ButtonBehavior, Image):
    link = ObjectProperty(None)

    def on_press(self):
        webbrowser.open(self.link)
    
    def on_enter(self, *args):
        Window.set_system_cursor("hand")

    def on_leave(self, *args):
        Window.set_system_cursor("arrow")
