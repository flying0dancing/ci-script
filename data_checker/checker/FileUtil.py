#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os, csv,logging,re
from checker import Logger
import configparser
logger = logging.getLogger('checker.FileUtil')
from checker import DataType

# specialTables=("GridKey","GridRef","List","Ref","Sums","Vals","XVals")

def lookAllCsvs(csvPath,csvList):
    for x in os.listdir(csvPath):
        if os.path.isdir(os.sep.join([csvPath, x])):
            lookAllCsvs(os.sep.join([csvPath, x]),csvList)
        if os.path.isfile(os.sep.join([csvPath, x])) and os.path.splitext(x)[1] == '.csv':
            logger.info('/'.join([csvPath, x]))
            csvList.append(os.sep.join([csvPath, x]))

def lookCsvsByFilter1(csvPath,csvFilter,dic):
    if dic is None:
        dic={}
    for x in os.listdir(csvPath):
        if os.path.isdir(os.sep.join([csvPath,x])):
            # print(os.sep.join([csvPath,x])+r'===='+csvFilter)
            lookCsvsByFilter1(os.sep.join([csvPath,x]),csvFilter,dic)
        if os.path.isfile(os.sep.join([csvPath,x])) and os.path.splitext(x)[1].lower()=='.csv':
            # print(os.sep.join([csvPath,x])+r'===='+csvFilter)
            if re.search(csvFilter, x,flags=re.IGNORECASE):
                logger.debug(r'Found file:{0}'.format(x))
                csvFileFullName = os.sep.join([csvPath, x])
                logger.info(r'Need to check:{0}'.format(csvFileFullName))
                dic[csvFileFullName]=x
                # csv_returnId = re.sub(r'(?:GridKey|GridRef|List|Ref|Sums|Vals|XVals)\_(\d+)\.csv', r'\1',x.lower(),flags=re.IGNORECASE)
                # flag =readColumns(csvFileFullName, propsFullName, csv_returnId, dateFormat)

def lookCsvsByFilter2(csvPath,csvFilter):
    for x in os.listdir(csvPath):
        if os.path.isdir(os.sep.join([csvPath,x])):
            lookCsvsByFilter1(os.sep.join([csvPath,x]),csvFilter)
        if os.path.isfile(os.sep.join([csvPath,x])) and os.path.splitext(x)[1].lower()=='.csv':
            if re.search(csvFilter, x,flags=re.IGNORECASE):
                logger.debug(r'Found file:{0}'.format(x))
                csvFileFullName = os.sep.join([csvPath, x])
                logger.info(r'Need to check:{0}'.format(csvFileFullName))
                yield {csvFileFullName:x}

def lookCsvsByFilter(csvPath, propsFullName,csvFilter, dateFormat='US'):
    flag=False
    for x in os.listdir(csvPath):
        if os.path.isdir(os.sep.join([csvPath,x])):
            # print(os.sep.join([csvPath,x])+r'===='+csvFilter)
            flag =lookCsvsByFilter(os.sep.join([csvPath,x]), propsFullName,csvFilter,dateFormat)
        if os.path.isfile(os.sep.join([csvPath,x])) and os.path.splitext(x)[1].lower()=='.csv':
            # print(os.sep.join([csvPath,x])+r'===='+csvFilter)
            if re.search(csvFilter, x,flags=re.IGNORECASE):
                logger.debug(r'Found file:{0}'.format(x))
                csvFileFullName = os.sep.join([csvPath, x])
                logger.info(r'checking file:{0}'.format(csvFileFullName))
                csv_returnId = re.sub(r'(?:GridKey|GridRef|List|Ref|Sums|Vals|XVals)\_(\d+)\.csv', r'\1',x.lower(),flags=re.IGNORECASE)
                flag =readColumns(csvFileFullName, propsFullName, csv_returnId, dateFormat)
            # else:
            #     print('error:{0}'.format(x))
    # print(r'flag in lookCsvsByFilter:{0}'.format(flag))
    return flag

def test_Cols(*args,col,tableName):
    row_id=2
    for col_value in col:
        logger.info(r'row[{1}], value:{0}'.format(col_value,row_id))
        row_id=row_id+1
    return True

