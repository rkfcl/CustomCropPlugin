package customcropsplugin.customcropsplugin;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class WateringCanManager implements Listener {
    private Plugin plugin;
    public WateringCanManager(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void watering_can(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // 우클릭한 블럭이 아닌 경우 무시합니다.
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block water = event.getClickedBlock().getRelative(event.getBlockFace());
        CustomStack.getInstance("watering_can");
        // 아이템과 블럭이 null이 아닌지 확인합니다.
        if (item == null || item.getItemMeta() == null || event.getClickedBlock() == null) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (item.getType() == Material.SHEARS && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == 10) {
            if (item.getDurability() == 212 || item.getDurability() == 238) {
                ItemStack watering_can = ItemsAdder.getCustomItem("watering_can");
                player.getInventory().setItemInMainHand(watering_can);
            }
        }
        if (item.getType() == Material.SHEARS && itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == 101) {
            if (water.getType() == Material.WATER) {
                ItemStack watering_can = ItemsAdder.getCustomItem("watering_can_fill");
                player.getInventory().setItemInMainHand(watering_can);
            }
        }

    }
}
