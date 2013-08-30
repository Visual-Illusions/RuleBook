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

    private final Item rBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1),
        pBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1),
        mBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1),
        aBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1);
    private PropertiesFile pluginCfg;
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
            if (!makeBooks()) {
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

    private final boolean makeBooks() {
        if (!new File("config/RuleBook/RuleBookPlugin.cfg").exists()) {
            pluginCfg = new PropertiesFile("config/RuleBook/RuleBookPlugin.cfg");
            pluginCfg.addHeaderLines("RuleBook Plugin configuration file");
            pluginCfg.getBoolean("useLockdownArea", true);
            pluginCfg.getString("lockdownLocation", "0,0,0");
            pluginCfg.getInt("lockdownRadius", 50);
            pluginCfg.getString("promotionGroup", "players");
            pluginCfg.getString("welcome.message", "&6Welcome to the Server. Please read the given rule book and confirm the rules before proceeding.");
            pluginCfg.save();
            getLogman().logWarning("This plugin needs to be configured before use. A new Config has be generated in config/RuleBook/RuleBookPlugin.cfg");
            return false;
        }
        else {
            pluginCfg = new PropertiesFile("config/RuleBook/RuleBookPlugin.cfg");
        }
        createBook("RuleBook", rBook);
        createBook("PlayerBook", pBook);
        createBook("ModBook", mBook);
        createBook("AdminBook", aBook);
        return true;
    }

    private final void createBook(String book, Item iBook) {
        PropertiesFile bookcfg = new PropertiesFile("config/RuleBook/" + book + ".cfg");
        if (!bookcfg.containsKey("title")) {
            bookcfg.addHeaderLines("Use \\n for new lines and &[color] for color and formating codes", "You can add more pages by setting keys as page#=Some Text  (replacing # with the next page number)");
            bookcfg.setString("title", book);
            bookcfg.setString("page0", "Front Page");
            bookcfg.setString("page1", "Example Page");
            bookcfg.setString("page2", "Formating Examples &00&11&22&33&44&55&66&77&88&99\n&AA&BB&CC&DD&EE&FF&KK&LL&MM&NN&OO");
            bookcfg.save();
        }
        int page = 0;
        while (bookcfg.containsKey("page" + page)) {
            BookHelper.addPages(iBook, bookcfg.getString("page" + page).replace("\\n", "\n").replaceAll("(?i)&([0-9A-FK-OR])", "\u00A7$1"));
            page++;
        }
        BookHelper.setTitle(iBook, bookcfg.getString("title"));
    }

    final static Group getPromotionGroup() {
        return Canary.usersAndGroups().getGroup($.pluginCfg.getString("promotionGroup"));
    }

    final static Item getRuleBook(Player player, String code, boolean generate) {
        Item newRuleBook = $.rBook.clone();
        BookHelper.setAuthor(newRuleBook, player.getName());
        if (generate) {
            BookHelper.addPages(newRuleBook, "Command:\n/rulebook confirm ".concat(code));
        }
        return newRuleBook;
    }

    final static Item getPlayerBook() {
        return $.pBook.clone();
    }

    final static Item getModBook() {
        return $.mBook.clone();
    }

    final static Item getAdminBook() {
        return $.aBook.clone();
    }

    final static String getWelcomeMessage() {
        return $.pluginCfg.getString("welcome.message").replaceAll("(?i)&([0-9A-FK-OR])", "\u00A7$1");
    }

    public final static Vector3D getLockdownLocation() {
        if ($.lockdown_location == null) {
            int[] coords = $.pluginCfg.getIntArray("lockdownLocation");
            $.lockdown_location = new Vector3D(coords[0], coords[1], coords[2]);
        }
        return $.lockdown_location;
    }

    public final static int getLockdownRadius() {
        return $.pluginCfg.getInt("lockdownRadius");
    }

    public final static boolean useLockdown() {
        return $.pluginCfg.getBoolean("useLockdownArea");
    }

    final static void removePlayerCode(Player player) {
        codes.remove(player.getName());
    }
}
