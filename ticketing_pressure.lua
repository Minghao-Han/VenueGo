local tokens = {}
for line in io.lines("tokens.txt") do
    tokens[#tokens + 1] = line
end

math.randomseed(os.time())
local counter = 0

request = function()
    local token = tokens[(counter % #tokens) + 1]
    counter = counter + 1

    local purchaseCount = math.random(1, 3)

    local headers = {}
    headers["Content-Type"] = "application/json"
    headers["Authorization"] = "Bearer " .. token

    local body = string.format([[
{
    "ticketTierId": "9f155be1-5c0c-49c6-ac91-7c9514a7ea41",
    "purchaseCount": %d
}
]], purchaseCount)

    return wrk.format("POST", "/api/v1/tickets/purchase", headers, body)
end
-- local printed = 0
-- response = function(status, headers, body)
--     if (status < 200 or status >= 400 ) and printed < 10 then
--         -- print("status:", status)
--         -- print("body:", body)
--         -- print("------")
--         -- printed = printed + 1
--     end
-- end