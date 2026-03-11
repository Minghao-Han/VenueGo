local url = "/graphql"
local vid = "019cdabf-4f4f-72d2-822d-c2d867d62bef"

local json_body = [[{
  "query": "query GetVenueDetail($vid: ID!) { venueById(id: $vid) { id name cityCode address description posterUrl ticketTiers { id price tierName totalCapacity saleStartTime saleEndTime } } }",
  "variables": {
    "vid": "]] .. vid .. [["
  }
}]]

local headers = {}
headers["Content-Type"] = "application/json"
headers["Accept"] = "application/json"
headers["Authorization"] = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwMTljZGFhMC0zZTk0LTdlMGItYTQzYi1jNWViNWEyYmQ1NDIiLCJyb2xlcyI6WyJVU0VSIl0sImlzcyI6InZlbnVlZ28tYXV0aC1zZXJ2aWNlIiwiZXhwIjoxNzczMjMwNDY1LCJpYXQiOjE3NzMxOTQ0NjUsImp0aSI6ImNmMjg4YzEwLWE5MTYtNDJhNC1iZGI2LTdhOTFiMDcwYjY4MiIsImVtYWlsIjoiYWxpY2VAZXhhbXBsZS5jb20ifQ.qibAHN6ZZ8OzvUvWTRwkOr5KTFal7cyXsVOxjvRJu1XJ4U5E3bDlTssgSFto49Z8lL4LPaQg5JkhsLMpMKseqGMOTpOAr6klSm1F6z2ZKwRLyUhnx21-zklArzMQJK40-11Q2vX3Swbj7R7Wet6fYGlKg0GtLn3fjMnfCWWTrD5uk-cOT89eohPHtCH77-xQGw2ooj2p3l9Z-hsy4mX6JMpMselkjaNey-V4ysUAsHZI0uXwlRXEH1Lk-xBo1WyIM_qcujd1itDPg99E0M5s1rp8lFU3yf6VON7nkBL_QxdLh5SB9mCfdfvruq3U6w83y7miEk9uTP10jTpcFCUWCg"

local printed = 0

request = function()
    return wrk.format("POST", url, headers, json_body)
end

local ok_2xx = 0
local err_4xx = 0
local err_5xx = 0
local other = 0

response = function(status, headers, body)
    -- print("status:", status)
end