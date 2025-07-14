#!/bin/bash

app_target="imdb-app/target/scala-2.12"
assembly_name="spark-imdb-app-assembly"

# Функция для чтения .env файла и установки переменных среды
load_env() {
  if [ -f ".env" ]; then
    echo "Loading .env file..."
    export $(grep -v '^#' .env | xargs)
  else
    echo "For your convenience, you can create the .env file"
  fi
}

# Функция для проверки и запроса переменной среды
check_and_prompt_variable() {
  local var_name=$1
  local var_value=${!var_name}

  if [ -z "$var_value" ]; then
    read -p "Enter $var_name: " var_value
    if [ -z "$var_value" ]; then
      echo "$var_name is not set. Exiting..."
      exit 1
    else
      export $var_name=$var_value
    fi
  fi
}

choose_mod() {
  local choice

  echo "Please enter a run mode [1, 2, or 3]:"
  echo "1) run Main"
  echo "2) run Samples"
  echo "3) run KryEx2"

  read choice
  
  case $choice in
  1)
    app_mod="Main"
    ;;
  2)
    app_mod="Samples"
    ;;
  3)
    app_mod="KryoEx2"
    ;;
  *)
    echo "Invalid option selected"
    choose_mod
    ;;
  esac
}

load_env
check_and_prompt_variable "SPARK_HOME"
check_and_prompt_variable "SPARK_MASTER"
check_and_prompt_variable "JAR_VERSION"

echo "Using:"
echo "SPARK_HOME:     $SPARK_HOME"
echo "SPARK_MASTER    $SPARK_MASTER"
echo "JAR_VERSION:    $JAR_VERSION"
echo "app_target:     ${app_target}"
echo "assembly_name:  ${assembly_name}"

function set_driver_params() {
 
  if [ -z "${SPARK_DRIVER_PORT}" ]; then
      # fake arg to make Spark happy	
      spark_driver_port="a=1"
    else
      spark_driver_port="spark.driver.port=${SPARK_DRIVER_PORT}"
  fi
  if [ -z "${SPARK_DRIVER_HOST}" ]; then
      spark_driver_host="b=1"
    else
      spark_driver_host="spark.driver.host=${SPARK_DRIVER_HOST}"
  fi

}

function run_item() {
  echo "Trying to submit the ${assembly_name}..."
  echo "==========================================================="
  $SPARK_HOME/bin/spark-submit \
    --master $SPARK_MASTER \
    --conf ${spark_driver_port} \
    --conf ${spark_driver_host} \
    --conf "spark.driver.extraJavaOptions=-Dlog4j2.configurationFile=file:log4j2.xml" \
    --conf "spark.executor.extraJavaOptions=-Dlog4j2.configurationFile=file:log4j2.xml" \
    ${app_target}/${assembly_name}-${JAR_VERSION}.jar $@
}

choose_mod
echo "Using mod:" $app_mod

set_driver_params
echo "driver params:" ${spark_driver_port} "," ${spark_driver_host}

run_item "--mod" ${app_mod}
