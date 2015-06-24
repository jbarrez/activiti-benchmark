cd target
mysql -u alfresco -palfresco -e "DROP SCHEMA benchmark"
mysql -u alfresco -palfresco -e "CREATE SCHEMA benchmark DEFAULT CHARACTER SET utf8 COLLATE utf8_bin"
java -Xms512M -Xmx2048M -DjdbcUrl=jdbc:mysql://localhost:3306/benchmark?characterEncoding=UTF-8 -DjdbcUsername=alfresco -DjdbcPassword=alfresco -DjdbcDriver=com.mysql.jdbc.Driver -Dhistory=none -Dconfig=spring -jar activiti-basic-benchmark.jar 500 8