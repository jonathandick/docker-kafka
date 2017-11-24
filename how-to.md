Do the following in separate terminals:

1. create kafka broker (this container also has zookeeper):
```
docker run -d --name my-kafka -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p'` --env ADVERTISED_PORT=9092 spotify/kafka
```
2. start mysql
```
docker run -it --rm --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=debezium -e MYSQL_USER=mysqluser -e MYSQL_PASSWORD=mysqlpw debezium/example-mysql
```
3. start mysql client
```
docker run -it --rm --name mysqlterm --link mysql --rm mysql:5.7 sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'
```
4. start debezium kafka connect (note that we are using the same my-kafka container to link boy kafka and zookeeper
```
docker run -it --rm --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my_connect_configs -e OFFSET_STORAGE_TOPIC=my_connect_offsets --link my-kafka:zookeeper --link my-kafka:kafka --link mysql:mysql debezium/connect
```
5. create kafka topics linked to the mysql tables
```
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{ "name": "inventory-connector", "config": { "connector.class": "io.debezium.connector.mysql.MySqlConnector", "tasks.max": "1", "database.hostname": "mysql", "database.port": "3306", "database.user": "debezium", "database.password": "dbz", "database.server.id": "184054", "database.server.name": "dbserver1", "database.whitelist": "inventory", "database.history.kafka.bootstrap.servers": "kafka:9092", "database.history.kafka.topic": "dbhistory.inventory" } }'
```
From here, you can run your scala spark-streaming program to ingest the kafka topic of choice

Once you have this running,

Go back to your mysql-client and run a few updates:
```
UPDATE customers SET first_name='JD3' WHERE id=1004;
```
You should see the result of this end up in the spark console
