# MikroServer
### Version 2.0 (beta)
#
MikroServer is a lightweight embeddable java web server, using [Quasar Fibers](http://docs.paralleluniverse.co/quasar/) with high concurrency and low overhead.
#### 1. Features
* __Fast File Server with Cache Support__
* __Full HTTP 1.1 Support(GET/POST)__
* __Reverse Proxy__
* __JSON Suppport with Gson Library__

#### 2. How to build
>In order to build and run, first clone project :
```sh
 $ git clone https://gitlab.com/kasra.sh13/mikroserver.git
```

>Then use gradle to build/run :
##### Build :
#
```sh
$ ./gradlew build
```
##### Run :
#
```sh
$ ./gradlew run
```

##### Make Jar
> You can also make a jar file with all dependencies, however in order to run the jar you have to set _-javaagent_ to the path of quasar-core.jar file.
```sh
$ ./gradlew fatJar
```
##### Run Jar
#
```sh
$ java -javaagent:/home/blkr/quasar-core-0.7.9.jar -jar path/to/file.jar
```
You can also disable Quasar warnings by adding another parameter after _-javaagent_ :
```sh 
... -Dco.paralleluniverse.fibers.detectRunawayFibers=false ...
```
