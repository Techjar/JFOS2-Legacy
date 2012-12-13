/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.server;

/**
 *
 * @author Techjar
 */
public class Server {
    public static Server server;
    private String name;

    public static void run(String[] args) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
