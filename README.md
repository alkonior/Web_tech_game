# Многопользовательская игра "Мышиный лабиринт"




### Server

Спецификация пакетов: [здесь](packet-specs.md)

Для создания .jar файла установите maven
http://maven.apache.org/install.html

После чего примените из корневого каталога репозитория
```cmd
cd server
mvn clean install
```

```
target/Server-1.0-SNAPSHOT-jar-with-dependencies - нужный файл.
```

###JavaFx Client

Для создания .jar файла установите maven 
http://maven.apache.org/install.html

После чего примените из корневого каталога репозитория
```cmd
mvn clean install
```

```
target/JavaFX-Client-2.0-jar-with-dependencies.jar - нужный файл.
```