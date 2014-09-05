/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.client;

import com.techjar.jfos2.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class ResourceDownloader {
    public static final URI url = URI.create("http://server.techjargaming.com/download/list.php?dir=jfos2");
    public static final String urlPart = "http://server.techjargaming.com/download/jfos2/";
    private static volatile String status = "";
    private static volatile float progress;
    private static volatile boolean completed;
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            
        }
    };

    public static void checkAndDownload() {
        try {
            List<URI> downloads = new ArrayList<>();
            System.out.println("Retrieving download list...");
            HttpURLConnection conn = (HttpURLConnection)url.toURL().openConnection();
            //conn.setRequestMethod("GET");
            //conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                File file = new File("resources/" + parts[0].replace(urlPart, ""));
                if (file.exists()) {
                    if (!parts[1].equals(Util.getFileMD5(file))) {
                        downloads.add(URI.create(parts[0]));
                    }
                }
                else downloads.add(URI.create(parts[0]));
            }
            br.close();
            conn.disconnect();
            if (downloads.size() > 0) {
                Thread thread = new DownloadThread(downloads);
                thread.start();
            }
            else {
                completed = true;
                System.out.println("All files are up to date!");
            }
        }
        catch (Exception ex) {
            Client.crashException(ex);
        }
    }

    public static float getProgress() {
        return progress;
    }

    public static String getStatus() {
        return status;
    }

    public static boolean isCompleted() {
        return completed;
    }

    public static class DownloadThread extends Thread {
        private List<URI> urls;

        public DownloadThread(List<URI> urls) {
            this.urls = urls;
        }

        @Override
        public void run() {
            try {
                Iterator it = urls.iterator();
                for (int i = 1; it.hasNext(); i++) {
                    URI uri = (URI)it.next();
                    String path = "resources/" + uri.toString().replace(urlPart, "");
                    status = new StringBuilder("Downloading ").append(i).append(" of ").append(urls.size()).append("... ").toString();
                    progress = 0;
                    System.out.println("Downloading " + path);
                    File file = new File(path);
                    File dir = new File(path.substring(0, path.lastIndexOf('/')));
                    if (file.exists()) file.delete();
                    else dir.mkdirs();
                    file.createNewFile();
                    HttpURLConnection conn = (HttpURLConnection)uri.toURL().openConnection();
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    OutputStream out = new FileOutputStream(file);
                    byte[] bytes = new byte[1024];
                    float total = 0, size = conn.getContentLength();
                    int count;
                    while ((count = in.read(bytes, 0, bytes.length)) != -1) {
                        out.write(bytes, 0, count);
                        total += count;
                        progress = total / size;
                    }
                    in.close();
                    out.close();
                    conn.disconnect();
                }
                status = "Downloads completed!";
                completed = true;
                System.out.println("All files are up to date!");
            }
            catch (Exception ex) {
                Client.crashException(ex);
            }
        }
    }
}
