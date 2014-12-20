/*
 * This file is part of RuleBooks.
 *
 * Copyright © 2013-2014 Visual Illusions Entertainment
 *
 * RuleBooks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.rulebooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.canarymod.Canary;
import net.canarymod.api.chat.ChatComponent;
import net.canarymod.api.chat.ClickEvent;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.factory.ChatComponentFactory;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.inventory.helper.BookHelper;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.user.Group;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;
import net.visualillusionsent.utils.PropertiesFile;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RuleBooks extends VisualIllusionsCanaryPlugin {

    private final HashMap<String, Item> books = Maps.newHashMap();
    private PropertiesFile pluginCfg;
    private Vector3D lockdown_location;
    private static RuleBooks $;
    private static final ChatComponentFactory ccf = Canary.factory().getChatComponentFactory();
    private static final HashMap<String, String> codes = Maps.newHashMap();
    private static final Random rnd = new Random();
    private static final FileFilter bookFilter = new FileFilter() {
        @Override
        public boolean accept(File pathName) {
            return pathName.getName().endsWith(".book");
        }
    };

    public RuleBooks() {
        $ = this;
    }

    @Override
    public final boolean enable() {
        try {
            if (!checkSettingsMakeBooks()) {
                return false;
            }
            new RuleBooksHookHandler(this);
            new RuleBooksCommandHandler(this);
        }
        catch (Exception ex) {
            getLogman().error("Failed to load RuleBooks due to an exception...", ex);
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
        $ = null;
        codes.clear();
    }

    public static String getCode(Player player, boolean forceNew) {
        if (!codes.containsKey(player.getName()) || forceNew) {
            codes.put(player.getName(), $.generateCode());
        }
        return codes.get(player.getName());
    }

    private String generateCode() {
        String preCode = "";
        while (preCode.length() < 16) {
            preCode = Long.toHexString(Double.doubleToLongBits(rnd.nextDouble()));
        }
        int randomStart = rnd.nextInt(8);
        return preCode.substring(randomStart, randomStart + 6);
    }

    private boolean checkSettingsMakeBooks() {
        if (!new File("config/RuleBooks/settings.cfg").exists()) {
            pluginCfg = new PropertiesFile("config/RuleBooks/settings.cfg");
            pluginCfg.addHeaderLines("RuleBooks Plugin configuration file");
            pluginCfg.getBoolean("useLockdownArea", false);
            pluginCfg.getString("lockdownLocation", getDefaultSpawn());
            pluginCfg.getInt("lockdownRadius", 150);
            pluginCfg.getString("promotionGroup", "players");
            pluginCfg.getString("welcome.message", "&6Welcome to the Server. Please read the given rule book and confirm the rules before proceeding.");
            pluginCfg.save();
            scanBooks();
            getLogman().warn("This plugin needs to be configured before use. A new Config has be generated in config/RuleBooks/settings.cfg");
            return false;
        }
        else {
            pluginCfg = new PropertiesFile("config/RuleBooks/settings.cfg");
            scanBooks();
            return true;
        }
    }

    private void scanBooks() {
        File book_dir = new File("config/RuleBooks/books/");
        if (book_dir.exists()) {
            if (book_dir.listFiles(bookFilter).length > 0) {
                for (File book : book_dir.listFiles(bookFilter)) {
                    createBook(book.getName().replace(".book", ""));
                }
            }
            else {
                createBook("RuleBook");
            }
        }
        else if (book_dir.mkdir()) {
            createBook("RuleBook");
        }
        else {
            throw new RuntimeException("Unable to create books directory");
        }
    }

    private void createBook(String book) {
        PropertiesFile bookcfg = new PropertiesFile("config/RuleBooks/books/" + book + ".book");
        if (!bookcfg.containsKey("title")) {
            bookcfg.addHeaderLines("Use \\n for new lines and &[color] for color and formatting codes", "You can add more pages by setting keys as page#=Some Text  (replacing # with the next page number)");
            bookcfg.setString("title", book);
            bookcfg.setString("permission", "", "Books other than the main RuleBook can be permission locked. All permissions are prefixed with rulebooks.book.  ie: rulebooks.book.handbook");
            bookcfg.setString("page0", "Front Page");
            bookcfg.setString("page1", "Example Page");
            bookcfg.setString("page2", "Formatting Examples &00&11&22&33&44&55&66&77&88&99\n&AA&BB&CC&DD&EE&FF\n&KK&R&LL&R&MM&R&NN&R&OO");
            bookcfg.save();
        }
        Item iBook = Canary.factory().getItemFactory().newItem(ItemType.WrittenBook, 0, 1);
        int page = 0;
        ArrayList<ChatComponent> pages = Lists.newArrayList();
        while (bookcfg.containsKey("page" + page)) {
            pages.add(ccf.newChatComponent(bookcfg.getString("page" + page).replace("\\n", "\n").replaceAll("(?i)&([0-9A-FK-OR])", "\u00A7$1")));
            page++;
        }
        BookHelper.addPages(iBook, pages.toArray(new ChatComponent[pages.size()]));
        BookHelper.setTitle(iBook, bookcfg.getString("title"));
        iBook.getMetaTag().put("permission", "rulebooks.book.".concat(bookcfg.getString("permission", "")));
        books.put(book.toLowerCase(), iBook);
    }

    static Group getPromotionGroup() {
        return Canary.usersAndGroups().getGroup($.pluginCfg.getString("promotionGroup"));
    }

    static Item getRuleBook(Player player, String code, boolean generate) {
        Item newRuleBook = $.books.get("rulebook").clone();
        BookHelper.setAuthor(newRuleBook, player.getName());
        if (generate) {
            ClickEvent cmdEvent = ccf.newClickEvent(ccf.getRunCommand(), "/rulebook confirm ".concat(code));
            ChatComponent eventComponent = ccf.newChatComponent("Click here to confirm rules.");
            eventComponent.getChatStyle().setChatClickEvent(cmdEvent);
            eventComponent.getChatStyle().setColor(ccf.colorDarkGreen());
            BookHelper.addPages(newRuleBook, eventComponent);
        }
        return newRuleBook;
    }

    static String getWelcomeMessage() {
        return $.pluginCfg.getString("welcome.message").replaceAll("(?i)&([0-9A-FK-OR])", "\u00A7$1");
    }

    public static Vector3D getLockdownLocation() {
        if ($.lockdown_location == null) {
            int[] coords = $.pluginCfg.getIntArray("lockdownLocation");
            $.lockdown_location = new Vector3D(coords[0], coords[1], coords[2]);
        }
        return $.lockdown_location;
    }

    public static int getLockdownRadius() {
        return $.pluginCfg.getInt("lockdownRadius");
    }

    public static boolean useLockdown() {
        return $.pluginCfg.getBoolean("useLockdownArea");
    }

    static void removePlayerCode(Player player) {
        codes.remove(player.getName());
    }

    static Item getBook(String book_name) {
        return $.books.get(book_name.toLowerCase());
    }

    static String getDefaultSpawn() {
        Location spawn = Canary.getServer().getDefaultWorld().getSpawnLocation();
        return String.format("%d,%d,%d", spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }
}
