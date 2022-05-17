---@alias NbtType string | "END" | "BYTE" | "SHORT" | "INT" | "LONG" | "FLOAT" | "DOUBLE" | "BYTE[]" | "STRING" | "LIST" | "COMPOUND" | "INT[]" | "LONG[]"

---@class NbtElement
---@field type NbtType
---@field isnumber NbtType
---@field islist NbtType
---@field isinteger NbtType
---@field iscompound NbtType
---@field isstring NbtType
---@field asnumber number
---@field asstring string
---@field asboolean boolean
---@field astable table
---@field aslist table
local NbtElement = {
    ---Converts this nbt element to a serialized snbt string
    ---@param format? string | "pretty" | "compact"
    ---@return string
    tosnbt = function (format) end,
    ---Returns a human-readable non-deserializable representation of this nbt element
    inspect = function () end
}

---@class nbt
local nbt = {
    ---@param snbt string
    ---@return NbtElement
    fromsnbt = function (snbt) end
}

return nbt