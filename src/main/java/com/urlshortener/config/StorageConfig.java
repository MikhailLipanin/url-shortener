package com.urlshortener.config;

/**
 * Конфигурация хранилища
 */
public class StorageConfig {
    private String directory;
    private String user_links_file;
    private String active_users_file;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getUser_links_file() {
        return user_links_file;
    }

    public void setUser_links_file(String user_links_file) {
        this.user_links_file = user_links_file;
    }

    public String getActive_users_file() {
        return active_users_file;
    }

    public void setActive_users_file(String active_users_file) {
        this.active_users_file = active_users_file;
    }
}
