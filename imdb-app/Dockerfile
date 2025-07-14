FROM bitnami/spark:3.5.1
LABEL author="neoflex"
ENV APP_VERSION="3.5.1" \
    BITNAMI_APP_NAME="spark" \
    JAVA_HOME="/opt/bitnami/java" \
    LD_LIBRARY_PATH="/opt/bitnami/python/lib:/opt/bitnami/spark/venv/lib/python3.8/site-packages/numpy.libs:$LD_LIBRARY_PATH" \
    LIBNSS_WRAPPER_PATH="/opt/bitnami/common/lib/libnss_wrapper.so" \
    NSS_WRAPPER_GROUP="/opt/bitnami/spark/tmp/nss_group" \
    NSS_WRAPPER_PASSWD="/opt/bitnami/spark/tmp/nss_passwd" \
    PYTHONPATH="/opt/bitnami/spark/python/:$PYTHONPATH" \
    SPARK_HOME="/opt/bitnami/spark" \
    SPARK_USER="spark"
ADD target/scala-2.12/spark-imdb-app-assembly-0.0.1.jar /opt/apps/imdb-app.jar
ADD docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

