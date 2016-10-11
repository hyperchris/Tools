# Author: Xiaochen
# Date: 07/30/2015
# Version: 3.3
# Note: Add strength comaprison and freq comparison
# Function: Find out the pickup gesture from two records. Then calculate the difference between the two selected data
# Usage: python parse.py [path_1] [path_2] [sensor] [distance_type] [bar_range] [mode] 
# 	- path_1 and path_2 must end with '/'
#	- sensor must have '.dat'

from dtw import dtw 
import pylab as pl
import numpy as np
from numpy.linalg import norm
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import scipy.fftpack
import sys, os

BAR_RANGE = 7.5 
HOME = 'home.dat'
ACC = 'acc.dat'
DELTA_TIME = 40
TIME_WINDOW = 10000 # 10 sec
STABLE_WINDOW = 1500 # 1.5 sec
UPPER = 10.5
LOWER = 8.5

# Three functions are used for fetching specific data from one line of file input
def get_timestamp(line):
	return long(line.split(' ')[0])

def get_strength(line):
	if ',' in line: # 3-d data
		data_unit = line.split(' ')[-1].split('\n')[0].split(',')
		return (float(data_unit[0]) ** 2 + float(data_unit[1]) ** 2 + float(data_unit[2]) ** 2) ** 0.5
	else: # 1-d data
		data_unit = line.split(' ')[-1].split('\n')[0]
		return np.abs(float(data_unit))

def get_data(line):
	if ',' in line: # 3-d data
		data_unit = line.split(' ')[-1].split('\n')[0].split(',')
		return [float(data_unit[0]), float(data_unit[1]), float(data_unit[2])]
	else: # 1-d data
		return [float(line.split(' ')[-1].split('\n')[0])]

# user defined custom distance
def my_custom_dist(x, y):
	return (x * x) + (y * y)

# plot dtw calculation path
def dtw_plot(dist, cost, path):
	print 'Minimum distance found:', dist
	pl.imshow(cost.T, origin='lower', cmap=pl.cm.gray, interpolation='nearest')
	pl.plot(path[0], path[1], 'w')
	pl.xlim((-0.5, cost.shape[0]-0.5))
	pl.ylim((-0.5, cost.shape[1]-0.5))
	pl.show()

# calculate Euclidean Distance
def euclidean_dist(list1, list2):
	result = 0
	list_size = len(list1) # find the min size 
	if len(list1) > len(list2):
		list_size = len(list2)
	for i in range(list_size):
		index = -1-i
		if type(list1[0]) is list: # three-dimentional data
			for j in range(3):
				result += (list1[index][j] - list2[index][j]) ** 2
		else: # one-dimentional data
			result += (list1[index] - list2[index]) ** 2
	return result ** 0.5

# Plot heatmap
# input data format: data = [[1,2,3,5], [4,3,1,0], [2,4,1,5]]; size = 4
def heatmap_plot(data, size, ratio, dir_name):
	im = plt.imshow(data, interpolation='none', aspect=ratio) # change the aspect if needed
	plt.xticks(range(size))
	plt.jet()
	plt.colorbar()
	plt.clim(0,BAR_RANGE)
	# plt.show()
	plt.savefig(dir_name + 'comp.png')
	plt.close()

# calculate multiple types of distance
def cal_dist(type, list1, list2):
	if 'ED' == type:
		return euclidean_dist(list1, list2) 
	elif 'DTW' == type:
		dist, cost, path = dtw(list1, list2)
		return dist
	elif 'DTWD' == type:
		if len(list1) > len(list2): # make both lists same long
			list1 = list1[-len(list2):-1]
		else:
			list2 = list2[-len(list1):-1]
		dist_dtw = cal_dist('DTW', list1, list2)
		dist_ed = cal_dist('ED', list1, list2)
		if 0 == dist_ed:
			return 0
		else:
			return dist_dtw / dist_ed 
	else:
		print 'ERROR: wrong distance type!'
		return 0

# calculate dtw and print result
def data_process(data1, data2, distance_type, dir_name):
	result = []
	for i in range(len(data1)):
		result_line = []
		for j in range(0, len(data2)):
			result_line.append(cal_dist(distance_type, data1[i], data2[j]))
		result.append(result_line)
		print 'i:' + `i`
		print result_line
	heatmap_plot(result, len(data2), 5.0/5, dir_name)

