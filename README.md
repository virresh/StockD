# StockD
[![Join the chat at https://gitter.im/virresh/StockD](https://badges.gitter.im/virresh/StockD.svg)](https://gitter.im/virresh/StockD?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

EOD Stock data downloader

## Features
- Download historical end-of-day data for equity and futures
- Features three download profiles
- Can use custom profiles for links and settings

## F.A.Q
- Who provides this data?  
The data provided can be set using given profiles. All the current profiles fetch data from Official NSE website. You may be able to use other community-supplied profiles as well.
- Selecting less indices still downloads data of all scripts?  
Yes. Indices are seperate from equity data. It is not feasible to download selected scrips only, instead it's way faster to download all data. Selecting indices only works when downloading data of indices, not scrips.
- Can't download data?  
There can be many reasons. First of all, check your internet connectivity. Next check if NSE actualy has data for the date that you want to download. Next you can try changing the link profiles, just in case your data might be present at one of the places.
- How fast is the data download?  
It's extremely fast in my experience. I was able to download ~5 years of data in 30 minutes.  
However, if you're downloading lots of data, be mindful of NSE's server's bandwidth. NSE has two servers, the main server (www.nse...) and the old server (www1.nse...). The new server is extremely fast, but I advise every user to be respectful of their usage. Some general points would be:  
    - Download large amounts of data only during off-market hours. I recommend night time.
    - Downloading a day or even a week's data is fine. It's not a terribly large amount of data
- Where to get help?  
I welcome everyone to join in the [gitter chatroom](https://gitter.im/virresh/StockD) and help each other out.

Steps to run:  
```
$ mvn clean javafx:run
```
