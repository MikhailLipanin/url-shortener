package com.urlshortener.config;

/**
 * Конфигурация хранилища
 */
public class StorageConfig {
    private String directory;
    private String userLinksFile;
    private String activeUsersFile;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getUserLinksFile() {
        return userLinksFile;
    }

    public void setUserLinksFile(String userLinksFile) {
        this.userLinksFile = userLinksFile;
    }

    public String getActiveUsersFile() {
        return activeUsersFile;
    }

    public void setActiveUsersFile(String activeUsersFile) {
        this.activeUsersFile = activeUsersFile;
    }
}
