@echo off

call ./gradlew build

copy /Y .\build\libs\app.jar .\docker\app.jar

cd docker

docker build -t rinha-back-kotlin .

docker-compose up -d