# 1. Create a custom network for development
docker network create venuego-net || true

# 2. start redis
docker run -d \
  --name redis-dev \
  --network venuego-net \
  -p 6379:6379 \
  -v ~/redis_data:/data \
  redis:7.2-alpine \
  redis-server --appendonly yes

# 3. run mysql
docker run -d \
  --name mysql-dev \
  --network venuego-net \
  -e MYSQL_ROOT_PASSWORD=4445566 \
  -e MYSQL_DATABASE=venuego \
  -p 3306:3306 \
  -v ~/mysql_data:/var/lib/mysql \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password
