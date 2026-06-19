-- GET the value of the lock
local value = redis.call('get', KEYS[1])

-- If the lock does not exist, return 0
if not value then
    return 0
end

-- If the value matches the provided value, then delete the key
if value == ARGV[1] then
    return redis.call('del', KEYS[1])
end

return 0
