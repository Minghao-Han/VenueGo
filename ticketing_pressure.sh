wrk -t4 -c200 -d60s --latency \
  -s ticketing_pressure.lua \
  http://localhost:8080

#   -t 线程数
#   -c 并发连接数
#   -d 压测持续时间
#   --latency 输出延迟分布
#   -s 指定 Lua 脚本路径