def readColumn(csvFullName,col_id,col_props,file_id,dateFormat='US'):
    flag = False
    try:
        tableName=os.path.basename(os.path.splitext(csvFullName)[0])
        with open(csvFullName) as fh:
            col = (row[col_id - 1] for row in csv.reader(fh))
            col_name = next(col)
            dataFlag = True
            if col_name == col_props[0]:
                logger.debug(r'match:{3}- column[{0}] name:{1} props:{2}'.format(col_id, col_name, col_props[1:],tableName))
                # pattern=r'^(-?\d+)(\.\d+)?$'
                # rr=re.compile(pattern)
                if re.match(r'ReturnId',col_name,flags=re.IGNORECASE) and re.match(r'\d+',file_id):
                    row_id = 2
                    for col_value in col:
                        if file_id == col_value:
                            logger.debug(r'match:{4}- row[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value,col_props[1],tableName))
                        else:
                            logger.error(r'mismatch:{5}-  row[{0}] column[{1}]:{2}, expected:{4} type:[{3}]'.format(row_id, col_id, col_value, col_props[1],file_id,tableName))
                            dataFlag=False
                        row_id = row_id + 1
                else:
                    dataFlag=DataType.getTypeRegex(*col_props,data=col,tableName=tableName,col_id=col_id,dateFormat=dateFormat)
                    # for col_value in col:
                    #     flagTmp = DataType.getTypeRegex(*col_props, data=col_value, dateFormat=dateFormat)
                    #     if flagTmp:
                    #         logger.debug(r'match:{4}- row[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value,col_props[1],tableName))
                    #     else:
                    #         logger.error(r'mismatch:{4}- row[{0}] column[{1}]:{2} type:[{3}]'.format(row_id, col_id, col_value, col_props[1],tableName))
                    #         dataFlag = False
                    #     row_id = row_id + 1
                    
            else:
                logger.error(r'mismatch:{4}- column[{0}] name:{1}  [expected:{2}] other props:{3}'.format(col_id, col_name, col_props[0],col_props[1:],tableName))
                dataFlag = False
            if dataFlag:
                flag =True
    except IOError as eio:
        logger.error(r'IOError: File[{0}] cannot open.[{1}]'.format(csvFullName,eio.args))
    except IndexError as e:
        logger.error(r'IndexError:File[{0}] column [{1}] out of range[{2}]'.format(csvFullName,str(col_id),e.args))
    except BaseException as be:
        logger.error(r'Error: {0}'.format(be.args))
    return flag

def readColumns(csvFullName,propsFullName,file_id,dateFormat='US'):
    flag=False
    try:
        config_name=os.path.basename(propsFullName)
        tableName=os.path.basename(os.path.splitext(csvFullName)[0])
        table_name=tableName
        config = configparser.ConfigParser()
        config.read(propsFullName)
        if table_name not in config:
            logger.warning(r'table[{0}] not defined in {1}'.format(table_name,config_name))
            table_name=re.split(r'\_',table_name)[0]
            if table_name not in config:
                logger.warning(r'table[{0}] not defined in {1}'.format(table_name,config_name))
        if table_name in config:
            # """"
            csv_name = os.path.basename(csvFullName)
            with open(csvFullName) as fh:
                rowl = (row for row in csv.reader(fh))
                header = next(rowl)
            col_configlen = len(config.items(table_name))
            col_csvLen = len(header)
            if col_csvLen>col_configlen:
                logger.error('Error: please check count of column in data config is lesser than in csv.')
                logger.error('{0} [{1}] columns:{2},\t {3} columns:{4}'.format(config_name, table_name, col_configlen, csv_name, col_csvLen))
            elif col_csvLen==col_configlen:
                dataflag=True
                for key, value in config.items(table_name):
                    col_id=int(key[3:])
                    col_name=re.split(r'[ \(\)]+', value)[0]
                    if header[col_id-1]==col_name:
                        logger.debug('{0} [{1}] column[{2}]:{3},\t {4} column[{2}]:{5}'.format(config_name, table_name, col_id, col_name, csv_name, header[col_id - 1]))
                    else:
                        dataflag=False
                        logger.error('Error: please check column name.')
                        logger.error('{0} [{1}] column[{2}]:{3},\t {4} column[{2}]:{5}'.format(config_name, table_name, col_id, col_name, csv_name, header[col_id - 1]))
                if dataflag:
                    for key, value in config.items(table_name):
                        col_id = int(key[3:])
                        col_props = re.split(r'[ \(\)]+', value)
                        flagTmp = readColumn(csvFullName, col_id, col_props, file_id, dateFormat)
                        if not flagTmp:
                            dataflag = flagTmp
                if dataflag:
                    flag=dataflag
            else:
                logger.error('Error: please check count of column in data config is more than in csv.')
                logger.error('{0} [{1}] columns:{2},\t {3} columns:{4}'.format(config_name, table_name, col_configlen, csv_name, col_csvLen))
            #
            # dataflag=compareColumnsCount(csvFullName,propsFullName,table_name)
            # for key, value in config.items(table_name):
            #     col_id = int(key[3:])
            #     col_props = re.split(r'[ \(\)]+', value)
            #     flagTmp = readColumn(csvFullName, col_id, col_props, file_id, dateFormat)
            #     if not flagTmp:
            #         dataflag=False
            # if dataflag:
            #     flag=True
        else:
            logger.error(r'table[{0}] not defined in {1}'.format(tableName, config_name))
    except IOError as ioe:
        logger.error(r'IOError: File[{0}] cannot open.[{1}]'.format(csvFullName, ioe.args))
    except BaseException as be:
        logger.error(r'Error: {0}'.format(be.args))
    return flag

def compareColumnsCount(csvFullName, propsFullName, table_name):
    flag=False
    try:
        config_name=os.path.basename(propsFullName)
        config = configparser.ConfigParser()
        config.read(propsFullName)
        if config.items(table_name) is None or len(config.items(table_name))==0:
            logger.error(r'Error: please check [{0}] definition in {1}'.format(table_name, config_name))
        else:
            csv_name=os.path.basename(csvFullName)
            with open(csvFullName) as fh:
                rowl = (row for row in csv.reader(fh))
                header = next(rowl)
            col_configlen = len(config.items(table_name))
            col_csvLen=len(header)
            if col_csvLen>col_configlen:
                logger.error('Error: please check count of column in data config is lesser than in csv.')
                logger.error('{0} [{1}] columns:{2},\t {3} columns:{4}'.format(config_name, table_name, col_configlen, csv_name, col_csvLen))
            elif col_csvLen==col_configlen:
                dataflag=True
                for key, value in config.items(table_name):
                    col_id=int(key[3:])
                    col_name=re.split(r'[ \(\)]+', value)[0]
                    if header[col_id-1]==col_name:
                        logger.info('{0} [{1}] column[2]:{3},\t {4} column[2]:{5}'.format(config_name, table_name, col_id, col_name, csv_name, header[col_id - 1]))
                    else:
                        dataflag=False
                        logger.error('Error: please check column name.')
                        logger.error('{0} [{1}] column[2]:{3},\t {4} column[2]:{5}'.format(config_name, table_name, col_id, col_name, csv_name, header[col_id - 1]))
                if dataflag:
                    flag=dataflag
            else:
                logger.error('Error: please check count of column in data config is more than in csv.')
                logger.error('{0} [{1}] columns:{2},\t {3} columns:{4}'.format(config_name, table_name, col_configlen, csv_name, col_csvLen))

    except IOError as ioe:
        logger.error(r'IOError: File[{0}] cannot open.[{1}]'.format(csvFullName, ioe.args))
    except BaseException as be:
        logger.error(r'Error: {0}'.format(be.args))

if __name__=='__main__':
    pass
    # print('limit'.center(30, '*'))
    # csvlist = []
    # lookAllCsvs(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata', csvlist)
    # for i in csvlist:
    #     logger.info(i)
    # readColumn(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FormLinkModule.csv',1,['ID', 'VARCHAR', '20', 'Nullable'],'FormLinkModule.csv')
    readColumn(r'E:\ComplianceProduct\fed\src\Metadata\XVals\XVals_30245.csv',12,['StartDate','DATE', 'Nullable'],'30245',dateFormat='US')
    #readColumns(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FormLinkModule.csv',r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FED_FORM_META.ini',22202)


    # csvFilter = re.escape(r'*GridRef_*8*')
    # csvFilter = re.sub(r'\\\*', r'.*', csvFilter)
    # csvFilter = r'^{0}$'.format(csvFilter)
    # print('csvPattern:{0}'.format(csvFilter))
    # lookCsvsByFilter(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata',r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FED_FORM_META.ini', csvFilter )

    #compareColumnsCount(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FormLinkModule.csv',r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata\FED_FORM_META.ini','FormLinkModule')