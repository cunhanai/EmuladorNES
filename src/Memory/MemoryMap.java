package Memory;

import nes.memory.Mapper;

import java.util.ArrayList;
import java.util.List;

// Inicializa variavel de tamanho da memoria do nes, cria um vetor com cada endereço, e uma lista com blocos lógicos da memória
public class MemoryMap {
    private static final int MEMORY_SIZE = 0x10000; // 64 KB
    private final byte[] addressSpace = new byte[MEMORY_SIZE];
    private final List<MemorySegment> segments = new ArrayList<>();
    private final MonitorAcessoMemoria monitor;

    // Handlers opcionais para integração com PPU/APU/Input
    private transient MemoryMappedDevice ppuHandler;
    private transient MemoryMappedDevice apuHandler;
    private transient MemoryMappedDevice inputHandler;
    private transient Mapper mapper;

    // inicializa segmentos
    public MemoryMap() {
        this.monitor = new MonitorAcessoMemoria();
        initializeSegments();
    }

    // Permite registrar dispositivos mapeados em memória
    public void setPpuHandler(MemoryMappedDevice ppuHandler) {
        this.ppuHandler = ppuHandler;
    }

    public void setApuHandler(MemoryMappedDevice apuHandler) {
        this.apuHandler = apuHandler;
    }

    public void setInputHandler(MemoryMappedDevice inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    // define os principais blocos de memória do nes.NES.
    private void initializeSegments() {
        segments.add(new MemorySegment("Internal RAM", 0x0000, 0x07FF, false));
        segments.add(new MemorySegment("Mirrors of RAM", 0x0800, 0x1FFF, false));
        segments.add(new MemorySegment("PPU Registers", 0x2000, 0x3FFF, false));
        segments.add(new MemorySegment("APU & I/O", 0x4000, 0x401F, false));
        segments.add(new MemorySegment("Expansion ROM", 0x4020, 0x5FFF, true));
        segments.add(new MemorySegment("SRAM", 0x6000, 0x7FFF, false));
        segments.add(new MemorySegment("PRG-ROM (Bank 0)", 0x8000, 0xBFFF, true)); // read only
        segments.add(new MemorySegment("PRG-ROM (Bank 1)", 0xC000, 0xFFFF, true)); // read only
    }

    // NES suporta cartuchos de 16 e 32kb
    public void loadPRGComEspelhamento(byte[] prgRom) {
        if (mapper != null) {
            return;
        }
        // se 16kb a rom será espelhada
        if (prgRom.length == 0x4000) {
            System.arraycopy(prgRom, 0, addressSpace, 0x8000, 0x4000);
            System.arraycopy(prgRom, 0, addressSpace, 0xC000, 0x4000);
        // se 32kb a rom será carregada inteira
        } else if (prgRom.length == 0x8000) {
            System.arraycopy(prgRom, 0, addressSpace, 0x8000, 0x8000);
        } else {
            throw new IllegalArgumentException("Unsupported PRG-ROM size: " + prgRom.length);
        }
    }

    // para teste - carrega código diretamente sem proteção ROM
    public void loadTestProgram(int startAddress, byte[] program) {
        System.arraycopy(program, 0, addressSpace, startAddress, program.length);
    }

    //valida um endereço e converte para int
    public int readByte(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException("Invalid memory read: 0x" + Integer.toHexString(address));
        }

        if (address >= 0x2000 && address <= 0x3FFF && ppuHandler != null) {
            return ppuHandler.read(address);
        }

        if (address >= 0x4000 && address <= 0x401F && apuHandler != null) {
            return apuHandler.read(address);
        }

        if (address >= 0x4020 && mapper != null) {
            return mapper.cpuRead(address) & 0xFF;
        }

        int value = Byte.toUnsignedInt(addressSpace[address]);
        
        // Registra a leitura no monitor
        MemorySegment seg = getSegment(address);
        String segmentName = seg != null ? seg.getName() : "Unknown";
        monitor.recordRead(address, value, segmentName);
        
        return value;
    }

    // verifica se o endereço é válido, se for read only ignora se não escreve no array
    public void writeByte(int address, int value) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IllegalArgumentException("Invalid memory write: 0x" + Integer.toHexString(address));
        }

        if (address >= 0x2000 && address <= 0x3FFF && ppuHandler != null) {
            ppuHandler.write(address, value);
            return;
        }

        if (address >= 0x4000 && address <= 0x401F && apuHandler != null) {
            apuHandler.write(address, value);
            return;
        }

        if (address >= 0x4020 && mapper != null) {
            mapper.cpuWrite(address, value);
            return;
        }

        MemorySegment seg = getSegment(address);
        String segmentName = seg != null ? seg.getName() : "Unknown";
        
        if (seg != null && seg.isReadOnly()) {
            System.out.printf("[WARN] Write ignored (ROM) at 0x%04X in segment %s\n", address, seg.getName());
            return;
        }
        
        addressSpace[address] = (byte) (value & 0xFF);
        
        // Registra a escrita no monitor
        monitor.recordWrite(address, value & 0xFF, segmentName);
    }

    // procura em qual bloco o endereço está
    public MemorySegment getSegment(int address) {
        for (MemorySegment s : segments) {
            if (address >= s.getStart() && address <= s.getEnd()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Obtém o monitor de acessos à memória
     */
    public MonitorAcessoMemoria getMonitor() {
        return monitor;
    }
}
