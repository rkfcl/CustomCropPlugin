package customcropsplugin.customcropsplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static customcropsplugin.customcropsplugin.CropManager.cropstageMap;
import static customcropsplugin.customcropsplugin.CropManager.taskMap;

public class TaskManager {
    private Plugin plugin;
    private CropManager cropManager;
    public TaskManager(Plugin plugin,CropManager cropManager){
        this.plugin = plugin;
        this.cropManager = cropManager;
    }

    public void saveTasks(File f, HashMap<Location, String> map) {

        try (FileWriter writer = new FileWriter(f)) {
            for (Map.Entry<Location, String> entry : map.entrySet()) {
                Location location = entry.getKey();
                String cropStageName = entry.getValue();
                String taskLine = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + cropStageName;
                writer.write(taskLine + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadTasks(File f, HashMap<Location, String> map, int delay) {
        taskMap.clear();
        cropstageMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    String stageInfo = parts[4];
                    String stagePrefix = extractStagePrefix(stageInfo);
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    map.put(location, stageInfo);
                    int taskId = cropManager.createGrowthTaskAsync(location, map, delay, stagePrefix);
                    taskMap.put(location, taskId);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String extractStagePrefix(String stageInfo) {
        // Assuming the stageInfo format is "corn_seed_stage_0" or similar
        int lastUnderscoreIndex = stageInfo.lastIndexOf("_");
        if (lastUnderscoreIndex >= 0) {
            return stageInfo.substring(0, lastUnderscoreIndex + 1); // Include the underscore
        } else {
            return stageInfo; // Return the original stageInfo if no underscore found
        }
    }
}
