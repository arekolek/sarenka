
Building

1. Run:
$ mvn install:install-file -Dfile=lib/sugar-1.2.jar -DgroupId=com.orm -DartifactId=sugar -Dversion=1.2 -Dpackaging=jar

to add dependency for Sugar ORM.

2. mvn clean package android:deploy android:run
