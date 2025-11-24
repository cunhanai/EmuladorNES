package nes.memory;

public enum MapperType {
    NROM(0),
    MMC1(1),
    UXROM(2),
    CNROM(3),
    MMC3(4),
    UNKNOWN(-1);

    private final int id;

    MapperType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MapperType fromId(int mapperId) {
        for (MapperType type : values()) {
            if (type.id == mapperId) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

