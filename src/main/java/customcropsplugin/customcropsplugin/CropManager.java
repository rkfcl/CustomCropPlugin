package customcropsplugin.customcropsplugin;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

import static customcropsplugin.customcropsplugin.CustomCropsPlugin.GROWTH_TASK_DELAY;

public class CropManager implements Listener {
    private Plugin plugin;
    public static HashMap<Location, Integer> taskMap= new HashMap<>() ;
    public static HashMap<Location, String> cropstageMap= new HashMap<>() ;
    public CropManager(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void plantCustomSeed(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // 우클릭한 블럭이 아닌 경우 무시합니다.
        }
        Block clickedBlock = event.getClickedBlock();
        CustomBlock custom = CustomBlock.getInstance("water_farmland");
        if (clickedBlock == null || !clickedBlock.getBlockData().equals(custom.getBaseBlockData())) {
            return; // 블럭이 없거나 Material.DIRT가 아닌 경우 무시합니다.
        }
        Location location = clickedBlock.getLocation().add(0, 1, 0); //클릭한 땅위에 작물 심는 좌표 설정
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return; // 아이템이 없는 경우 무시합니다.
        }
        ItemMeta itemMeta = event.getItem().getItemMeta();
        CustomStack stack = CustomStack.byItemStack(itemStack);
        if (stack == null){
            return;
        }
        String cropSeedName = stack.getNamespacedID();
        String[] seedName = cropSeedName.split(":");

        if (itemMeta != null && seedName[1].contains("seed")) { //심는 씨앗이 커스텀 씨앗인지 확인
            itemStack.setAmount(itemStack.getAmount() - 1);
            CustomBlock customBlock = CustomBlock.getInstance(seedName[1]+"_stage_0"); //커스텀 씨앗 설치하기
            if (customBlock != null) {
                customBlock.place(location);
                cropstageMap.put(location,seedName[1]+"_stage_0");
                int taskId = createGrowthTaskAsync(location,cropstageMap,GROWTH_TASK_DELAY,seedName[1]+"_stage_" );
                taskMap.put(location, taskId);
            }
        }
    }
    //커스텀 작물 부서지는 메서드
    @EventHandler
    public void handleCustomCropDestruction(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        // 아래에 있는 블록 위치 계산
        Location upLocation = location.clone().add(0, 1, 0);
        // 아래 블록이 부서지면 작물 제거
        if (taskMap.containsKey(upLocation)) {
            cancelTask(upLocation);
            CustomBlock.remove(upLocation);
        }
        if (taskMap.containsKey(location)) {
            cancelTask(location);
        }
    }
    //작물 성장 비동기 테스크 실행 메서드
    public int createGrowthTaskAsync(Location location, HashMap<Location, String> map, int delay, String growthStagesPrefix) {
        int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            String stageInfo = map.get(location);
            int stageIndex = extractStageNumber(stageInfo);
            String currentStageName = growthStagesPrefix + stageIndex;
            CustomBlock currentBlock = CustomBlock.getInstance(currentStageName);

            if (currentBlock == null) {
                cancelTask(location);
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                currentBlock.place(location);
            });

            int lastindex = stageIndex+1;
            String nestStageName = growthStagesPrefix + lastindex;
            CustomBlock nextBlock = CustomBlock.getInstance(nestStageName);

            if (nextBlock == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    cancelTask(location);
                    location.subtract(0, 1, 0);
                    CustomBlock.getInstance("farmland").place(location);
                });
            }
            map.put(location,nestStageName);

        }, delay, delay).getTaskId();

        return taskId;
    }
    private int extractStageNumber(String stageInfo) {
        // Assuming the stageInfo format is "corn_seed_stage_0" or similar
        String[] parts = stageInfo.split("_");
        return Integer.parseInt(parts[parts.length - 1]);
    }
    private void cancelTask(Location location) {
        if (taskMap.containsKey(location)) {
            int taskId = taskMap.get(location);
            Bukkit.getScheduler().cancelTask(taskId);
            taskMap.remove(location);
            cropstageMap.remove(location);
        }
    }
    public void cancelAllTasks() {
        for (int taskId : taskMap.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskMap.clear();
        cropstageMap.clear();
    }
}
