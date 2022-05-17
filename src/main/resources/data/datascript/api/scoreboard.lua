---@alias Slot string | "list" | "sidebar" | "belowName" | "sidebar.team.black" | "sidebar.team.dark_blue" | "sidebar.team.dark_green" | "sidebar.team.dark_aqua" | "sidebar.team.dark_red" | "sidebar.team.dark_purple" | "sidebar.team.gold" | "sidebar.team.gray" | "sidebar.team.dark_gray" | "sidebar.team.blue" | "sidebar.team.green" | "sidebar.team.aqua" | "sidebar.team.red" | "sidebar.team.light_purple" | "sidebar.team.yellow" | "sidebar.team.white"

---@alias RenderType string | "integer" | "hearts"

---@alias SimpleCriterion string | "teamkill.dark_aqua" | "killedByTeam.blue" | "killedByTeam.red" | "killedByTeam.gold" | "killedByTeam.dark_green" | "totalKillCount" | "killedByTeam.dark_aqua" | "killedByTeam.dark_purple" | "deathCount" | "air" | "teamkill.gray" | "teamkill.red" | "killedByTeam.yellow" | "teamkill.gold" | "dummy" | "teamkill.black" | "teamkill.blue" | "teamkill.dark_green" | "killedByTeam.dark_red" | "teamkill.dark_red" | "killedByTeam.black" | "killedByTeam.green" | "playerKillCount" | "teamkill.yellow" | "teamkill.dark_blue" | "level" | "teamkill.dark_purple" | "health" | "killedByTeam.dark_blue" | "trigger" | "food" | "teamkill.dark_gray" | "killedByTeam.aqua" | "killedByTeam.white" | "armor" | "teamkill.green" | "teamkill.light_purple" | "teamkill.aqua" | "teamkill.white" | "xp" | "killedByTeam.gray" | "killedByTeam.light_purple" | "killedByTeam.dark_gray"

