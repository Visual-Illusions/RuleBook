/*
 * This file is part of RuleBook.
 *
 * Copyright © 2013 Visual Illusions Entertainment
 *
 * RuleBook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * RuleBook is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RuleBook.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.rulebook;

import static net.canarymod.Canary.factory;
import java.io.File;
import java.util.HashMap;
import java.util.Random;
import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.inventory.helper.BookHelper;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.plugin.Plugin;
import net.canarymod.user.Group;
import net.visualillusionsent.utils.PropertiesFile;

public class RuleBook extends Plugin {

    private final Item theRuleBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1);
    private PropertiesFile bookcfg;
    private Vector3D lockdown_location;
    private static RuleBook $;
    private static HashMap<String, String> codes = new HashMap<String, String>();
    private static final Random rnd = new Random();

    public RuleBook() {
        $ = this;
    }

    @Override
    public final boolean enable() {
        try {
            if (!generateBook()) {
                return false;
            }
            new RuleBookHookHandler(this);
            new RuleBookCommandHandler(this);
        }
        catch (Exception ex) {
            getLogman().logStacktrace("Failed to load RuleBook due to an exception...", ex);
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
        $ = null;
        codes.clear();
    }

    public static final String getCode(Player player, boolean forceNew) {
        if (!codes.containsKey(player.getName()) || forceNew) {
            codes.put(player.getName(), $.generateCode());
        }
        return codes.get(player.getName());
    }

    private final String generateCode() {
        String precode = Long.toHexString(Double.doubleToLongBits(Math.random()));
        int randomStart = rnd.nextInt(precode.length());
        return precode.substring(randomStart, randomStart + 6);
    }

    private final boolean generateBook() {
        if (!new File("config/RuleBook/RuleBook.cfg").exists()) {
            PropertiesFile thebookcfg = new PropertiesFile("config/RuleBook/RuleBook.cfg");
            thebookcfg.addHeaderLines("RuleBook configuration file", "For pages add page#= (replace # with a number, pages start @ 0)", "use \\n for new lines within pages and & for colors/formatting");
            thebookcfg.setBoolean("useLockdownArea", true);
            thebookcfg.setString("lockdownLocation", "0,0,0");
            thebookcfg.setInt("lockdownRadius", 35);
            thebookcfg.setString("promotionGroup", "players");
            thebookcfg.setString("title", "The Rule Book v1.0");
            thebookcfg.setString("page0", "Opening Page");
            thebookcfg.setString("page1", "Example Page 1");
            thebookcfg.save();
            getLogman().logWarning("This plugin needs to be configured before use. A new Config has be generated in config/RuleBook/RuleBook.cfg");
            return false;
        }
        bookcfg = new PropertiesFile("config/RuleBook/RuleBook.cfg");
        int page = 0;
        while (bookcfg.containsKey("page" + page)) {
            BookHelper.addPages(theRuleBook, bookcfg.getString("page" + page).replace("\\n", "\n").replaceAll("(?i)&([0-9A-FK-OR])", "\u00A7$1"));
            page++;
        }
        theRuleBook.getDataTag().put("title", Canary.factory().getNBTFactory().newStringTag("title", bookcfg.getString("title")));
        return true;
    }

    public static Group getPromotionGroup() {
        return Canary.usersAndGroups().getGroup($.bookcfg.getString("promotionGroup"));
    }

    public final static Item getRuleBook() {
        return $.theRuleBook.clone();
    }

    public final static Item generateBook(Player player, String code) {
        Item newRuleBook = getRuleBook();
        // I broke the BookHelper for STRING Tags, this is the work around
        newRuleBook.getDataTag().put("author", factory().getNBTFactory().newStringTag("author", player.getName()));
        //BookHelper.setAuthor(newRuleBook, player.getName());
        BookHelper.addPages(newRuleBook, "Command: /rulebook confirm ".concat(code));
        return newRuleBook;
    }

    public final static Vector3D getLockdownLocation() {
        if ($.lockdown_location == null) {
            int[] coords = $.bookcfg.getIntArray("lockdownLocation");
            $.lockdown_location = new Vector3D(coords[0], coords[1], coords[2]);
        }
        return $.lockdown_location;
    }

    public final static int getLockdownRadius() {
        return $.bookcfg.getInt("lockdownRadius");
    }

    public final static boolean useLockdown() {
        return $.bookcfg.getBoolean("useLockdownArea");
    }

    final static void removePlayerCode(Player player) {
        codes.remove(player.getName());
    }
}
