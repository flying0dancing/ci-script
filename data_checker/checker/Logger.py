#!/usr/bin/python
# -*- coding: UTF-8 -*-
import logging,logging.config
import os,datetime
from docs import Conf
# logger = logging.getLogger('logger')
# logger.setLevel(level=logging.INFO)
#
# handler = logging.FileHandler(os.sep.join([conf.BASE_DIR, 'data_checker.log']))
# handler.setLevel(logging.INFO)
# formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
# handler.setFormatter(formatter)
#
# console = logging.StreamHandler()
# console.setLevel(logging.INFO)
# console.setFormatter(formatter)
#
# logger.addHandler(handler)
# logger.addHandler(console)

def loggerSettings():
    LOG_DIR = os.path.join(Conf.BASE_DIR, "logs")
    if not os.path.exists(LOG_DIR):
        os.makedirs(LOG_DIR)  # 创建路径

    LOG_FILE = datetime.datetime.now().strftime("%Y-%m-%d") + ".log"
    LOG_FULLFILE=os.sep.join([LOG_DIR, LOG_FILE])
    if os.path.exists(LOG_FULLFILE):
        pass
        #os.remove(LOG_FULLFILE)
    LOGGING = {
        "version": 1,
        "disable_existing_loggers": False,
        "formatters": {
            "simple": {
                'format': '%(asctime)s [%(name)s:%(lineno)d] [%(levelname)s]- %(message)s'
            },
            'standard': {
                'format': '%(asctime)s [%(threadName)s:%(thread)d] [%(name)s:%(lineno)d] [%(levelname)s]- %(message)s'
            },
        },

        "handlers": {
            "console": {
                "class": "logging.StreamHandler",
                "level": "DEBUG",
                "formatter": "simple",
                "stream": "ext://sys.stdout"
            },

            "default": {
                "class": "logging.handlers.RotatingFileHandler",
                "level": "INFO",
                "formatter": "simple",
                "filename": LOG_FULLFILE,
                'mode': 'w+',
                "maxBytes": 1024*1024*5,  # 5 MB
                "backupCount": 20,
                "encoding": "utf8"
            },
        },

        # "loggers": {
        #     "app_name": {
        #         "level": "INFO",
        #         "handlers": ["console"],
        #         "propagate": "no"
        #     }
        # },

        "root": {
            'handlers': ['default','console'],
            'level': "INFO",
            'propagate': False
        }
    }

    logging.config.dictConfig(LOGGING)

def do_something():
    log = logging.getLogger('a')
    print("print A")
    log.info("log B")

loggerSettings()

if __name__=='__main__':
    do_something()

