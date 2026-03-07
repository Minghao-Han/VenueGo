# 1. 生成 2048 位的私钥 (存放在 private.pem)
openssl genrsa -out private.pem 2048

# 2. 从私钥中提取公钥 (存放在 public.pem，这是标准的 PKIX 格式)
openssl rsa -in private.pem -pubout -out public.pem

# 3. 查看公钥内容 (一会儿复制到 K8s)
cat public.pem