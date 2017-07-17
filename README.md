Build and run under Windows (make sure to install beta Chrome >60 first).
```
gradlew build fatJar && java -jar build/libs java-chrome.jar url 1920 1460
```

Build and run docker image (java + unstable latest version of headless chrome):
```
docker build -t java-chrome .
docker run -dit -p 9092:9092 --cap-add=SYS_ADMIN --network="host" java-chrome
```
Now you can copy sources under docker image and run app using same arguments listed above.