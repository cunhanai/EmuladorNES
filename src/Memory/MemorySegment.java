package Memory;

class MemorySegment {
    private final String name;
    private final int start;
    private final int end;
    private final boolean readOnly;


    //caracteristicas das memorias
    public MemorySegment(String name, int start, int end, boolean readOnly) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.readOnly = readOnly;
    }

    //gets
    public String getName() { return name; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public boolean isReadOnly() { return readOnly; }
}
