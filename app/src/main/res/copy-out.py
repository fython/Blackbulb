import os
import sys
import shutil

default_dir = os.getcwd()
DRAWABLE_DIR = 'drawable'
DRAWABLE_TYPE = ['mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi']

type = raw_input('Please input icon\'s category name: ')
category_dir = default_dir + '/' + type
while (not os.path.exists(category_dir)):
	type = raw_input(type + ' doesn\'t exist. Please try another one: ')
	category_dir = default_dir + '/' + type

icon_name = raw_input('What is the name of icon? (Such as \'ic_arrow_back_black_24dp\') ')

for i in range(0, len(DRAWABLE_TYPE)):
	src = category_dir + '/' + DRAWABLE_DIR + '-' + DRAWABLE_TYPE[i] + '/' + icon_name + '.png'
	target = default_dir + '/' + DRAWABLE_DIR + '-' + DRAWABLE_TYPE[i]
	if os.path.isfile(src):
		os.makedirs(target)
		shutil.copy(src, target)
		print('Copied ' + src + ' to ' + target + '!')
raw_input('Done!')