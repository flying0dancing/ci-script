#!/usr/bin/python
# -*- coding: UTF-8 -*-
from docs import Conf
import os,sys,time,re,logging
from checker import FileUtil
from checker import Logger
logger = logging.getLogger('checker.Main')

def main():
    a_start = time.time()
    if len(sys.argv)!=5:
        logger.error(r'data checker need 4 arguments: projectFloderName, configurationFileName,dataFileNameFilter,dateFormat.')
        return 1
    dateFormatList=['CN','GB','US']
    if str(sys.argv[4]).upper()  not in dateFormatList:
        logger.error(r'date format need use one of them.{0}'.format(dateFormatList))
        return 1
    # abc,abc*,ab*c,*abc,*
    flag=1
    argx = sys.argv[1]
    arg_config=sys.argv[2] #'FED_FORM_META.ini'
    arg_csv=sys.argv[3] #'CFG_CONFIG_DEFINED_*'['CFG_CONFIG_DEFINED_VARIABLES.csv', 'ExportFormatModule.csv', 'FormLink.csv', 'XVals_30245.csv']
    arg_format=sys.argv[4].upper()
    execParentPath = os.path.dirname(os.path.dirname(Conf.BASE_DIR))
    pardirs = re.split(r'[/\\]', execParentPath)
    pardirs.extend([argx, 'src', 'Metadata'])
    dataPath = os.sep.join(pardirs)
    logger.info(r'checking files in path:[ {0}]'.format(dataPath))
    csvFilter = re.escape(arg_csv)
    csvFilter = re.sub(r'\\\*', r'.*?', csvFilter)
    csvFilter = r'^{0}$'.format(csvFilter)
    logger.debug('csvPattern:{0}'.format(csvFilter))

    csv_count=0
    failedFiles=[]
    for csv_info in FileUtil.lookCsvsByFilter2(dataPath, csvFilter):
        t_start=time.time()
        csv_count=csv_count+1
        csvFullName=csv_info[0]
        csv_filename=csv_info[1]
        logger.info(r''.center(80, "*"))
        logger.info(r'checking file:{0}'.format(csvFullName))
        csv_returnId = re.sub(r'(?:GridKey|GridRef|List|Ref|Sums|Vals|XVals)\_(\d+)\.csv', r'\1', csv_filename,flags=re.IGNORECASE)
        flagTmp=FileUtil.readColumns(csvFullName, os.sep.join([dataPath, arg_config]), csv_returnId, arg_format)
        if flagTmp:
            logger.info(r'successfully, checked file:{0} used {1} seconds'.format(csv_filename,time.time()-t_start))
        else:
            failedFiles.append(csv_filename)
            logger.error(r'fail, checked file:{0} used {1} seconds'.format(csv_filename,time.time() - t_start))
    
    if csv_count==0:
        logger.error(r'Error: no files found by filter[{0}] under [{1}]'.format(arg_csv,dataPath))
    else:
        failedCount=len(failedFiles)
        if failedCount>0:
            logger.error(r'fail, checking {0} files with {1} failed files{2}.'.format(csv_count,failedCount,failedFiles))
        else:
            flag=0
            logger.info(r'Successfully, checking {0} files without failed files.'.format(csv_count))
    logger.info('totally, used {0} seconds.'.format(time.time()-a_start))
    return flag


if __name__=='__main__':
    flag=main()
    print(flag)

