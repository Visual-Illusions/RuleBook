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

import static net.visualillusionsent.rulebook.RuleBook.getCode;
import static net.visualillusionsent.rulebook.RuleBook.getModBook;
import static net.visualillusionsent.rulebook.RuleBook.getPromotionGroup;
import static net.visualillusionsent.rulebook.RuleBook.getRuleBook;
import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;

public class RuleBookCommandHandler implements CommandListener {

    public RuleBookCommandHandler(RuleBook rulebook) throws CommandDependencyException {
        Canary.commands().registerCommands(this, rulebook, false);
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
            msgrec.notice("Rulebook is only for Players");
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
                    msgrec.message(TextFormat.ORANGE + "Relogging may be needed for changes to take effect.");
                }
                else {
                    msgrec.notice("That is the incorrect code. Please read the book and try again.");
                    msgrec.notice("Lost the book? Use /rulebook to get a new one.");
                    msgrec.notice("The book's author should be YOU as every code is different.");
                }
            }
            else {
                msgrec.notice("You have no need to re-enter a code, you already have build rights.");
            }
        }
        else {
            msgrec.notice("Rulebook is only for Players");
        }
    }

    @Command(aliases = { "modbook" },
        parent = "rulebook",
        description = "Gives the Moderators Book for review",
        toolTip = "/rulebook modbook",
        permissions = { "rulebook.mod" })
    public final void modBook(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            ((Player) msgrec).getInventory().addItem(getModBook());
            msgrec.notice("Here is your new Moderators Book");
        }
        else {
            msgrec.notice("Rulebook is only for Players");
        }
    }

    @Command(aliases = { "playerbook", "pbook" },
        parent = "rulebook",
        description = "Gives the Players Book for review",
        toolTip = "/rulebook playerbook",
        permissions = { "" })
    public final void pBook(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            ((Player) msgrec).getInventory().addItem(getModBook());
            msgrec.notice("Here is your new Players Book");
        }
        else {
            msgrec.notice("Rulebook is only for Players");
        }
    }

    @Command(aliases = { "adminbook", "abook" },
        parent = "rulebook",
        description = "Gives the Admin Book for review",
        toolTip = "/rulebook adminbook",
        permissions = { "rulebook.admin" })
    public final void aBook(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            ((Player) msgrec).getInventory().addItem(getModBook());
            msgrec.notice("Here is your new Players Book");
        }
        else {
            msgrec.notice("Rulebook is only for Players");
        }
    }
}
