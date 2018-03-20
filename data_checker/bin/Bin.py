#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os,sys
BASE_DIR=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(BASE_DIR)

from checker import Main
Main.main()
