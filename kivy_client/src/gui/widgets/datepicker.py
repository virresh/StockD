#!/usr/bin/python
# -*- coding: utf-8 -*-

###########################################################
# KivyCalendar (X11/MIT License)
# Calendar & Date picker widgets for Kivy (http://kivy.org)
# https://bitbucket.org/xxblx/kivycalendar
# 
# Oleg Kozlov (xxblx), 2015
# https://xxblx.bitbucket.org/
###########################################################
from kivy.lang import Builder
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.popup import Popup
from kivy.uix.relativelayout import RelativeLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.togglebutton import ToggleButton
from kivy.uix.textinput import TextInput
from kivy.uix.label import Label
from kivy.core.window import Window
from kivy.properties import NumericProperty, ReferenceListProperty

###########################################################
Builder.load_string("""
<ArrowButton>:
    background_normal: ""
    background_down: ""
    background_color: 1, 1, 1, 0
    size_hint: .1, .1
<MonthYearLabel>:
    pos_hint: {"top": 1, "center_x": .5}
    size_hint: None, 0.1
    halign: "center"
<MonthsManager>:
    pos_hint: {"top": .9}
    size_hint: 1, .9
<ButtonsGrid>:
    cols: 7
    rows: 7
    size_hint: 1, 1
    pos_hint: {"top": 1}
<DayAbbrLabel>:
    text_size: self.size[0], None
    halign: "center"
<DayAbbrWeekendLabel>:
    color: 1, 0, 0, 1
    
<DayButton>:
    group: "day_num"
    
<DayNumWeekendButton>:
    background_color: 1, 0, 0, 1
""")    
###########################################################

class DatePicker(TextInput):
    """ 
    Date picker is a textinput, if it focused shows popup with calendar
    which allows you to define the popup dimensions using pHint_x, pHint_y, 
    and the pHint lists, for example in kv:
    DatePicker:
        pHint: 0.7,0.4 
    would result in a size_hint of 0.7,0.4 being used to create the popup
    """
    pHint_x = NumericProperty(0.7)
    pHint_y = NumericProperty(0.7)
    pHint = ReferenceListProperty(pHint_x ,pHint_y)

    def __init__(self, touch_switch=False, *args, **kwargs):
        super(DatePicker, self).__init__(*args, **kwargs)
        
        self.touch_switch = touch_switch
        self.init_ui() 

    def init_ui(self):
        
        self.text = today_date()
        # Calendar
        self.cal = CalendarWidget(as_popup=True, 
                                  touch_switch=self.touch_switch)
        # Popup
        self.popup = Popup(content=self.cal, on_dismiss=self.update_value, 
                           title="")
        self.cal.parent_popup = self.popup
        
        self.bind(focus=self.show_popup)
        
    def show_popup(self, isnt, val):
        """ 
        Open popup if textinput focused, 
        and regardless update the popup size_hint 
        """
        self.popup.size_hint=self.pHint        
        if val:
            # Automatically dismiss the keyboard 
            # that results from the textInput 
            Window.release_all_keyboards()
            self.popup.open()
        
    def update_value(self, inst):
        """ Update textinput value on popup close """
            
        self.text = "%s-%s-%s" % tuple(self.cal.active_date)
        self.focus = False

