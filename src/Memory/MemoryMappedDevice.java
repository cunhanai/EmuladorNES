package Memory;

/**
 * Interface para dispositivos mapeados em memória
 * (ex: PPU, APU, controladores de entrada).
 */
public interface MemoryMappedDevice {
    int read(int address);
    void write(int address, int value);
}

