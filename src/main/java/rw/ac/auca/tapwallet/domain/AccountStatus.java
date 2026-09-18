package rw.ac.auca.tapwallet.domain;

public enum AccountStatus {
    ACTIVE,
    FROZEN;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
