@echo off

call ./gradlew build

copy /Y .\build\libs\app.jar .\docker\app.jar

cd docker

docker build -t sousaherbert138/rinha-kotlin:latest .

docker push sousaherbert138/rinha-kotlin:latest