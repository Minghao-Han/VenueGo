docker run --rm \
    --network venuego-net \
  -v "$(pwd)/ticketing_pressure.lua:/data/ticketing_pressure.lua" \
  williamyeh/wrk \
  -t12 -c5000 -d60s --latency \
  -s /data/ticketing_pressure.lua \
  http://ticketing-service:6240

#   -t 线程数
#   -c 并发连接数
#   -d 压测持续时间
#   --latency 输出延迟分布
#   -s 指定 Lua 脚本路径