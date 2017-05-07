# StalkBoard
Live twitter stream analysis using Spark Streaming

## Architecture:
![Architecture](https://github.com/sagarnanduunc/StalkBoard/blob/master/Architecture.JPG)
We collect our tweets using Twitter Streaming API in Python. We bounce these tweets onto a socket. We then collect these tweets over the socket using Spark Streaming using Socket Streaming. We push analyzed data over elasticsearch over an index with a particular mapping. Kibana then takes this data on the mapping and gives us different visual analysis. 

## Dashboards Created:
### Count Dashboard
![Count](https://github.com/sagarnanduunc/StalkBoard/blob/master/Images/Generic%20Dashboard.JPG)