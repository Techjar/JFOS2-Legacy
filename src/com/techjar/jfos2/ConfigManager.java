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
