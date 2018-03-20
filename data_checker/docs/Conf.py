#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
BASE_DIR=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

if __name__=='__main__':
    print(BASE_DIR)
    print(os.path.dirname(BASE_DIR))