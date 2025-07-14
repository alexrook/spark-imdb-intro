FROM bitnami/spark:3.5.1
LABEL author="neoflex"
USER root
RUN  mkdir /var/lib/apt/lists /var/cache/apt/archives
RUN apt-get autoremove && \
    apt-get update && \
    apt-get upgrade
RUN install_packages curl

