#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os, re,logging,time,datetime
from checker import Logger
logger = logging.getLogger('checker.DataType')

def strcmp(str1,str2):
    return str1.lower()==str2.lower()

def checkTypeOfColumn(*args, data, pattern,argsLen,argsIndex):
    flag = False
    logger.debug('type:{0}'.format(args[1]))
    if args:
        if data is None or data.strip() == '':
            if len(args) == argsLen and strcmp(args[argsIndex], 'Nullable'):
                logger.debug('this column could be nullable')
                flag = True
            else:
                logger.error('this column cannot be nullable')
        else:
            rr = re.compile(pattern)
            ret = rr.match(data)
            # print(ret)
            if ret is not None:
                logger.debug('match')
                flag = True
            else:
                logger.error('mismatch')
    return flag

def varcharColumn( *args,data):
    pattern = r'.*'
    flag = checkTypeOfColumn(*args,data=data,pattern=pattern,argsLen=4,argsIndex=3)
    return flag

def longtextColumn( *args,data):
    pattern = r'.*'
    flag = checkTypeOfColumn(*args,data=data,pattern=pattern,argsLen=3,argsIndex=2)
    return flag

def dateColumn(*args,data,dateFormat='US'):
    pattern_US=r"^[1-9]\d{3}/(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])$" #yyyy/mm/dd
    pattern_CN = r"^[1-9]\d{3}\-(?:0?[1-9]|1[0-2])\-(?:0?[1-9]|[12]\d|3[01])$" #yyyy/mm/dd
    pattern_GB=r"^[1-9]\d{3}/(?:0?[1-9]|[12]\d|3[01])/(?:0?[1-9]|1[0-2])$" #yyyy/dd/mm
    dateFormats = {'CN': pattern_CN, 'GB': pattern_GB, 'US': pattern_US}
    # zh_CN,en_GB,en_US
    # r"^[1-9]\d{3}\\(?:0?[1-9]|1[0-2])\\(?:0?[1-9]|[12]\d|3[01])$"  yyyy\mm\dd
    # r"^[1-9]\d{3}\\(?:0?[1-9]|[12]\d|3[01])\\(?:0?[1-9]|1[0-2])$" yyyy\dd\mm
    print(dateFormat.upper())
    dateFormat=dateFormat.upper()
    if dateFormat not in dateFormats:
        logger.error('Error: date format[{0}] is wrong, should be CN or GB or US.'.format(dateFormat))
        flag=False
    else:
        flag = checkTypeOfColumn(*args,data=data,pattern=dateFormats[dateFormat],argsLen=3,argsIndex=2)
    return flag



def longColumn(*args,data):
    pattern = r'^\-?\d+$'
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag


def integerColumn(*args,data):
    pattern = r'^\-?\d+$'
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def singleColumn(*args,data):
    pattern = r'^\-?\d+$'
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def doubleColumn(*args,data):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag


def decimalColumn(*args,data):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def getTypeRegex(*args,data,dateFormat='US'):
    # switch={'VARCHAR':r'[]','LONGTEXT':r'','DATE':r'','LONG':r'','INTEGER':r'','SINGLE':r'','DOUBLE':r'','DECIMAL':r''}
    switch = {'VARCHAR': varcharColumn, 'LONGTEXT': longtextColumn, 'DATE': dateColumn, 'LONG': longColumn, 'INTEGER': integerColumn, 'SINGLE': singleColumn, 'DOUBLE':doubleColumn,
              'DECIMAL': decimalColumn}
    try:
        return switch[args[1].upper()](*args,data=data,dateFormat=dateFormat)  # 执行相应的方法。
    except KeyError as e:
        logger.error('error[{0}] exist in getTypeRegex'.format(e))
        return False

if __name__=='__main__':
    li=['ID', 'VARCHAR', '20', 'Nullable']
    li1=['ID', 'VARCHAR', '20']
    li2=['ID', 'date', 'Nullable']
    li3=['ID', 'double']
    value=r'2000/3/9'
    getTypeRegex(*li,data=value)
    # print(time.localtime())
    # print(time.struct_time.tm_yday)
    # print(time.gmtime())
    # print(time.ctime())
    # help(time)