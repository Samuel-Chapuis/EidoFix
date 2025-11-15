package fr.eidolyth.transportation;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import fr.eidolyth.EidoFix;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EidoFix.MODID)
public class ElytraRestriction {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // Ne s'applique qu'aux joueurs
        if (!(event.getEntity() instanceof Player player)) return;

        // Vérifier si des élytres sont équipées dans l'emplacement de plastron
        if (event.getSlot() == EquipmentSlot.CHEST && event.getTo().is(Items.ELYTRA)) {
            LOGGER.info("[EidoFix] Tentative d'équipement d'élytres bloquée pour {}", player.getName().getString());
            
            // Empêcher l'équipement en remettant l'ancien item
            player.setItemSlot(EquipmentSlot.CHEST, event.getFrom());
            
            // Remettre les élytres dans l'inventaire
            if (!player.getInventory().add(event.getTo().copy())) {
                // Si l'inventaire est plein, faire tomber l'item
                player.drop(event.getTo().copy(), false);
            }
            
            // Message au joueur
            player.sendSystemMessage(Component.translatable("[EidoFix] Les élytres ne peuvent pas être équipées !"));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Vérifier toutes les 20 ticks (1 seconde) si des élytres sont équipées
        if (event.getEntity().tickCount % 20 != 0) return;

        Player player = event.getEntity();
        ItemStack chestArmor = player.getInventory().getArmor(2); // Slot de plastron

        if (chestArmor.is(Items.ELYTRA)) {
            LOGGER.info("[EidoFix] Élytres détectées équipées, retrait forcé pour {}", player.getName().getString());
            
            // Retirer les élytres
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            
            // Essayer de les remettre dans l'inventaire
            if (!player.getInventory().add(chestArmor.copy())) {
                // Si l'inventaire est plein, faire tomber l'item
                player.drop(chestArmor.copy(), false);
            }
            
            // Message au joueur
            player.sendSystemMessage(Component.translatable("[EidoFix] Les élytres ont été automatiquement retirées !"));
        }
    }
}