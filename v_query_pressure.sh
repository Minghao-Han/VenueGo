docker run --rm \
    --network venuego-net \
  -v "$(pwd)/venue_query_pressure.lua:/data/venue_query_pressure.lua" \
  williamyeh/wrk \
  -t12 -c5000 -d60s --latency \
  -s /data/venue_query_pressure.lua \
  http://venue-service:6236

#   -t 线程数
#   -c 并发连接数
#   -d 压测持续时间
#   --latency 输出延迟分布
#   -s 指定 Lua 脚本路径