# find pickup behavior, return [pickup, unlock_time]
def find_pickup(path):
	print 'find_pickup starts!'
	# read lines of home file to find unlock actions
	unlock_time = []
	home_file = open(path + HOME, 'r')
	print path + HOME + ' file opened!'
	home_line = home_file.readline()
	while home_line != '': 
		if home_line.split(' ')[1] == 'UNLOCK\n':
			unlock_time.append(long(home_line.split(' ')[0]))
		home_line = home_file.readline()
	home_file.close()
	print 'Unlock history:'
	print unlock_time

	# get acc strength w/i time window
	acc_strength = []
	acc_time = []
	acc = open(path + ACC, 'r')
	for i in range(len(unlock_time)):
		# print 'processing unlock time ' + `i` + ': ' + `unlock_time[i]`
		acc_line = acc.readline()
		while acc_line != '' and get_timestamp(acc_line) < unlock_time[i] - TIME_WINDOW:
			acc_line = acc.readline() # pad to the unlock time - time window
		tmp_strength = []
		tmp_time = []
		last_ts = long(0)
		while acc_line != '' and get_timestamp(acc_line) <= unlock_time[i]:
			if get_timestamp(acc_line) > last_ts: # in case that some timestamp is in revered order
				tmp_strength.append(get_strength(acc_line)) # append data in the time window
				tmp_time.append(get_timestamp(acc_line))
				last_ts = get_timestamp(acc_line)
			acc_line = acc.readline()
		acc_strength.append(tmp_strength)
		acc_time.append(tmp_time)
	acc.close()

	# go back to find the pickup point
	result = []
	for i in range(len(acc_strength)):
		first_stable = long(0)
		found_pickup = False
		for j in reversed(range(0, len(acc_strength[i]))):
			if first_stable == 0 and acc_strength[i][j] < UPPER and acc_strength[i][j] > LOWER:
				first_stable = acc_time[i][j] # set first stable time
			elif first_stable > 0 and first_stable - acc_time[i][j] < STABLE_WINDOW and (acc_strength[i][j] > UPPER or acc_strength[i][j] < LOWER):
				first_stable = 0 # reset first stable time because of violation
			elif first_stable > 0 and first_stable - acc_time[i][j] >= STABLE_WINDOW:
				result.append([first_stable, unlock_time[i]])
				print 'pickup time found: ' + `i` + ' length of stability: ' + `(first_stable - acc_time[i][j])` + ' length for all: ' + `(acc_time[i][-1] - acc_time[i][0])`
				if acc_time[i][j] < acc_time[i][0]:
					print 'j:' + `j` + ' acc_time_i_j:' + `acc_time[i][j]` + ' acc_time_i_0:' + `acc_time[i][0]`
					print acc_time[i]
				found_pickup = True
				break
		if not found_pickup:
			print 'pickup not found........... in round ' + `i`  + ' length for all: ' + `(acc_time[i][-1] - acc_time[i][0])`
	print 'special points size: ' + `len(result)`
	return result

# print the strength and two special timestamps
def print_strength(data_file, special_pairs, dir_name, suffix):
	# Find the pickup behaviors
	data = open(data_file, 'r')
	data_line = data.readline()
	data_strength = []
	special_pts = [] # specials points include pickup and unlock
	special_time = [] # the time of pickup and unlock (but in the same array)
	for i in range(len(special_pairs)): # generate the special time array
		for j in range(len(special_pairs[i])):
			special_time.append(special_pairs[i][j])
	special_index = 0
	while data_line != '': # generate the data for two spcial timestamps 
		data_strength.append(get_strength(data_line)) # record strength
		if special_index < len(special_time): # record special points
			delta = get_timestamp(data_line) - special_time[special_index]
			if delta < DELTA_TIME and delta > -DELTA_TIME: # is special point
				if special_index % 2 == 0: # pickup 
					special_pts.append(1)
				else: # unlock
					special_pts.append(2)
				special_index += 1
			elif delta > DELTA_TIME and special_index == 0: # Move the index if we missed the first special point
				special_index += 1
				special_pts.append(0)
			else:
				special_pts.append(0)
		data_line = data.readline()
	data.close()

	# plot data
	x = []
	for i in range(len(data_strength)):
		x.append(i)
	pl.plot(x, data_strength)
	for i in range(len(special_pts)):
		if special_pts[i] != 0:
			pa = [i, i]
			pb = [-5, 25]
			if special_pts[i] == 1:
				pl.plot(pa, pb, 'r')
			elif special_pts[i] == 2:
				pl.plot(pa, pb, 'g')
	pl.savefig(dir_name + data_file.split('/')[-2] + suffix + '.png')
	pl.close()

