package bencrafterred.datascript.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonParseException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import bencrafterred.datascript.LuaUtils;
import bencrafterred.datascript.Minecraft;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public class ScoreboardLib extends TwoArgFunction {
    public static final LuaValue NATIVE = valueOf("_native");

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue scoreboard = new LuaTable().setmetatable(tableOf(new LuaValue[] {
            INDEX, new TwoArgFunction() {
                @Override
                public LuaValue call(LuaValue self, LuaValue key) {
                    if (key.eq_b(NATIVE)) {
                        return CoerceJavaToLua.coerce(Minecraft.getServer().getScoreboard());
                    }
                    return self.rawget(key);
                }
            }
        }));
        scoreboard.set("listSimpleCriteria", new listSimpleCriteria());
        scoreboard.set("listCriteria", new listCriteria());
        scoreboard.set("listDisplaySlots", new listDisplaySlots());
        scoreboard.set("listRenderTypes", new listRenderTypes());
        scoreboard.set("objectives", new objectives());
        scoreboard.set("display", new display());
        env.get("package").get("loaded").set("api::scoreboard", scoreboard);
        return scoreboard;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> getScoreboardCriteria() {
        ArrayList<String> criteria = new ArrayList<>(ScoreboardCriterion.getAllSimpleCriteria());
        for (StatType statType : Registry.STAT_TYPE) {
            for (Object value : statType.getRegistry()) {
                criteria.add(Stat.getName(statType, value));
            }
        }
        return criteria;
    }

    public static ScoreboardCriterion checkScoreboardCriterion(LuaValue value, ScoreboardCriterion fallback) {
        return value.isnil()
            ? fallback
            : ScoreboardCriterion.getOrCreateStatCriterion(value.checkjstring())
                .orElseThrow(() -> new LuaError("Invalid scoreboard criterion '" + value + "'"));
    }

    public static Text checkDisplayName(LuaValue value, String fallback) {
        Text displayName = new LiteralText(fallback);
        if (!value.isnil()) {
            try {
                displayName = Text.Serializer.fromJson(value.checkjstring());
            } catch (JsonParseException e) {
                String message = e.getMessage();
                String at = message.substring(message.lastIndexOf(" at ") + 1);
                error("Invalid json text for display name (" + at + ")");
            }
        }
        return displayName;
    }

    public static RenderType checkRenderType(LuaValue value, RenderType fallback) {
        RenderType renderType = fallback;
        try {
            if (!value.isnil()) {
                renderType = RenderType.getType(value.checkjstring());
            }
        } catch (IllegalArgumentException e) {
            error("Invalid render type");
        }
        return renderType;
    }

    public static String checkObjectiveName(LuaValue value) {
        if (value.isstring()) {
            return value.checkjstring();
        } else if (value.istable()) {
            LuaValue nameValue = value.get("name");
            return nameValue.checkjstring();
        }
        return null;
    }

    static class listSimpleCriteria extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return listOf(ScoreboardCriterion.getAllSimpleCriteria().stream()
                .map(LuaString::valueOf)
                .collect(Collectors.toList())
                .toArray(new LuaString[]{}));
        }
    }

    static class listCriteria extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return listOf(getScoreboardCriteria().stream()
                .map(LuaString::valueOf)
                .collect(Collectors.toList())
                .toArray(new LuaString[]{}));
        }
    }

    static class listDisplaySlots extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return listOf(Stream.of(Scoreboard.getDisplaySlotNames())
                .map(LuaString::valueOf)
                .collect(Collectors.toList())
                .toArray(new LuaString[]{}));
        }
    }

    static class listRenderTypes extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            return listOf(Stream.of(RenderType.values())
                .map(renderType -> valueOf(renderType.getName()))
                .collect(Collectors.toList())
                .toArray(new LuaString[]{}));
        }
    }

    static class display extends LuaTable {
        public display() {
            super();
            this.setmetatable(tableOf(new LuaValue[] {
                INDEX, new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key) {
                        int slot = Scoreboard.getDisplaySlotId(key.checkjstring());
                        ScoreboardObjective objective = Minecraft.getServer().getScoreboard().getObjectiveForSlot(slot);
                        if (objective == null) {
                            return NIL;
                        }
                        return Objective.fromScoreboardObjective(objective);
                    }
                },
                NEWINDEX, new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
                        int slot = Scoreboard.getDisplaySlotId(key.checkjstring());
                        assert_(slot != -1, "Invalid display slot name '" + key + "'");
                        ScoreboardObjective objective = Minecraft.getServer().getScoreboard().getNullableObjective(checkObjectiveName(value));
                        Minecraft.getServer().getScoreboard().setObjectiveSlot(slot, objective);
                        return NIL;
                    }
                }
            }));
        }

        @Override
        public Varargs next(LuaValue key) {
            List<String> names = List.of(Scoreboard.getDisplaySlotNames());
            int index = -1;
            if (!key.isnil()) {
                index = names.indexOf(key.checkjstring());
                assert_(index != -1, "invalid key to 'next'");
            }
            index++;
            if (index >= names.size()) {
                return NIL;
            }
            String name = names.get(index);
            Varargs varargs = varargsOf(valueOf(name), this.get(name));
            return varargs;
        }
    }

    static class objectives extends LuaTable {
        public objectives() {
            super();
            this.setmetatable(tableOf(new LuaValue[] {
                INDEX, new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key) {
                        return Objective.fromName(key.checkjstring());
                    }
                },
                NEWINDEX, new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
                        String name = key.checkjstring();
                        ScoreboardObjective objective = Minecraft.getServer().getScoreboard().getNullableObjective(name);
                        if (objective == null && !value.isnil()) { // create new objective
                            value.checktable();
                            ScoreboardCriterion criterion = checkScoreboardCriterion(value.get("criterion"), ScoreboardCriterion.DUMMY);
                            Text displayName = checkDisplayName(value.get("displayName"), name);
                            RenderType renderType = checkRenderType(value.get("renderType"), criterion.getDefaultRenderType());
                            Minecraft.getServer().getScoreboard().addObjective(name, criterion, displayName, renderType);
                        } else if (objective != null && value.isnil()) { // delete objective
                            Minecraft.getServer().getScoreboard().removeObjective(objective);
                        } else if (objective != null && !value.isnil()) {
                            Text displayName = checkDisplayName(value.get("displayName"), name);
                            RenderType renderType = checkRenderType(value.get("renderType"), objective.getCriterion().getDefaultRenderType());
                            objective.setDisplayName(displayName);
                            objective.setRenderType(renderType);
                        }
                        return NIL;
                    }
                }
            }));
        }

        @Override
        public Varargs next(LuaValue key) {
            List<String> names = Minecraft.getServer().getScoreboard().getObjectives().stream().map(objective -> objective.getName()).toList();
            int index = key.isnil() ? 0 : names.indexOf(key.checkjstring()) + 1;
            if (index >= names.size()) {
                return NIL;
            }
            String name = names.get(index);
            Varargs varargs = varargsOf(valueOf(name), this.get(name));
            return varargs;
        }
    }

    static class Objective extends LuaTable {
        public static final LuaValue NAME = valueOf("name");
        public static final LuaValue CRITERION = valueOf("criterion");
        public static final LuaValue DISPLAYNAME = valueOf("displayName");
        public static final LuaValue RENDERTYPE = valueOf("renderType");
        public static final LuaValue PLAYERS = valueOf("players");
        private static final LuaTable KEYS_TABLE = LuaUtils.createKeysTable(NATIVE, NAME, CRITERION, DISPLAYNAME, RENDERTYPE, PLAYERS);

        protected Objective(String objectiveName) {
            this.setmetatable(tableOf(new LuaValue[] {
                INDEX, new TwoArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key) {
                        ScoreboardObjective objective = Minecraft.getServer().getScoreboard().getNullableObjective(objectiveName);
                        assert_(objective != null, "Scoreboard objective '" + objectiveName + "' does not exist (anymore)");
                        if (key.eq_b(PLAYERS)) {
                            return new players(objective);
                        } else if (key.eq_b(NATIVE)) {
                            return CoerceJavaToLua.coerce(objective);
                        } else if (key.eq_b(NAME)) {
                            return valueOf(objective.getName());
                        } else if (key.eq_b(CRITERION)) {
                            return valueOf(objective.getCriterion().getName());
                        } else if (key.eq_b(DISPLAYNAME)) {
                            return valueOf(objective.getDisplayName().getString());
                        } else if (key.eq_b(RENDERTYPE)) {
                            return valueOf(objective.getRenderType().getName());
                        }
                        return self.rawget(key);
                    }
                },
                NEWINDEX, new ThreeArgFunction() {
                    @Override
                    public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
                        if (key.eq_b(NATIVE) || key.eq_b(NAME) || key.eq_b(CRITERION) || key.eq_b(PLAYERS)) {
                            error("'" + key + "' is a read-only value");
                        }
                        ScoreboardObjective objective = Minecraft.getServer().getScoreboard().getNullableObjective(objectiveName);
                        assert_(objective != null, "Scoreboard objective '" + objectiveName + "' does not exist (anymore)");
                        if (key.eq_b(DISPLAYNAME)) {
                            try {
                                objective.setDisplayName(Text.Serializer.fromJson(value.checkjstring()));
                            } catch (JsonParseException e) {
                                String message = e.getMessage();
                                String at = message.substring(message.lastIndexOf(" at ") + 1);
                                error("Invalid json text for display name (" + at + ")");
                            }
                        } else if (key.eq_b(RENDERTYPE)) {
                            try {
                                System.out.println(value + " " + RenderType.getType(value.checkjstring()));
                                objective.setRenderType(RenderType.getType(value.checkjstring()));
                            } catch (IllegalArgumentException e) {
                                error("Render type must be '" + RenderType.INTEGER + "' (default when nil) or '" + RenderType.HEARTS + "'");
                            }
                        } else {
                            self.rawset(key, value);
                        }
                        return NIL;
                    }
                }
            }));
        }

        @Override
        public Varargs next(LuaValue key) {
            return varargsOf(KEYS_TABLE.next(key).arg1(), this.get(key));
        }

        public static LuaValue fromScoreboardObjective(ScoreboardObjective scoreboardObjective) {
            if (scoreboardObjective == null) {
                return NIL;
            }
            return new Objective(scoreboardObjective.getName());
        }

        public static LuaValue fromName(String name) {
            return fromScoreboardObjective(Minecraft.getServer().getScoreboard().getNullableObjective(name));
        }

        static class players extends LuaTable {
            private final ScoreboardObjective objective;

            public players(ScoreboardObjective objective) {
                super();
                this.objective = objective;
                this.setmetatable(tableOf(new LuaValue[] {
                    INDEX, new TwoArgFunction() {
                        @Override
                        public LuaValue call(LuaValue self, LuaValue key) {
                            return valueOf(Minecraft.getServer().getScoreboard().getPlayerScore(name, objective).getScore());
                        }
                    },
                    NEWINDEX, new ThreeArgFunction() {
                        @Override
                        public LuaValue call(LuaValue self, LuaValue key, LuaValue value) {
                            String name = key.checkjstring();
                            if (value.isnil()) {
                                Minecraft.getServer().getScoreboard().resetPlayerScore(name, objective);
                            } else {
                                Minecraft.getServer().getScoreboard().getPlayerScore(name, objective).setScore(value.checkint());
                            }
                            return NIL;
                        }
                    }
                }));
            }
    
            @Override
            public Varargs next(LuaValue key) {
                List<String> names = Minecraft.getServer().getScoreboard().getAllPlayerScores(objective).stream().map(score -> score.getPlayerName()).toList();
                int index = key.isnil() ? 0 : names.indexOf(key.checkjstring()) + 1;
                if (index >= names.size()) {
                    return NIL;
                }
                String name = names.get(index);
                System.out.println(names + " " + index + " " + key + " " + name);
                Varargs varargs = varargsOf(valueOf(name), this.get(name));
                return varargs;
            }
        }
    }
}
