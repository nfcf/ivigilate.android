package com.ivigilate.android.core.classes;

public class Device {
    public enum Type {
        BeaconFixed ("BF"),
        BeaconMovable ("BM"),
        DetectorFixed ("DF"),
        DetectorMovable ("DM"),
        DetectorUser ("DU");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }

    public Type type;
    public String uid;
    public String name;

    public Device() {}

    public Device(Type type, String uid, String name) {
        this.type = type;
        this.uid = uid;
        this.name = name;
    }
}
