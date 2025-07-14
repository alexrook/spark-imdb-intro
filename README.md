# Project spark-intro

## Description
Это тестовый проект `agrachev@neoflex.ru`, созданный в рамках прохождения испытательного срока.
Проект включает в себя приложение для Apache Spark для расчета статистики по публичным наборам данных от IMDB.com.

Набор данных имеет следующую структуру:

![dataset plantuml](app-vol/datasets/imdb/imdbsets.png "IMDB Datasets")

## Project Goals

- [ ] Понятная структура проекта
- [ ] Описание через README.md
- [ ] Тесты
- [ ] Предусмотреть конфигурирование джобы через файл конфига application.conf 
  - [X] библиотека pureConfig и переменные окружения
  - [X] имя джобы,
  - [ ] имя файла источника данных
  - [ ] имя файла с результатами обработки
  - [X] изменение уровня логирования
- [X] Добавить дополнительное логирование, вывести в лог конфиг, с которым запускалась джоба
- [ ] Чистый код, комментарии, логи, там где нужно
- [ ] Организован сбор метрик и отображение в Grafana
- [ ] docker-compose с необходимым окружением
    - [X] spark-master
    - [X] spark-worker
    - [ ] prometheus 
    - [ ] grafana
    - [ ] db?
    - [ ] hdfs?
- [X] Обязательно использование функций join, groupBy, agg
- [X] Получить fat-jar файл джобы, для запуска на кластере с помощью spark-submit
    - [X] использовать плагин sbt-assembly

## Getting started

Для вашего удобства, перед началом работы проверьте, что в вашей системе установлены следующие инструменты

* [Docker](https://www.docker.com/)
* [sdkman](https://sdkman.io/)

### Get IMDB dataset
Перед началом работы необходимо скачать [^1] IMDB public datasets:
```shell
>cd app-vol/datasets/imdb 
>wget -ci datasets.urls
>gunzip *.tsv.gz
```

Измените права на запись для каталогов, это нужно чтобы приложение находять в контейнере докера могло туда сохранять данные.

```shell
>cd <project-root>
>chmod -R a+w app-vol/
```


[^1]: Проект разрабатывался на операционной системе Linux, и здесь и далее используются команды для нее.

### Geting Apache Spark

Если у вас не установлен Apache Spark, необходимо его [скачать](https://spark.apache.org/downloads.html) и разархивировать, к примеру в каталог `/opt/spark/`

### Add your files
Для настройки запуска приложения вы можете добавить свой собственный `.env` файл, 
к примеру

```shell
SPARK_HOME=/opt/spark/351
JAR_VERSION=0.0.1
#use this if you have a remote Spark cluster
#SPARK_DRIVER_PORT=7777
#SPARK_DRIVER_HOST=192.168.5.3
# local
#SPARK_MASTER=local[*]
#docker
SPARK_MASTER=spark://0.0.0.0:7077
```

### Run tools

Находясь в корне проекта, настройте перeменные среды и запустите докер

```shell
>sdk env install && sdk env 
>docker compose up -d
```

Соберите проект
```shell
>sbt clean assembly
```

### Test and Deploy

Запуск проекта происходит при помощи утилиты `spark-submit`, которая обернута скриптом `run.sh`

Находясь в корне проекта, запустите

```shell
>./run.sh
```

Проект имеет три режима запуска 
- Main собирает и показывает статистику по наборам данных  IMDB
- Samples - собирает samples для создания тестовых наборов данных
- UDAF - user defined functions
- UDAFTyped - user defined functions typed
- PartitionExample - эксперимент с числом партиций

### Logging

Приложение в своей работе использует настроки логирования через файл `log4j2.xml` в корне проекта.  Запуск тестов использует `log4j2-test.xml` в тестовых ресурсах проекта.

### Results

Результаты работы проекта для режимов `Main`, `UDAF`, `UDAFTyped`, 'PartitionExample' будут отображены в консоли.
Для режима `Samples` результаты будут сохранены в каталог `app-vol/datasets/imdb/samples`

## Conclusion

Проект нуждается в вашем feedback. Пожалуйста пишите его на почту `agrachev@neoflex.ru` или в комментариях [gitlab](https://neogit.neoflex.ru/agrachev/spark-intro). Вы также можете отметить Project Goals в этом документе, которые, по вашему мнению, выполнены.







