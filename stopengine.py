# !/usr/bin/python
import os
import subprocess
n=subprocess.check_output("sudo lsof -t -i:8888",shell=True)

if n:
	os.system("sudo kill -9 %s" %n)
	print("LCC service has been successfully stopped.")
else:
	print("Failed to find LCC service on port 8888")

