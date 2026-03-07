-- 设置固定参数
local url = "/graphql"
local vid = "019cc55b-9b5e-7a40-aac0-35b2cce64b31"

-- 预先定义好 Body 字符串，避免在 request 函数中反复拼接，提高压测性能
-- 注意：GraphQL 的 query 字符串需要正确转义引号
local json_body = [[{
  "query": "query GetVenueDetail($vid: ID!) { venueById(id: $vid) { id name cityCode address description ticketTiers {id tierName price}} }",
  "variables": {
    "vid": "]] .. vid .. [["
  }
}]]

-- 设置请求头
local headers = {}
headers["Content-Type"] = "application/json"

-- 每次请求调用的函数
request = function()
    -- 使用 wrk.format 构造高效的 POST 请求
    return wrk.format("POST", url, headers, json_body)
end