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
    # flag=FileUtil.lookCsvsByFilter(r'D:\Kun_Work\wk_home\ComplianceProduct\fed\src\Metadata',os.sep.join([dataPath,arg_config]), csvFilter ,arg_format)
    # FileUtil.readColumns(os.sep.join([dataPath,arg_csv]),os.sep.join([dataPath,arg_config]),arg_format)
    dic_csvs={}
    FileUtil.lookCsvsByFilter1(dataPath, csvFilter,dic_csvs)
    fileCount=len(dic_csvs.keys())
    if fileCount==0:
        logger.error(r'Error: no files found by filter[{0}] under [{1}]'.format(arg_csv,dataPath))
    else:
        failedFiles=[]
        for key in dic_csvs:
            t_start=time.time()
            logger.info(r'checking file:{0}'.format(key))
            csv_returnId = re.sub(r'(?:GridKey|GridRef|List|Ref|Sums|Vals|XVals)\_(\d+)\.csv', r'\1', dic_csvs[key].lower(),
                                  flags=re.IGNORECASE)
            flagTmp=FileUtil.readColumns(key, os.sep.join([dataPath, arg_config]), csv_returnId, arg_format)
            if flagTmp:
                logger.info(r'successfully, checked file:{0} used {1} seconds'.format(dic_csvs[key],time.time()-t_start))
            else:
                failedFiles.append(dic_csvs[key])
                logger.error(r'fail, checked file:{0} used {1} seconds'.format(dic_csvs[key],time.time() - t_start))
            logger.info(r''.center(80, "*"))
        failedCount=len(failedFiles)
        if failedCount>0:
            logger.error(r'fail, checking {0} files with {1} failed files{2}.'.format(fileCount,failedCount,failedFiles))
        else:
            flag=0
            logger.info(r'Successfully, checking {0} files without failed files.'.format(fileCount))
        logger.info('totally, used {0} seconds.'.format(time.time()-a_start))
    return flag


if __name__=='__main__':
    flag=main()
    print(flag)

