mvn clean install -Dmaven.test.skip=true
# mvn install:install-file -Dfile=target/mybatis-generator-plugin.jar -DgroupId=com.arlen -DartifactId=mybatis-generator-plugin -Dversion=1.0.0 -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=com.arlen -DartifactId=mybatis-generator-plugin -Dversion=1.0.1 -Dpackaging=jar -Dfile=target/mybatis-generator-plugin.jar -Durl=http://192.168.226.68:8081/nexus/content/repositories/thirdparty/ -DrepositoryId=thirdparty
