local ticketInventoryKey = KEYS[1]
local userOrderedKey = KEYS[2]
local ticketInfoKey = KEYS[3]
local purchaseCount = tonumber(ARGV[1])
local nowEpoch = tonumber(ARGV[2])


local currentInventory = tonumber(redis.call('get', ticketInventoryKey) or '0')
if currentInventory < purchaseCount then
    return 1
end

local ticketInfo = redis.call('hmget', ticketInfoKey, 'saleStartEpoch', 'saleEndEpoch', 'purchaseLimit')
if not ticketInfo[1] or not ticketInfo[2] or not ticketInfo[3] then
    return 4
end
local saleStartEpoch = tonumber(ticketInfo[1] or '0')
local saleEndEpoch = tonumber(ticketInfo[2] or '0')
local limitValue = tonumber(ticketInfo[3] or '0')

local userOrderedCount = tonumber(redis.call('get', userOrderedKey) or '0')
if (userOrderedCount + purchaseCount) > limitValue then
    return 2
end

if nowEpoch < saleStartEpoch or nowEpoch > saleEndEpoch then
    return 3
end

redis.call('decrby', ticketInventoryKey, purchaseCount)
redis.call('incrby', userOrderedKey, purchaseCount)

return 0