# trim data according to the special time pairs: [pickup, unlock]
def trim_data(data_file, special_pairs):
	data = open(data_file, 'r')
	result = []
	for i in range(len(special_pairs)):
		# print 'Trimming data from ' + `special_pairs[i][0]` + ' to ' + `special_pairs[i][1]`
		data_line = data.readline()
		while data_line != '' and get_timestamp(data_line) < special_pairs[i][0]:
			data_line = data.readline() # pad to the unlock time
		tmp_data = []
		last_ts = long(0)
		while data_line != '' and get_timestamp(data_line) <= special_pairs[i][1]:
			if get_timestamp(data_line) > last_ts:
				tmp_data.append(get_data(data_line)) # append data in the time window
				last_ts = get_timestamp(data_line)
			data_line = data.readline()
		result.append(tmp_data)
		# print 'result len: ' + `len(result)`
	data.close()
	return result

# store the trimmed data (acc, gyro, mag) into files
def data_store(data, dir_name, data_path):
	for i in range(len(data)):
		print 'store data ' + `i` + ' into ' + dir_name
		file_name = dir_name + data_path.split('/')[-2][0:3] + '_' + `i` + '.txt'
		dout = open(file_name,'w')
		for j in range(len(data[i])):
			if len(data[i][j]) == 9: # 9-d data: acc, gyro, mag
				dout.write(`data[i][j][0]` + ' ' + `data[i][j][1]` + ' ' + `data[i][j][2]` + ' ' +\
						`data[i][j][3]` + ' ' + `data[i][j][4]` + ' ' + `data[i][j][5]` + ' ' +\
						`data[i][j][6]` + ' ' + `data[i][j][7]` + ' ' + `data[i][j][8]` + '\n')
			else: # 1-d data
				dout.write(`data[i][j][0]` + '\n')
		dout.close()

# input: trimmed data, output: their strength
def cal_strength(data):
	result = []
	for i in range(len(data)):
		print 'calculating strength for record ' + `i`
		tmp_result = []
		for j in range(len(data[i])):
			if len(data[i][j]) == 3: # 3-d data
				tmp_result.append((data[i][j][0] ** 2 + data[i][j][1] ** 2 + data[i][j][2] ** 2) ** 0.5)
			else: # 1-d data
				tmp_result.append(data[i][j][0])
		result.append(tmp_result)
	return result

def cal_freq(data):
	result = []
	for i in range(len(data)):
		print 'calculating freq for record ' + `i`
		df = scipy.fftpack.fft(data[i])
		result.append(np.abs(df[1:len(df)/2]))
		# show the data
	return result

def normalize(data):
	result = []
	for i in range(len(data)):
		print 'normalizing record ' + `i`
		tmp_result = []
		if len(data[i][0]) == 3: # 3-d data = (x/A, y/A, z/A), A = strength
			for j in range(len(data[i])):
				strength = (data[i][j][0] ** 2 + data[i][j][1] ** 2 + data[i][j][2] ** 2) ** 0.5
				tmp_result.append([data[i][j][0]/strength, data[i][j][1]/strength, data[i][j][2]/strength])
		else: # 1-d data = (data - avg) / standard_division
			avg = 0.0
			for j in range(len(data[i])):
				avg += data[i][j][0]
			avg /= len(data[i])
			div_2 = 0.0
			for j in range(len(data[i])):
				div_2 += (data[i][j][0] - avg) ** 2
			s_d = 0.0
			if len(data[i]) > 1: # n - 1 > 0
				s_d = (div_2 / (len(data[i]) - 1)) ** 0.5
			else: # n == 1
				result.append(data[i][0][0])
				continue
			for j in range(len(data[i])):
				tmp_result.append([(data[i][j][0] - avg) / s_d])
		result.append(tmp_result)
	return result

