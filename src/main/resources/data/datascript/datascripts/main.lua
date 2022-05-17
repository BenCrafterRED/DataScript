--For testing purposes

---@module "api.scoreboard"
local scoreboard = require("api::scoreboard")
---@module "api.nbt"
local nbt = require("api::nbt")

local e = nbt.fromsnbt("[{a:1}, {a:2}, {a:2}]")
print(e.get("[{a:2}]"))
e.set("[{a:2}]", nbt.fromsnbt("{b:1}"))
print(e)

return function ()
    --for key, value in pairs(scoreboard.objectives.rang.players) do
    --    if value == 6 then
    --        scoreboard.objectives.rang.players[key] = nil
    --        scoreboard.objectives.rang.players[tonumber(key) + 1] = 6
    --    end
    --end
end
