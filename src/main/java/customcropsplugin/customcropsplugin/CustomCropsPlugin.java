package customcropsplugin.customcropsplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;


import static customcropsplugin.customcropsplugin.CropManager.cropstageMap;

public final class CustomCropsPlugin extends JavaPlugin {
    private final File CropsFile = new File(getDataFolder(), "/crops.txt");
    private CropManager cropManager;
    private TaskManager taskManager;
    private WateringCanManager wateringCanManager;
    public static final int GROWTH_TASK_DELAY = 30;
    @Override
    public void onEnable() {
        // Plugin startup logic
        makeFile(CropsFile);
        cropManager = new CropManager(this);
        wateringCanManager = new WateringCanManager(this);
        taskManager = new TaskManager(this,cropManager);
        getServer().getPluginManager().registerEvents(cropManager, this);
        getServer().getPluginManager().registerEvents(wateringCanManager, this);
        taskManager.loadTasks(CropsFile,cropstageMap,GROWTH_TASK_DELAY);
        getLogger().info("CustomCropsPlugin Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        taskManager.saveTasks(CropsFile,cropstageMap);
        cropManager.cancelAllTasks();
        getLogger().info("CustomCropsPlugin Plugin Disabled.");
    }

    public void makeFile(File f) {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // 데이터 폴더 생성
        }

        if (!f.exists() || !f.isFile()) {
            try {
                f.createNewFile(); // 파일 생성
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