class CalendarWidget(RelativeLayout):
    """ Basic calendar widget """
    
    def __init__(self, as_popup=False, touch_switch=False, *args, **kwargs):
        super(CalendarWidget, self).__init__(*args, **kwargs)
        
        self.as_popup = as_popup
        self.touch_switch = touch_switch
        self.prepare_data()     
        self.init_ui()
        
    def init_ui(self):
        
        self.left_arrow = ArrowButton(text="<", on_press=self.go_prev,
                                      pos_hint={"top": 1, "left": 0})
        
        self.right_arrow = ArrowButton(text=">", on_press=self.go_next,
                                       pos_hint={"top": 1, "right": 1})
        
        self.add_widget(self.left_arrow)        
        self.add_widget(self.right_arrow)
        
        # Title        
        self.title_label = MonthYearLabel(text=self.title)
        self.add_widget(self.title_label)
        
        # ScreenManager
        self.sm = MonthsManager()
        self.add_widget(self.sm)
        
        self.create_month_scr(self.quarter[1], toogle_today=True) 
    
    def create_month_scr(self, month, toogle_today=False):
        """ Screen with calendar for one month """        
        
        scr = Screen()
        m = self.month_names_eng[self.active_date[1] - 1]
        scr.name = "%s-%s" % (m, self.active_date[2])  # like march-2015
        
        # Grid for days
        grid_layout = ButtonsGrid()
        scr.add_widget(grid_layout)
        
        # Days abbrs 
        for i in range(7):
            if i >= 5:  # weekends
                l = DayAbbrWeekendLabel(text=self.days_abrs[i])
            else:  # work days
                l = DayAbbrLabel(text=self.days_abrs[i])
            
            grid_layout.add_widget(l)
            
        # Buttons with days numbers
        for week in month:
            for day in week:
                if day[1] >= 5:  # weekends
                    tbtn = DayNumWeekendButton(text=str(day[0]))
                else:  # work days
                    tbtn = DayNumButton(text=str(day[0]))
                
                tbtn.bind(on_press=self.get_btn_value)
                
                if toogle_today:
                    # Down today button
                    if day[0] == self.active_date[0] and day[2] == 1:
                        tbtn.state = "down"
                # Disable buttons with days from other months
                if day[2] == 0:
                    tbtn.disabled = True
                
                grid_layout.add_widget(tbtn)

        self.sm.add_widget(scr)
        
    def prepare_data(self):
        """ Prepare data for showing on widget loading """
    
        # Get days abbrs and month names lists 
        self.month_names = get_month_names()
        self.month_names_eng = get_month_names_eng()
        self.days_abrs = get_days_abbrs()    
        
        # Today date
        self.active_date = today_date_list()
        # Set title
        self.title = "%s - %s" % (self.month_names[self.active_date[1] - 1], 
                                  self.active_date[2])
                
        # Quarter where current month in the self.quarter[1]
        self.get_quarter()
    
    def get_quarter(self):
        """ Get caledar and months/years nums for quarter """
        
        self.quarter_nums = calc_quarter(self.active_date[2], 
                                                  self.active_date[1])
        self.quarter = get_quarter(self.active_date[2], 
                                            self.active_date[1])
    
    def get_btn_value(self, inst):
        """ Get day value from pressed button """
        
        self.active_date[0] = int(inst.text)
                
        if self.as_popup:
            self.parent_popup.dismiss()
        
    def go_prev(self, inst):
        """ Go to screen with previous month """        

        # Change active date
        self.active_date = [self.active_date[0], self.quarter_nums[0][1], 
                            self.quarter_nums[0][0]]

        # Name of prev screen
        n = self.quarter_nums[0][1] - 1
        prev_scr_name = "%s-%s" % (self.month_names_eng[n], 
                                   self.quarter_nums[0][0])
        
        # If it's doen't exitst, create it
        if not self.sm.has_screen(prev_scr_name):
            self.create_month_scr(self.quarter[0])
            
        self.sm.current = prev_scr_name
        self.sm.transition.direction = "left"
        
        self.get_quarter()
        self.title = "%s - %s" % (self.month_names[self.active_date[1] - 1], 
                                  self.active_date[2])
        
        self.title_label.text = self.title
    
    def go_next(self, inst):
        """ Go to screen with next month """
        
         # Change active date
        self.active_date = [self.active_date[0], self.quarter_nums[2][1], 
                            self.quarter_nums[2][0]]

        # Name of prev screen
        n = self.quarter_nums[2][1] - 1
        next_scr_name = "%s-%s" % (self.month_names_eng[n], 
                                   self.quarter_nums[2][0])
        
        # If it's doen't exitst, create it
        if not self.sm.has_screen(next_scr_name):
            self.create_month_scr(self.quarter[2])
            
        self.sm.current = next_scr_name
        self.sm.transition.direction = "right"
        
        self.get_quarter()
        self.title = "%s - %s" % (self.month_names[self.active_date[1] - 1], 
                                  self.active_date[2])
        
        self.title_label.text = self.title
        
    def on_touch_move(self, touch):
        """ Switch months pages by touch move """
                
        if self.touch_switch:
            # Left - prev
            if touch.dpos[0] < -30:
                self.go_prev(None)
            # Right - next
            elif touch.dpos[0] > 30:
                self.go_next(None)
        
