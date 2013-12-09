#! /bin/sh
java -cp config:classes:lib/* -Xmx1024m -Dlog4j.configuration=log4j.properties \
    -Djppf.config=jppf.properties -Djava.util.logging.config.file=config/logging.properties\
    org.jppf.application.findfactor.FindFactorRunner $1 $2
