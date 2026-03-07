-- 初始化随机数种子，确保每个线程生成的随机数序列不同
math.randomseed(os.time())

-- 生成随机 UUID 的辅助函数 (符合 v4 格式近似值)
local function generate_uuid()
    local template = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
    return string.gsub(template, '[xy]', function (c)
        local v = (c == 'x') and math.random(0, 0xf) or math.random(8, 0xb)
        return string.format('%x', v)
    end)
end

-- 设置固定参数
local ticket_tier_id = "e0b50233-e779-4c63-abcd-3b05a5a7ede6"
local url = "/api/v1/tickets/purchase"

-- 每一次请求都会调用这个函数
request = function()
    -- 1. 生成随机 User ID
    local user_id = generate_uuid()
    
    -- 2. 设置 Headers
    local headers = {}
    headers["X-User-Id"] = user_id
    headers["Content-Type"] = "application/json"
    
    -- 3. 设置 Body (purchaseCount 固定为 1)
    local body = string.format('{"ticketTierId": "%s", "purchaseCount": 1}', ticket_tier_id)
    
    -- 4. 组装请求
    return wrk.format("POST", url, headers, body)
end