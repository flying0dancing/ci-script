#!/usr/bin/env bash
#   Use this script to test if a given TCP host/port are available

echoErr() {
    if [[ ${QUIET} -ne 1 ]]; then
        echo "try-connect.sh: $@" 1>&2
    fi
}

usage() {
    cat << USAGE >&2
Usage:
try-connect.sh host:port [-s] [-t timeout] [-- command args]
    -h, --host=HOST        Host or IP under test
    -p, --port=PORT        TCP port under test
                           Alternatively, you specify the host and port as host:port
    -s, --strict           Only execute sub-command if the test succeeds
    -q, --quiet            Don't output any status messages
    -t, --timeout=TIMEOUT  Timeout in seconds, zero for no timeout
    -- COMMAND ARGS        Execute command with args after the test finishes
USAGE
    exit 1
}

tryConnect() {
    if [[ ${TIMEOUT} -gt 0 ]]; then
        echoErr "waiting ${TIMEOUT} seconds for ${HOST}:${PORT}"
    else
        echoErr "waiting for ${HOST}:${PORT} without a timeout"
    fi
    start_ts=$(date +%s)
    while :
    do
        (echo > /dev/tcp/${HOST}/${PORT}) >/dev/null 2>&1
        RESULT=$?
        if [[ ${RESULT} -eq 0 ]]; then
            end_ts=$(date +%s)
            echoErr "${HOST}:${PORT} is available after $((end_ts - start_ts)) seconds"
            break
        fi
        sleep 1
    done
    return ${RESULT}
}

wrappedTryConnect() {
    # In order to support SIGINT during timeout: http://unix.stackexchange.com/a/57692
    if [[ ${QUIET} -eq 1 ]]; then
        timeout ${TIMEOUT} $0 --quiet --child --host=${HOST} --port=${PORT} --timeout=${TIMEOUT} &
    else
        timeout ${TIMEOUT} $0 --child --host=${HOST} --port=${PORT} --timeout=${TIMEOUT} &
    fi
    PID=$!
    trap "kill -INT -${PID}" INT
    wait ${PID}
    RESULT=$?
    if [[ ${RESULT} -ne 0 ]]; then
        echoErr "timeout occurred after waiting ${TIMEOUT} seconds for ${HOST}:${PORT}"
    fi
    return ${RESULT}
}

# process arguments
while [[ $# -gt 0 ]]
do
    case "$1" in
        *:* )
        hostport=(${1//:/ })
        HOST=${hostport[0]}
        PORT=${hostport[1]}
        shift 1
        ;;
        --child)
        CHILD=1
        shift 1
        ;;
        -q | --quiet)
        QUIET=1
        shift 1
        ;;
        -s | --strict)
        STRICT=1
        shift 1
        ;;
        -h)
        HOST="$2"
        if [[ ${HOST} == "" ]]; then break; fi
        shift 2
        ;;
        --host=*)
        HOST="${1#*=}"
        shift 1
        ;;
        -p)
        PORT="$2"
        if [[ ${PORT} == "" ]]; then break; fi
        shift 2
        ;;
        --port=*)
        PORT="${1#*=}"
        shift 1
        ;;
        -t)
        TIMEOUT="$2"
        if [[ ${TIMEOUT} == "" ]]; then break; fi
        shift 2
        ;;
        --timeout=*)
        TIMEOUT="${1#*=}"
        shift 1
        ;;
        --)
        shift
        CLI="$@"
        break
        ;;
        --help)
        usage
        ;;
        *)
        echoErr "Unknown argument: $1"
        usage
        ;;
    esac
done

if [[ "${HOST}" == "" ]] || [[ "${PORT}" == "" ]]; then
    echoErr "Error: you need to provide a host and port to test."
    usage
fi

TIMEOUT=${TIMEOUT:-15}
STRICT=${STRICT:-0}
CHILD=${CHILD:-0}
QUIET=${QUIET:-0}

if [[ ${CHILD} -gt 0 ]]; then
    tryConnect
    RESULT=$?
    exit ${RESULT}
else
    if [[ ${TIMEOUT} -gt 0 ]]; then
        wrappedTryConnect
        RESULT=$?
    else
        tryConnect
        RESULT=$?
    fi
fi

if [[ ${CLI} != "" ]]; then
    if [[ ${RESULT} -ne 0 ]] && [[ ${STRICT} -eq 1 ]]; then
        echoErr "strict mode, refusing to execute sub-process"
        exit ${RESULT}
    fi
    exec ${CLI}
else
    exit ${RESULT}
fi