class ArrowButton(Button):
    pass

class MonthYearLabel(Label):
    pass

class MonthsManager(ScreenManager):
    pass

class ButtonsGrid(GridLayout):
    pass

class DayAbbrLabel(Label):
    pass

class DayAbbrWeekendLabel(DayAbbrLabel):
    pass

class DayButton(ToggleButton):
    pass

class DayNumButton(DayButton):
    pass

class DayNumWeekendButton(DayButton):
    pass

#!/usr/bin/python
# -*- coding: utf-8 -*-

###########################################################
# KivyCalendar (X11/MIT License)
# Calendar & Date picker widgets for Kivy (http://kivy.org)
# https://bitbucket.org/xxblx/kivycalendar
# 
# Oleg Kozlov (xxblx), 2015
# https://xxblx.bitbucket.org/
###########################################################

from calendar import month_name, day_abbr, Calendar, monthrange
from datetime import datetime
from locale import getdefaultlocale

def get_month_names():
    """ Return list with months names """
    
    result = []
    # If it possible get months names in system language
    try:
        with TimeEncoding("%s.%s" % getdefaultlocale()) as time_enc:
            for i in range(1, 13):
                result.append(month_name[i].decode(time_enc))
                
        return result
    
    except:
        return get_month_names_eng()
        
def get_month_names_eng():
    """ Return list with months names in english """
    
    result = []
    for i in range(1, 13):
        result.append(month_name[i])
        
    return result

def get_days_abbrs():
    """ Return list with days abbreviations """
    
    result = []
    # If it possible get days abbrs in system language
    try:
        with TimeEncoding("%s.%s" % getdefaultlocale()) as time_enc:
            for i in range(7):
                result.append(day_abbr[i].decode(time_enc))    
    except:
        for i in range(7):
            result.append(day_abbr[i])
            
    return result

def calc_quarter(y, m):
    """ Calculate previous and next month """
    
    # Previous / Next month's year number and month number
    prev_y = y
    prev_m = m - 1
    next_y = y
    next_m = m + 1    
    
    if m == 1:
        prev_m = 12
        prev_y = y - 1
    elif m == 12:
        next_m = 1
        next_y = y + 1
        
    return [(prev_y, prev_m), (y, m), (next_y, next_m)]

def get_month(y, m):
    """ 
    Return list of month's weeks, which day 
    is a turple (<month day number>, <weekday number>) 
    """
    
    cal = Calendar()
    month = cal.monthdays2calendar(y, m)
    
    # Add additional num to every day which mark from 
    # this or from other day that day numer
    for week in range(len(month)):
        for day in range(len(month[week])):
            _day = month[week][day]
            if _day[0] == 0:
                this = 0
            else: 
                this = 1
            _day = (_day[0], _day[1], this)
            month[week][day] = _day
    
    # Days numbers of days from preious and next monthes
    # marked as 0 (zero), replace it with correct numbers
    # If month include 4 weeks it hasn't any zero
    if len(month) == 4:
        return month        
    
    quater = calc_quarter(y, m)
    
    # Zeros in first week    
    fcount = 0
    for i in month[0]:
        if i[0] == 0:
            fcount += 1
    
    # Zeros in last week
    lcount = 0
    for i in month[-1]:
        if i[0] == 0:
            lcount += 1
            
    if fcount:
        # Last day of prev month
        n = monthrange(quater[0][0], quater[0][1])[1]
        
        for i in range(fcount):
            month[0][i] = (n - (fcount - 1 - i), i, 0)
            
    if lcount:
        # First day of next month
        n = 1
        
        for i in range(lcount):
            month[-1][-lcount + i] = (n + i, 7 - lcount + i, 0)
            
    return month

def get_quarter(y, m):
    """ Get quarter where m is a middle month """
    
    result = []
    quarter = calc_quarter(y, m)
    for i in quarter:
        result.append(get_month(i[0], i[1]))
        
    return result

def today_date_list():
    """ Return list with today date """
    
    return [datetime.now().day, datetime.now().month, datetime.now().year]
    
def today_date():
    """ Return today date dd.mm.yyyy like 28.02.2015 """

    return datetime.now().strftime("%d/%m/%Y")



if __name__ == "__main__":
    from kivy.base import runTouchApp
    
    c = DatePicker()
    runTouchApp(c)
