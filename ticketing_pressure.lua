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
    "ticketTierId": "be3b875a-ac63-4120-8c72-2a695cf0eacc",
    "purchaseCount": 1
}
]], purchaseCount)

    return wrk.format("POST", "/api/v1/tickets/purchase", headers, body)
end
-- response = function(status, headers, body)
--     if status < 200 or status >= 400 then
--         print("status:", status)
--         print("body:", body)
--     end
-- end