---@alias Criterion SimpleCriterion | "minecraft.mined:minecraft.*" | "minecraft.crafted:minecraft.*" | "minecraft.used:minecraft.*" | "minecraft.broken:minecraft.*" | "minecraft.picked_up:minecraft.*" | "minecraft.dropped:minecraft.*" | "minecraft.killed:minecraft.*" | "minecraft.killed_by:minecraft.*" | "minecraft.custom:minecraft.leave_game" | "minecraft.custom:minecraft.play_time" | "minecraft.custom:minecraft.total_world_time" | "minecraft.custom:minecraft.time_since_death" | "minecraft.custom:minecraft.time_since_rest" | "minecraft.custom:minecraft.sneak_time" | "minecraft.custom:minecraft.walk_one_cm" | "minecraft.custom:minecraft.crouch_one_cm" | "minecraft.custom:minecraft.sprint_one_cm" | "minecraft.custom:minecraft.walk_on_water_one_cm" | "minecraft.custom:minecraft.fall_one_cm" | "minecraft.custom:minecraft.climb_one_cm" | "minecraft.custom:minecraft.fly_one_cm" | "minecraft.custom:minecraft.walk_under_water_one_cm" | "minecraft.custom:minecraft.minecart_one_cm" | "minecraft.custom:minecraft.boat_one_cm" | "minecraft.custom:minecraft.pig_one_cm" | "minecraft.custom:minecraft.horse_one_cm" | "minecraft.custom:minecraft.aviate_one_cm" | "minecraft.custom:minecraft.swim_one_cm" | "minecraft.custom:minecraft.strider_one_cm" | "minecraft.custom:minecraft.jump" | "minecraft.custom:minecraft.drop" | "minecraft.custom:minecraft.damage_dealt" | "minecraft.custom:minecraft.damage_dealt_absorbed" | "minecraft.custom:minecraft.damage_dealt_resisted" | "minecraft.custom:minecraft.damage_taken" | "minecraft.custom:minecraft.damage_blocked_by_shield" | "minecraft.custom:minecraft.damage_absorbed" | "minecraft.custom:minecraft.damage_resisted" | "minecraft.custom:minecraft.deaths" | "minecraft.custom:minecraft.mob_kills" | "minecraft.custom:minecraft.animals_bred" | "minecraft.custom:minecraft.player_kills" | "minecraft.custom:minecraft.fish_caught" | "minecraft.custom:minecraft.talked_to_villager" | "minecraft.custom:minecraft.traded_with_villager" | "minecraft.custom:minecraft.eat_cake_slice" | "minecraft.custom:minecraft.fill_cauldron" | "minecraft.custom:minecraft.use_cauldron" | "minecraft.custom:minecraft.clean_armor" | "minecraft.custom:minecraft.clean_banner" | "minecraft.custom:minecraft.clean_shulker_box" | "minecraft.custom:minecraft.interact_with_brewingstand" | "minecraft.custom:minecraft.interact_with_beacon" | "minecraft.custom:minecraft.inspect_dropper" | "minecraft.custom:minecraft.inspect_hopper" | "minecraft.custom:minecraft.inspect_dispenser" | "minecraft.custom:minecraft.play_noteblock" | "minecraft.custom:minecraft.tune_noteblock" | "minecraft.custom:minecraft.pot_flower" | "minecraft.custom:minecraft.trigger_trapped_chest" | "minecraft.custom:minecraft.open_enderchest" | "minecraft.custom:minecraft.enchant_item" | "minecraft.custom:minecraft.play_record" | "minecraft.custom:minecraft.interact_with_furnace" | "minecraft.custom:minecraft.interact_with_crafting_table" | "minecraft.custom:minecraft.open_chest" | "minecraft.custom:minecraft.sleep_in_bed" | "minecraft.custom:minecraft.open_shulker_box" | "minecraft.custom:minecraft.open_barrel" | "minecraft.custom:minecraft.interact_with_blast_furnace" | "minecraft.custom:minecraft.interact_with_smoker" | "minecraft.custom:minecraft.interact_with_lectern" | "minecraft.custom:minecraft.interact_with_campfire" | "minecraft.custom:minecraft.interact_with_cartography_table" | "minecraft.custom:minecraft.interact_with_loom" | "minecraft.custom:minecraft.interact_with_stonecutter" | "minecraft.custom:minecraft.bell_ring" | "minecraft.custom:minecraft.raid_trigger" | "minecraft.custom:minecraft.raid_win" | "minecraft.custom:minecraft.interact_with_anvil" | "minecraft.custom:minecraft.interact_with_grindstone" | "minecraft.custom:minecraft.target_hit" | "minecraft.custom:minecraft.interact_with_smithing_table"

---@class ScoreboardObjective
---@field _native userdata
---@field name string
---@field criterion Criterion
---@field displayName string
---@field renderType RenderType
---@field players table<string,integer>
local ScoreboardObjective = {}

---@class scoreboard
---@field _native userdata
---@field objectives table<string, ScoreboardObjective>
---@field display table<Slot, ScoreboardObjective>
local scoreboard = {
    ---@return SimpleCriterion[] criteria
    listSimpleCriteria = function () end,

    ---@return Criterion[] criteria
    listCriteria = function () end,

    ---@return Slot[] slots
    listDisplaySlots = function () end,

    ---@return RenderType[] renderTypes
    listRenderTypes = function () end,

    display = {
        ["list"] = nil,
        ["sidebar"] = nil,
        ["belowName"] = nil,
        ["sidebar.team.black"] = nil,
        ["sidebar.team.dark_blue"] = nil,
        ["sidebar.team.dark_green"] = nil,
        ["sidebar.team.dark_aqua"] = nil,
        ["sidebar.team.dark_red"] = nil,
        ["sidebar.team.dark_purple"] = nil,
        ["sidebar.team.gold"] = nil,
        ["sidebar.team.gray"] = nil,
        ["sidebar.team.dark_gray"] = nil,
        ["sidebar.team.blue"] = nil,
        ["sidebar.team.green"] = nil,
        ["sidebar.team.aqua"] = nil,
        ["sidebar.team.red"] = nil,
        ["sidebar.team.light_purple"] = nil,
        ["sidebar.team.yellow"] = nil,
        ["sidebar.team.white"] = nil
    }
}

return scoreboard