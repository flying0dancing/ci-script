#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os, re,logging,time,datetime
from checker import Logger
logger = logging.getLogger('checker.DataType')

def strcmp(str1,str2):
    return str1.lower()==str2.lower()
def checkTypeOfStringColumn(*args, data, pattern,argsLen,argsIndex,tableName,col_id):
    flag = False
    logger.debug(r'type:{0}'.format(args[1]))
    if args:
        nullFlag=False
        if len(args) == argsLen and strcmp(args[argsIndex], 'Nullable'):
            nullFlag=True
        rr = re.compile(pattern)
        row_id=1
        data_flag=True
        for col_value in data:
            if col_value is None or col_value == '':
                if nullFlag:
                    logger.debug(r'this column could be nullable')
                else:
                    logger.error(r'this column cannot be nullable')
                    data_flag=False
            else:
                ret=rr.match(col_value)
                if ret is not None:
                    logger.debug(r'match:{4}- rec[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value,args[1:],tableName))
                    '''
                    qr = re.compile(r'\?{2,}')
                    qret = qr.search(col_value)
                    if qret is not None:
                        logger.error(r'mismatch: data might be contains special character.')
                        data_flag=False
                    else:
                        logger.debug(r'match:{4}- rec[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value,args[1:],tableName))
                    '''
                else:
                    logger.error(r'mismatch:{4}- rec[{0}] column[{5}][{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value, args[1:],tableName,args[0]))
                    data_flag=False
            row_id=row_id+1
        if data_flag:
            flag=data_flag
    return flag

def checkTypeOfColumn(*args, data, pattern,argsLen,argsIndex,tableName,col_id):
    flag = False
    logger.debug(r'type:{0}'.format(args[1]))
    if args:
        nullFlag=False
        if len(args) == argsLen and strcmp(args[argsIndex], 'Nullable'):
            nullFlag=True
        rr = re.compile(pattern)
        row_id=1
        data_flag=True
        
        for col_value in data:
            if col_value is None or col_value == '':
                if nullFlag:
                    logger.debug(r'this column could be nullable')
                else:
                    logger.error(r'this column cannot be nullable')
                    data_flag=False
            else:
                ret=rr.match(col_value)
                if ret is not None:
                    logger.debug(r'match:{4}- rec[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value,args[1:],tableName))
                else:
                    logger.error(r'mismatch:{4}- rec[{0}] column[{5}][{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value, args[1:],tableName,args[0]))
                    data_flag=False
            row_id=row_id+1
        if data_flag:
            flag=data_flag
    return flag

def varcharColumn( *args,**kwargs):
    flag = False
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return flag
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    if args:
        if int(args[2])>255:
            logger.error(r'mismatch: length of column[{0}]:{1} should be <= 255 in configuration'.format(args[0],args[2]))
            return flag
        else:
            #pattern =r'^[0-9a-zA-Z\u4E00-\u9FA5\s\`\~\!\@\#\$\%\^\&\*\(\)\_\-\=\+\\\|\{\[\}\]\;\:\'\"\,\<\.\>\/\?]{1,'+args[2]+r'}$'
            #pattern=r'^(?!.*([\？\?])\1+)^(?!.*\n+)^[÷éāǎǎàōóǒòêēéěèīíǐìūúǔùǖǘǚǜü\w\u4E00-\u9FA5\s\`\~\!\@\#\$\%\^\&\*\(\)\-\=\+\\\|\{\[\}\]\;\:\'\"\,\<\.\>\/\?\？\—\–±÷]+$'
            pattern=r'^(?!.*([\？\?])\1+)^(?:.|\s)*$'
            flag = checkTypeOfStringColumn(*args,data=data,pattern=pattern,argsLen=4,argsIndex=3,tableName=tableName,col_id=col_id)    
    return flag

def longtextColumn( *args,**kwargs):
    #pattern = r'^[0-9a-zA-Z\u4E00-\u9FA5\s\`\~\!\@\#\$\%\^\&\*\(\)\_\-\=\+\\\|\{\[\}\]\;\:\'\"\,\<\.\>\/\?]+$'
    pattern=r'^(?!.*([\？\?])\1+)^(?:.|\s)*$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag = checkTypeOfStringColumn(*args,data=data,pattern=pattern,argsLen=3,argsIndex=2,tableName=tableName,col_id=col_id)
    return flag

def dateColumn(*args,**kwargs):
    #pattern_US=r"^[1-5]\d{3}/(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])(?:\s*(?:1?\d|2[0-4])(?:\:(?:[0-5]?\d|60)){2})?$" #yyyy/mm/dd
    #pattern_US=r"^[1-9]\d{3}/(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})?$" #yyyy/mm/dd delete 24 in hour,60 in min and sec
    #pattern_CN1 = r"^[1-9]\d{3}\-(?:0?[1-9]|1[0-2])\-(?:0?[1-9]|[12]\d|3[01])(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})?$" #yyyy-mm-dd
    #pattern_GB1=r"^(?:0?[1-9]|[12]\d|3[01])/(?:0?[1-9]|1[0-2])/[1-9]\d{3}(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})?$" #dd/mm/yyyy
    #pattern_US1=r"^(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])/[1-9]\d{3}(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})?$" #mm/dd/yyyy
    pattern_CN = r"^[1-9]\d{3}\-(?:0?[1-9]|1[0-2])\-(?:0?[1-9]|[12]\d|3[01])(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})$" #yyyy-mm-dd
    pattern_GB=r"^(?:0?[1-9]|[12]\d|3[01])/(?:0?[1-9]|1[0-2])/[1-9]\d{3}(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})$" #dd/mm/yyyy
    pattern_US=r"^(?:0?[1-9]|1[0-2])/(?:0?[1-9]|[12]\d|3[01])/[1-9]\d{3}(?:\s*(?:1?\d|2[0-3])(?:\:(?:[0-5]?\d)){2})$" #mm/dd/yyyy
    dateFormats = {'CN': pattern_CN, 'GB': pattern_GB, 'US': pattern_US}

    if 'dateFormat' not in kwargs:
        dateFormat='US'
    else:
        dateFormat=kwargs['dateFormat']
        dateFormat=dateFormat.upper()
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data = kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    if dateFormat not in dateFormats:
        logger.error(r'Error: date format[{0}] is wrong, should be CN or GB or US.'.format(dateFormat))
        flag=False
    else:
        flag = checkTypeOfColumn(*args,data=data,pattern=dateFormats[dateFormat],argsLen=3,argsIndex=2,tableName=tableName,col_id=col_id)
    return flag


def integerColumn(*args,**kwargs):
    pattern = r'^\-?\d+$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2,tableName=tableName,col_id=col_id)
    if flag:
        test_data=int(data)
        if test_data>32767 or test_data<-32768:
            logger.error(r'mismatch: data[{0}] overflows as integer type.'.format(data))
            flag=False
    return flag

def longColumn(*args,**kwargs):
    pattern = r'^\-?\d+$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag = checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2,tableName=tableName,col_id=col_id)
    if flag:
        test_data=int(data)
        if test_data>2147483648 or test_data<-2147483647:
            logger.error(r'mismatch: data[{0}] overflows as long type.'.format(data))
            flag=False
    return flag

def singleColumn(*args,**kwargs):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2,tableName=tableName,col_id=col_id)
    if flag:
        test_data=int(data)
        if test_data>3.402823e38 or test_data<-3.402823e38:
            logger.error('mismatch: data[{0}] overflows as long type.'.format(data))
            flag=False
    return flag

def doubleColumn(*args,**kwargs):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2,tableName=tableName,col_id=col_id)
    return flag


def decimalColumn(*args,**kwargs):
    pattern = r'^\-?\d+(\.\d+)?([Ee][\+\-]\d+)?$'
    if 'data' not in kwargs:
        logger.error(r'Error: key data not defined in kwargs')
        return False
    data=kwargs['data']
    tableName=kwargs['tableName']
    col_id=kwargs['col_id']
    flag =  checkTypeOfColumn(*args, data=data, pattern=pattern, argsLen=3, argsIndex=2,tableName=tableName,col_id=col_id)
    return flag

def getTypeRegex(*args,data,tableName,col_id,dateFormat='US'):
    flag=False
    switch = {'VARCHAR': varcharColumn, 'LONGTEXT': longtextColumn, 'DATE': dateColumn, 'LONG': longColumn, 'INTEGER': integerColumn, 'SINGLE': singleColumn, 'DOUBLE':doubleColumn,
              'DECIMAL': decimalColumn}
    try:
        flag= switch[args[1].upper()](*args,data=data,tableName=tableName,col_id=col_id,dateFormat=dateFormat)  # 执行相应的方法。
    except KeyError as e:
        logger.error(r'Error:{0} exist in getTypeRegex'.format(e.args))
    return flag

if __name__=='__main__':
    li=['ID', 'VARCHAR', '20', 'Nullable']
    li1=['ID', 'VARCHAR', '20']
    li2=['ID', 'date', 'Nullable']
    li3=['ID', 'double']

    # value=r'2000/3/9??'
    value = r'9/30/2014 0:59:59'
    getTypeRegex(*li2,data=value,dateFormat='us1')

    value = r'b3#森哈w哈欧文??.s'
    getTypeRegex(*li1, data=value, dateFormat='us1')

    # print(time.localtime())
    # print(time.struct_time.tm_yday)
    # print(time.gmtime())
    # print(time.ctime())
    # help(time)