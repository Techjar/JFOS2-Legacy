package com.techjar.jfos2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Techjar
 */
public class ConfigManager {
    public final File file;
    private final boolean autoSave;
    private final Yaml yaml;
    private Map<String, Object> config;
    
    
    public ConfigManager(File file, boolean autoSave) {
        DumperOptions dumper = new DumperOptions();
        dumper.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        this.yaml = new Yaml(dumper);
        
        this.autoSave = autoSave;
        this.file = file;
        this.config = new HashMap<String, Object>();
    }
    
    public ConfigManager(String file, boolean autoSave) {
        this(new File(file), autoSave);
    }

    public ConfigManager(File file) {
        this(file, false);
    }
    
    public ConfigManager(String file) {
        this(new File(file), false);
    }
    
    public boolean fileExists() {
        return file.exists();
    }
    
    public Object getProperty(String name, Object def) {
        if (config.containsKey(name))
            return config.get(name);
        return def;
    }
    
    public Object getProperty(String name) {
        return getProperty(name, null);
    }
    
    public void setProperty(String name, Object value) {
        config.put(name, value);
        if (autoSave) save();
    }
    
    public void unsetProperty(String name) {
        if (config.containsKey(name)) {
            config.remove(name);
            if (autoSave) save();
        }
    }
    
    public void defaultProperty(String name, Object value) {
        if (!config.containsKey(name)) {
            config.put(name, value);
            if (autoSave) save();
        }
    }
    
    public boolean propertyExists(String name) {
        return config.containsKey(name);
    }
    
    public void load() {
        try {
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            FileReader fr = new FileReader(file);
            config = (Map)yaml.load(fr);
            if (config == null) config = new HashMap<String, Object>();
            fr.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void save() {
        try {
            FileWriter fw = new FileWriter(file);
            yaml.dump(parseMap(config), fw);
            fw.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Map<String, Object> parseMap(Map<String, Object> map) {
        if (map.isEmpty()) return map;
        Map<String, Object> retmap = new HashMap<String, Object>();
        mainloop: for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.indexOf('.') == -1) {
                if (retmap.containsKey(key)) System.out.println("Map already has key '" + key + "'! Value won't be saved!!");
                else retmap.put(key, value);
                continue;
            }
            Map<String, Object> curmap = retmap;
            while (key.indexOf('.') != -1) {
                String subkey = key.substring(0, key.indexOf('.'));
                key = key.substring(key.indexOf('.') + 1);
                if (curmap.get(subkey) == null) curmap.put(subkey, new HashMap<String, Object>());
                if (!(curmap.get(subkey) instanceof Map)) {
                    System.out.println("Sub-key '" + subkey + "' is not a Map! Value won't be saved!!");
                    continue mainloop;
                }
                curmap = (Map)retmap.get(subkey);
            }
            curmap.put(key, value);
        }
        return retmap;
    }
}
