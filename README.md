# Tools
**Version:** 1.1

##Description  
- vimrc: A sample of vimrc
- Py: Some code that can be used for data processing
- Android: a useful app for data collection

## Py

### web_download.py
Download data from a website (written in JSP). By using selenium and firefox, the script can go through all 1936 pages and save the necessary data into a csv file.

### face_ranking.py
[FaceValue] The user can give score to all photos in a given folder. The scores will be stored in a local csv file. (Read the code for detailed instruction)

### process_sensor_data.py
[PickupAuthentication] Some important things:
- Heatmap
- DTW, DTWD, Euclidean distance
- Find pickup gesture based on ACC strength changes
- Data normalization
- Plot strength

### BatchDownload
Download multiple links into files

*Usage*
- Download the folder
- Paste all URLs into the 'url_download.txt' (each URL should be in a single line)
- Open your terminal, 'cd' to the 'BatchDownload' folder. Then use command: python multi_download.py to run the script.
- Wait till the program prints 'done'. All downloaded files can be found in the folder called 'download_dir'.

##Android

###SensorMeter
[PickupAuthentication] Used for collecting sensor data on both smartphone and Android watches. The app can run in background and can capture events like HOME, UNLOCK, etc.
