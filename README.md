# StockD
<p align="center">
    <img alt="Icon" src="python_client/app/static/img/icon.png">  
</p>  

[![Join the chat at https://gitter.im/virresh/StockD](https://badges.gitter.im/virresh/StockD.svg)](https://gitter.im/virresh/StockD?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![Downloads](https://img.shields.io/github/downloads/virresh/stockd/total)

EOD Stock data downloader  
Official Website: https://virresh.github.io/projects/stockd  
Subscribe to new releases via email on the project's official website.

If you find this software useful, consider [contributing](https://github.com/virresh/StockD/wiki/Contributing) to the project.  

## Features
- Download historical end-of-day data for equity and futures
- Features several download profiles, in case one server stops working
- Can use custom profiles for links and settings
- Convert the EOD data into a common format which can be used by several professional softwares for charting
- Can download delivery data for equity

## F.A.Q
- EXE is detected as a Virus!  
Given that you've downloaded the exe from official sources (the github release website or the official website, link above), the exe file should be virus free. Unfortunately many anti-viruses use inaccurate heuristics to detect viruses and will occasionally give you a false positive, especially when the exe is not signed. Since I'm an individual maintaining this software, I don't have enough funds to get the exe signed by a trusted provided, so if you run into this problem, I recommend you to make exceptions in your antivirus or compile the application from source (in case you don't trust the sources). I'll also add md5 hashes of the files so you can verify them post downloading to eliminate any possibility of virus injection.   
- Application doesn't start up!  
The application is slow to load when starting for the first time. Thus, please wait a while (~5 minutes) on the first load. If you get an error, or it doesn't start up, ensure you have latest [Microsoft Dot Net](https://dotnet.microsoft.com/download/dotnet-framework) version installed. If the application takes long time to start even after your first load, try making an exception fro StockD in your antivirus. Antiviruses might be interfering with any exe performing disk access and StockD is no exception. Using an SSD should also help speeding up program loading. Ideally it should start in ~5-10 seconds after the first load. Also, do not delete the `generate_config.json` file after the first load, otherwise the program will take the same time to start next time.  
- Who provides this data?  
The data provided can be set using given profiles. All the current profiles fetch data from Official NSE website. You may be able to use other community-supplied profiles as well.
- What is the Data Format?  
All data is saved into comma seperated txt files with 8 columns, viz - Symbol, Date, Open, High, Low, Close, Open Interest
- Selecting less indices still downloads data of all scripts?  
Yes. Indices are seperate from equity data. It is not feasible to download selected scrips only, instead it's way faster to download all data. Selecting indices only works when downloading data of indices, not scrips.
- Can't download data?  
There can be many reasons. First of all, check your internet connectivity. Next check if NSE actualy has data for the date that you want to download. Next you can try changing the link profiles, just in case your data might be present at one of the places.  
Another thing to try would be to enable Insecure Mode. This turns off ssl certificate checks, and is not recommended unless you trust the network you are operating on.  
Also, if your internet connectivity is slow, try increasing the request timeout. If all else fails, only then create an issue with the debug logs.
- How to download historical data?  
You should use either the Nse Daily or Nse Archives link profile for historical data. Note that in historical data, you will often face problems if trying to download delivery data as well (it is usually not available for > 1 year on). 
- How fast is the data download?  
It's extremely fast in my experience. I was able to download ~5 years of data in 30 minutes.  
However, if you're downloading lots of data, be mindful of NSE's server's bandwidth. NSE has two servers, the main server (www.nse...) and the old server (www1.nse...). The new server is extremely fast, but I advise every user to be respectful of their usage. Some general points would be:  
    - Download large amounts of data only during off-market hours. I recommend night time.
    - Downloading a day or even a week's data is fine. It's not a terribly large amount of data
- Where to get help?  
I welcome everyone to join in the [gitter chatroom](https://gitter.im/virresh/StockD) and help each other out.

### Steps to run (For Users), from version 4.1 onwards:
- Install Microsoft dot net (latest version. >= version 4.8 is recommended) from https://dotnet.microsoft.com/download/dotnet-framework. Not required on most modern windows, so feel free to skip this step and return to it later if the application doesn't start.
- Download the latest release from https://github.com/virresh/StockD/releases/latest.
- Run the .exe on Windows

In case you find any bugs, please file an issue: https://github.com/virresh/StockD/issues  
In case you need help/want to connect with other StockD users, join the chatroom at: https://gitter.im/virresh/StockD  
For a detailed User Guide, have a look at: https://github.com/virresh/StockD/wiki/User-Guide

### Steps to run (For Developers, from source code):  
For java client (deprecated)
```
$ mvn clean javafx:run
```  

For Python client (StockD v4.1 and above)
```
$ python runner.py
```
