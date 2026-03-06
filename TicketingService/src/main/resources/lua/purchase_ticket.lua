local ticketInventoryKey = KEYS[1]
local userOrderedKey = KEYS[2]
local ticketInfoKey = KEYS[3]
local purchaseCount = tonumber(ARGV[1])
local nowEpoch = tonumber(ARGV[2])

local saleStartEpoch = tonumber(redis.call('hget', ticketInfoKey, 'saleStartEpoch') or '0')
local saleEndEpoch = tonumber(redis.call('hget', ticketInfoKey, 'saleEndEpoch') or '0')
local limitValue = tonumber(redis.call('hget', ticketInfoKey, 'purchaseLimit') or '0')

if saleStartEpoch == 0 or saleEndEpoch == 0 or limitValue == 0 then
    return 4
end

if nowEpoch < saleStartEpoch or nowEpoch > saleEndEpoch then
    return 3
end

local currentInventory = tonumber(redis.call('get', ticketInventoryKey) or '0')
if currentInventory < purchaseCount then
    return 1
end

local userOrderedCount = tonumber(redis.call('get', userOrderedKey) or '0')
if (userOrderedCount + purchaseCount) > limitValue then
    return 2
end

redis.call('decrby', ticketInventoryKey, purchaseCount)
local latestOrdered = redis.call('incrby', userOrderedKey, purchaseCount)

local ttlSeconds = saleEndEpoch - nowEpoch
if ttlSeconds > 0 then
    redis.call('expire', userOrderedKey, ttlSeconds)
end

return 0
