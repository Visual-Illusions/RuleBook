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
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.PlayerMoveHook;
import net.canarymod.plugin.PluginListener;

import static net.visualillusionsent.rulebooks.RuleBooks.getCode;
import static net.visualillusionsent.rulebooks.RuleBooks.getLockdownLocation;
import static net.visualillusionsent.rulebooks.RuleBooks.getLockdownRadius;
import static net.visualillusionsent.rulebooks.RuleBooks.getRuleBook;
import static net.visualillusionsent.rulebooks.RuleBooks.getWelcomeMessage;

public final class RuleBooksHookHandler implements PluginListener {

    public RuleBooksHookHandler(RuleBooks ruleBook) {
        Canary.hooks().registerListener(this, ruleBook);
    }

    @HookHandler
    public final void lockdown(PlayerMoveHook hook) {
        if (RuleBooks.useLockdown() && !hook.getPlayer().isInGroup(RuleBooks.getPromotionGroup(), true)) {
            Vector3D lockdown = getLockdownLocation();
            Vector3D v3d = new Vector3D(hook.getTo().getBlockX(), hook.getTo().getBlockY(), hook.getTo().getBlockZ());
            double distance = v3d.getDistance(lockdown);
            if (distance > getLockdownRadius()) {
                if (distance > getLockdownRadius() + 1) {
                    hook.getPlayer().teleportTo(lockdown);
                }
                else {
                    hook.setCanceled();
                }
                hook.getPlayer().notice("You are not allowed to leave this area until you read and confirm the rules");
            }
        }
    }

    @HookHandler
    public final void connect(ConnectionHook hook) {
        if (hook.isFirstConnection()) {
            Player player = hook.getPlayer();
            player.message(getWelcomeMessage());
            player.getInventory().addItem(getRuleBook(player, getCode(player, true), true));
        }
    }
}
