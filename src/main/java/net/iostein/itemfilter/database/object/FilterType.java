package net.iostein.itemfilter.database.object;

import org.jetbrains.annotations.NotNull;

public enum FilterType {

    DISABLED("kein Filter"),
    WHITELIST("erlaubte Items"),
    BLACKLIST("verbotene Items");

    private final String displayName;

    FilterType(String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

}