# Author: Chris 
# Time: 0610
# Function: batch download multiple links to files

import urllib2, os
import subprocess

url_file_path = 'url_download.txt' # replace these two lines if necessary
dir_name = 'download_dir'

print "start"
# create the download folder
cmd = "mkdir " + dir_name
os.popen(cmd)

url_file = open(url_file_path, 'r')

while True: # read urls line by line
	url = url_file.readline()
	if url == '':	# eof
		break;
	if len(url) < 4: # exclue empty lines
		continue
	print 'downloading url: ' + url

	if not "://" in url: # format checking
		continue

	if "." in url.split('/')[-1]: # if the download link has a file suffix
		suffix = url.split('/')[-1].split('.')[-1]
		if len(suffix) >= 2 and len(suffix) <= 4:
			cmd = "cd " + dir_name + "; curl -O " + url
			os.popen(cmd)
			continue
		else:
			print 'cannot download the file (suffix format error)!'
			
	response = urllib2.urlopen(url) # we can't use curl if url is not a file
	download_file = response.read()		
	file_name = 'tmp.html'
	if 'Content-Disposition' in response.info():
		if "filename" in response.info()['Content-Disposition']:
			file_name = response.info()['Content-Disposition'].split('filename=\"')[-1].split('\"')[0]
	else:
		print "Not a pdf or ppt!"

	version = 1
	while os.path.isfile(dir_name + '/' + file_name): # in case of duplication
		file_name = file_name.split('.')[0] + str(version) + '.' + file_name.split('.')[-1]
		version += 1

	fd = open(dir_name + '/' + file_name, 'w') # store the downloaded file
	fd.write(download_file)
	fd.close()

url_file.close()
print 'done'
