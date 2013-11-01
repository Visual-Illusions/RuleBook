/*
 * This file is part of RuleBooks.
 *
 * Copyright © 2013 Visual Illusions Entertainment
 *
 * RuleBooks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * RuleBooks is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with RuleBooks.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
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
package net.visualillusionsent.rulebooks;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import static net.visualillusionsent.rulebooks.RuleBooks.getBook;
import static net.visualillusionsent.rulebooks.RuleBooks.getCode;
import static net.visualillusionsent.rulebooks.RuleBooks.getPromotionGroup;
import static net.visualillusionsent.rulebooks.RuleBooks.getRuleBook;

public class RuleBooksCommandHandler extends VisualIllusionsCanaryPluginInformationCommand {

    public RuleBooksCommandHandler(RuleBooks rulebook) throws CommandDependencyException {
        super(rulebook);
        Canary.commands().registerCommands(this, rulebook, false);
    }

    @Command(aliases = { "rulebooks" },
            description = "RuleBooks plugin information",
            toolTip = "/rulebooks",
            permissions = "/rulebooks")
    public final void info(MessageReceiver msgrec, String[] args) {
        this.sendInformation(msgrec);
    }


    @Command(aliases = { "rulebook" },
            description = "Gives the RuleBook for review",
            toolTip = "/rulebook",
            permissions = { "" })
    public final void rulebook(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            Player player = (Player) msgrec;
            String code = "nocode";
            if (!player.isInGroup(getPromotionGroup(), true)) {
                code = getCode(player, true); //Generates
            }
            player.getInventory().addItem(getRuleBook(player, code, !code.equals("nocode")));
            msgrec.notice("Here is your new RuleBook");
        }
        else {
            msgrec.notice("RuleBook is only for Players");
        }
    }

    @Command(aliases = { "confirm" },
            parent = "rulebook",
            description = "Confirmation command that rule book has been read.",
            toolTip = "/rulebook confirm <code>",
            min = 2,
            permissions = { "" })
    public final void confirm(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            Player player = (Player) msgrec;
            if (!player.isInGroup(getPromotionGroup(), true)) {
                if (args[1].equals(getCode(player, false))) {
                    player.setGroup(getPromotionGroup());
                    msgrec.message(TextFormat.ORANGE + "Thank you for reading and confirming the rules.");
                    msgrec.message(TextFormat.ORANGE + "You have now been granted build rights.");
                    msgrec.message(TextFormat.ORANGE + "Re-logging may be needed for changes to take effect.");
                }
                else {
                    msgrec.notice("That is the incorrect code. Please read the book and try again.");
                    msgrec.notice("Lost the book? Use /rulebooks to get a new one.");
                    msgrec.notice("The book's author should be YOU as every code is different.");
                }
            }
            else {
                msgrec.notice("You have no need to re-enter a code, you already have build rights.");
            }
        }
        else {
            msgrec.notice("RuleBook is only for Players");
        }
    }

    @Command(aliases = { "get" },
            parent = "rulebook",
            description = "Gives a book of the given name",
            toolTip = "/rulebook get <bookname>",
            permissions = { "rulebooks.get" })
    public final void getABook(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            Item book = getBook(args[1]).clone();
            if (book != null) {
                if (msgrec.hasPermission(book.getMetaTag().getString("permission"))) {
                    ((Player) msgrec).getInventory().addItem(book);
                    msgrec.notice("Here is your new Book.");
                }
            }
            else {
                msgrec.notice("That book does not exist.");
            }
        }
        else {
            msgrec.notice("RuleBook is only for Players.");
        }
    }
}
