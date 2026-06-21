local key = KEYS[1]
local value = ARGV[1]
local ttl = tonumber(ARGV[2])

return redis.call("set", key, value, "NX", "PX", ttl) and 1 or 0
