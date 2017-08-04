# !/usr/bin/python
import os
import subprocess
n=subprocess.check_output("sudo lsof -t -i:8888",shell=True)
os.system("sudo kill -9 %s" %n)

