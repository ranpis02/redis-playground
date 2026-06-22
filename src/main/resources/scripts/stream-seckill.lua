-- Stream key:
local streamKey = KEYS[1]
-- Stock key: seckill:stock:{voucherId}
local stockKey = KEYS[2]
-- Order key: seckill:order:{voucherId}:{userId}
local orderKey = KEYS[3]
-- User ID
local userId = ARGV[1]
-- Voucher ID
local voucherId = ARGV[2]
-- Order ID
local orderId = ARGV[3]

-- Check if the user has already placed an order
if redis.call("EXISTS", orderKey) == 1 then
    return 2
end

-- Check if the stock is sufficient
if tonumber(redis.call("GET", stockKey)) <= 0 then
    return 1
end

-- Decrease the stock
redis.call("DECR", stockKey)

-- Mark the user as order placed
redis.call("SET", orderKey, userId)

-- Add the order detail to the stream (* - auto  the record ID)
redis.call("XADD", streamKey, "*",
        "userId", userId,
        "voucherId", voucherId,
        "id", orderId
)

return 0

