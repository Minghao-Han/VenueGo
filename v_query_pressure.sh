wrk -t8 -c100 -d60s --latency \
  -s ./venue_query_pressure.lua \
  http://192.168.49.2:30147

#   -t 线程数
#   -c 并发连接数
#   -d 压测持续时间
#   --latency 输出延迟分布
#   -s 指定 Lua 脚本路径