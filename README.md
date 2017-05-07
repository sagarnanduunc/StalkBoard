# StalkBoard
Live twitter stream analysis using Spark Streaming

## Spark Streaming Setup on Eclipse IDE:
Please refer the setup file

## Data Collection
Server.ipynb is IPython notebook which can be run to push stream live tweets over socket on port: 9994.
This code will give you geo-located tweets all over USA


## Architecture:
![Architecture](https://github.com/sagarnanduunc/StalkBoard/blob/master/Architecture.JPG)
We collect our tweets using Twitter Streaming API in Python. We bounce these tweets onto a socket. We then collect these tweets over the socket using Spark Streaming using Socket Streaming. We push analyzed data over elasticsearch over an index with a particular mapping. Kibana then takes this data on the mapping and gives us different visual analysis. 

## Dashboards Created:
### Count Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Generic%20Dashboard.JPG)

### Hastags Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Hashtag%20Analysis%20Dashboard.JPG)

### Geo-Point Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Geo%20Points%20Daashboard.JPG)

### Place Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Places%20Dashboard.JPG)

### Device Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Device%20Dashboard.JPG)

### Domination Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Domination.jpg)