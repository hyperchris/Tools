# Import the os module, for the os.walk function
import os
import sys

def get_timestamp(line):
	return long(line.split(' ')[0])
 
def ts_check(file, indent):
	f = open(file, 'r')
	line = f.readline()
	last_ts = long(0)
	while line != '':
		if get_timestamp(line) < last_ts:
			print indent + '------------' + `get_timestamp(line)` + ' ' + `last_ts` + '-------------'
		last_ts = get_timestamp(line)
		line = f.readline()
	f.close()

# Set the directory you want to start from
def dir_walk(rootDir, indent):
	for dirName, subdirList, fileList in os.walk(rootDir):
		print indent + dirName
		for fname in fileList:
			print indent + '\t'+ fname
			if 'acc.dat' in fname or 'gyro.dat' in fname or 'mag.dat' in fname:
				ts_check(dirName + '/' + fname, indent + '\t')
		for dname in subdirList:
			dir_walk(rootDir + dname, '\t')

if __name__ == '__main__':
	# Check the folder path
	if len(sys.argv) != 2:
		sys.exit('ERROR: wrong argc!')
	if not os.path.exists(sys.argv[1]):
		sys.exit('ERROR: ' + sys.argv[1] + ' was not found!')

	dir_walk(sys.argv[1], '')