def merge_data(acc, gyro, mag):
	result = []
	if len(acc) != len(gyro) or len(acc) != len(mag):
		print 'ERROR: three datasets have different length: ' + `len(acc)` + ' ' + `len(gyro)` + '  ' + `len(mag)`
		return []
	print 'len of merged data is ' + `len(acc)`
	for i in range(len(acc)):
		tmp_res = []
		shortest_size = len(acc[i])
		if shortest_size > len(gyro[i]):
			shortest_size = len(gyro[i])
		if shortest_size > len(mag[i]):
			shortest_size = len(mag[i])
		for j in range(shortest_size):
			tmp_res.append([acc[i][j][0], acc[i][j][1], acc[i][j][2], gyro[i][j][0], gyro[i][j][1], gyro[i][j][2], mag[i][j][0], mag[i][j][1], mag[i][j][2]])
		result.append(tmp_res)
	return result

# Usage: $ parse.py [path_1] [path_2] [sensor] [distance_type] [bar_range] [mode]
if __name__ == "__main__":
	print 'start'
	# Check the folder path
	if len(sys.argv) != 7:
		sys.exit('ERROR: wrong argc! The format should be like:\n $ python parse.py [path_1] [path_2] [sensor] [distance_type] [bar_range]')
	if (not os.path.exists(sys.argv[1])) or (not os.path.exists(sys.argv[2])):
		sys.exit('ERROR: ' + sys.argv[1] + ' or ' + sys.argv[2] + ' was not found!')

	path_1 = sys.argv[1]
	path_2 = sys.argv[2]
	sensor = sys.argv[3]
	dist_type = sys.argv[4]
	BAR_RANGE = sys.argv[5]
	mode = sys.argv[6]

	# create result dir
	dir_name = mode + '_' + dist_type + '_' + path_1.split('/')[-2][0:3] + '_' + path_2.split('/')[-2][0:3] + '/'
	if not os.path.exists(dir_name):
		os.makedirs(dir_name)

	# find pickup time
	pickup_time_1 = find_pickup(path_1)
	print_strength(path_1 + sensor, pickup_time_1, dir_name, '_stregth') # view the trimming result

	pickup_time_2 = find_pickup(path_2)
	print_strength(path_2 + sensor, pickup_time_2, dir_name, '_stregth') # view the trimming result

	# trim data according from 'pickup' to 'unlock'
	acc_1 = trim_data(path_1 + 'acc.dat', pickup_time_1)
	gyro_1 = trim_data(path_1 + 'gyro.dat', pickup_time_1)
	mag_1 = trim_data(path_1 + 'mag.dat', pickup_time_1)


	acc_2 = trim_data(path_1 + 'acc.dat', pickup_time_1)
	gyro_2 = trim_data(path_1 + 'gyro.dat', pickup_time_1)
	mag_2 = trim_data(path_1 + 'mag.dat', pickup_time_1)
	
	# merge the data
	data_1 = merge_data(acc_1, gyro_1, mag_1)
	data_2 = merge_data(acc_2, gyro_2, mag_2)

	# normalize the data
	# data_1 = normalize(data_1)
	# data_2 = normalize(data_2)

	if mode == 'store': # store the trimmed data
		data_store(data_1, dir_name, path_1)
		data_store(data_2, dir_name, path_2)
	elif mode == 'comp_data': # calculate the distance and print
		data_process(data_1, data_2, dist_type, dir_name)
	elif mode == 'comp_strength': # calculate the distance based on the strength
		strength_1 = cal_strength(data_1)
		strength_2 = cal_strength(data_2)
		data_process(strength_1, strength_2, dist_type, dir_name)
	elif mode == 'comp_freq':
		freq_1 = cal_freq(cal_strength(data_1))
		freq_2 = cal_freq(cal_strength(data_2))
		data_process(freq_1, freq_2, dist_type, dir_name)
	else:
		print 'Error format of mode! You can only input \'store\' or \'calculate\' or \'comp_strength\' or \'comp_freq\'!'

	print 'done'