#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os, re,logging,time,datetime
from checker import Logger
logger = logging.getLogger('checker.DataType')

def strcmp(str1,str2):
    return str1.lower()==str2.lower()

def checkTypeOfColumn(*args, **kwargs):
    flag = False
    logger.debug('type:{0}'.format(args[1]))
    if args:
        if kwargs['data'] is None or kwargs['data'].strip() == '':
            if len(args) == kwargs['argsLen'] and strcmp(args[kwargs['argsIndex']], 'Nullable'):
                logger.debug('this column could be nullable')
                flag = True
            else:
                logger.error('this column cannot be nullable')
        else:
            rr = re.compile(kwargs['pattern'])
            ret = rr.match(kwargs['data'])
            # print(ret)
            if ret is not None:
                logger.debug('match')
                flag = True
            else:
                logger.error('mismatch')
    return flag

def varcharColumn( *args,**kwargs):
    pattern = r'.*'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag = checkTypeOfColumn(*args,data=data,pattern=pattern,argsLen=4,argsIndex=3)
    return flag

def longtextColumn( *args,**kwargs):
    pattern = r'.*'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag = checkTypeOfColumn(*args,data=data,pattern=pattern,argsLen=3,argsIndex=2)
    return flag

def dateColumn(*args,**kwargs):
    pattern_US=r"^[1-9]\d{3}/(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])$" #yyyy/mm/dd
    pattern_CN = r"^[1-9]\d{3}\-(?:0?[1-9]|1[0-2])\-(?:0?[1-9]|[12]\d|3[01])$" #yyyy/mm/dd
    pattern_GB=r"^[1-9]\d{3}/(?:0?[1-9]|[12]\d|3[01])/(?:0?[1-9]|1[0-2])$" #yyyy/dd/mm
    dateFormats = {'CN': pattern_CN, 'GB': pattern_GB, 'US': pattern_US}
    # zh_CN,en_GB,en_US
    # r"^[1-9]\d{3}\\(?:0?[1-9]|1[0-2])\\(?:0?[1-9]|[12]\d|3[01])$"  yyyy\mm\dd
    # r"^[1-9]\d{3}\\(?:0?[1-9]|[12]\d|3[01])\\(?:0?[1-9]|1[0-2])$" yyyy\dd\mm
    if 'dateFormat' not in kwargs:
        dateFormat='US'
    else:
        dateFormat=kwargs['dateFormat']
        dateFormat=dateFormat.upper()
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data = kwargs['data']
    if dateFormat not in dateFormats:
        logger.error('Error: date format[{0}] is wrong, should be CN or GB or US.'.format(dateFormat))
        flag=False
    else:
        flag = checkTypeOfColumn(*args,data=data,pattern=dateFormats[dateFormat],argsLen=3,argsIndex=2)
    return flag


def longColumn(*args,**kwargs):
    pattern = r'^\-?\d+$'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag


def integerColumn(*args,**kwargs):
    pattern = r'^\-?\d+$'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def singleColumn(*args,**kwargs):
    pattern = r'^\-?\d+$'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def doubleColumn(*args,**kwargs):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag


def decimalColumn(*args,**kwargs):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    if 'data' not in kwargs:
        logger.error('Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2)
    return flag

def getTypeRegex(*args,**kwargs):
    # switch={'VARCHAR':r'[]','LONGTEXT':r'','DATE':r'','LONG':r'','INTEGER':r'','SINGLE':r'','DOUBLE':r'','DECIMAL':r''}
    switch = {'VARCHAR': varcharColumn, 'LONGTEXT': longtextColumn, 'DATE': dateColumn, 'LONG': longColumn, 'INTEGER': integerColumn, 'SINGLE': singleColumn, 'DOUBLE':doubleColumn,
              'DECIMAL': decimalColumn}
    try:
        return switch[args[1].upper()](*args,**kwargs)  # 执行相应的方法。
    except KeyError as e:
        logger.error('error[{0}] exist in getTypeRegex'.format(e))
        return False

if __name__=='__main__':
    li=['ID', 'VARCHAR', '20', 'Nullable']
    li1=['ID', 'VARCHAR', '20']
    li2=['ID', 'date', 'Nullable']
    li3=['ID', 'double']
    value=r'2000/3/9'
    getTypeRegex(*li1,data=value,dateFormat='US')
    # print(time.localtime())
    # print(time.struct_time.tm_yday)
    # print(time.gmtime())
    # print(time.ctime())
    # help(time)