#/usr/bin/env bash
# $ JAVA_OPTS='-Dconfig=default' Java/bin/runTest.sh
#
# Run an X11-using java class under root authority.
# 2016-06-10 -bob,mon.

# Let root open an X11 app over ssh:
FOO=`echo ${DISPLAY} | sed   's/localhost\(:[0-9]*\)\..*/\1/'`
AUTH=`xauth list | grep ${FOO}`
if [[ -n ${AUTH} ]]; then
    echo ${AUTH}
    sudo xauth add ${AUTH} ;
fi

if [[ $# == 1 ]]; then
    cd ./bin; sudo java $JAVA_OPTS -classpath .:classes:/opt/pi4j/lib/'*' ${1} ;
else
    echo ''
    echo Supply a Java class as the argument. ;
fi
