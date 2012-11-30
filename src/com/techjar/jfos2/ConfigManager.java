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
        if (containsYamlKey(config, name))
            return getYamlKey(config, name);
        return def;
    }
    
    public Object getProperty(String name) {
        return getProperty(name, null);
    }
    
    public void setProperty(String name, Object value) {
        putYamlKey(config, name, value);
        if (autoSave) save();
    }
    
    public void unsetProperty(String name) {
        if (containsYamlKey(config, name)) {
            removeYamlKey(config, name);
            if (autoSave) save();
        }
    }
    
    public void defaultProperty(String name, Object value) {
        if (!containsYamlKey(config, name)) {
            putYamlKey(config, name, value);
            if (autoSave) save();
        }
    }
    
    public boolean propertyExists(String name) {
        return containsYamlKey(config, name);
    }

    private Object getYamlKey(Map<String, Object> map, String key) {
        if (key.indexOf('.') == -1) {
            return map.get(key);
        }
        Map<String, Object> curmap = map;
        while (key.indexOf('.') != -1) {
            String subkey = key.substring(0, key.indexOf('.'));
            key = key.substring(key.indexOf('.') + 1);
            if (curmap.get(subkey) == null) return null;
            if (!(curmap.get(subkey) instanceof Map)) {
                throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
            }
            curmap = (Map)curmap.get(subkey);
        }
        return curmap.get(key);
    }

    private Object putYamlKey(Map<String, Object> map, String key, Object value) {
        if (key.indexOf('.') == -1) {
            return map.put(key, value);
        }
        Map<String, Object> curmap = map;
        while (key.indexOf('.') != -1) {
            String subkey = key.substring(0, key.indexOf('.'));
            key = key.substring(key.indexOf('.') + 1);
            if (curmap.get(subkey) == null) curmap.put(subkey, new HashMap<String, Object>());
            if (!(curmap.get(subkey) instanceof Map)) {
                throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
            }
            curmap = (Map)curmap.get(subkey);
        }
        return curmap.put(key, value);
    }

    private Object removeYamlKey(Map<String, Object> map, String key) {
        if (key.indexOf('.') == -1) {
            return map.remove(key);
        }
        Map<String, Object> curmap = map;
        while (key.indexOf('.') != -1) {
            String subkey = key.substring(0, key.indexOf('.'));
            key = key.substring(key.indexOf('.') + 1);
            if (curmap.get(subkey) == null) return null;
            if (!(curmap.get(subkey) instanceof Map)) {
                throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
            }
            curmap = (Map)curmap.get(subkey);
        }
        return curmap.remove(key);
    }

    private boolean containsYamlKey(Map<String, Object> map, String key) {
        if (key.indexOf('.') == -1) {
            return map.containsKey(key);
        }
        Map<String, Object> curmap = map;
        while (key.indexOf('.') != -1) {
            String subkey = key.substring(0, key.indexOf('.'));
            key = key.substring(key.indexOf('.') + 1);
            if (curmap.get(subkey) == null) return false;
            if (!(curmap.get(subkey) instanceof Map)) {
                throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
            }
            curmap = (Map)curmap.get(subkey);
        }
        return curmap.containsKey(key);
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
            yaml.dump(config, fw);
            fw.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
