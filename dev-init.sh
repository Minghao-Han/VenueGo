# 1. Create a custom network for development
NETWORK=venuego-net
BROKER_CONFIG=$(pwd)/rmq_broker.conf

docker network inspect $NETWORK >/dev/null 2>&1 || \
docker network create $NETWORK

# 2. start redis
docker run -d \
  --name redis-dev \
  --network $NETWORK \
  -p 6379:6379 \
  -v ~/redis_data:/data \
  redis:7.2-alpine \
  redis-server --appendonly yes

# 3. run mysql
docker run -d \
  --name mysql-dev \
  --network $NETWORK \
  -e MYSQL_ROOT_PASSWORD=4445566 \
  -e MYSQL_DATABASE=venuego \
  -p 3306:3306 \
  -v ~/mysql_data:/var/lib/mysql \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password


IMAGE=apache/rocketmq:5.3.2
docker pull $IMAGE

docker rm -f rmqnamesrv rmqbroker >/dev/null 2>&1 || true

echo "Starting RMQ NameServer..."

docker run -d \
  --name rmqnamesrv \
  -p 9876:9876 \
  --network $NETWORK \
  $IMAGE sh mqnamesrv

sleep 5

echo "Starting RMQ Broker..."

docker run -d \
  --name rmqbroker \
  --network $NETWORK \
  -p 10911:10911 \
  -p 10909:10909 \
  -p 10912:10912 \
  -e "NAMESRV_ADDR=rmqnamesrv:9876" \
  -v $BROKER_CONFIG:/home/rocketmq/rocketmq-5.3.2/conf/broker.conf \
  $IMAGE sh mqbroker \
  -c /home/rocketmq/rocketmq-5.3.2/conf/broker.conf

echo "RocketMQ started"
