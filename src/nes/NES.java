package nes;

import Memory.MemoryMap;
import Memory.MemoryMappedDevice;
import apu.APU;
import cpu.Cpu;
import input.Controller;
import leitor.LeitorINES;
import ppu.PPU;
import nes.memory.Mapper;
import nes.memory.MapperType;
import nes.memory.NROM;

import java.io.IOException;

/**
 * Emulador nes.NES completo
 * Integra CPU, PPU, APU, memória e entrada
 */
public class NES {
    private Cpu cpu;
    private PPU ppu;
    private APU apu;
    private MemoryMap memory;
    private Controller controller1;
    private Controller controller2;

    private boolean running;
    private long cpuCycles;
    private int dmaCyclesPending;
    private Mapper mapper;

    /**
     * Construtor do emulador nes.NES
     */
    public NES() {
        memory = new MemoryMap();
        cpu = new Cpu(memory);
        ppu = new PPU();
        apu = new APU();
        controller1 = new Controller();
        controller2 = new Controller();

        setupMemoryHandlers();
    }

    /**
     * Configura os handlers de memória para PPU, APU e Input
     */
    private void setupMemoryHandlers() {
        // Conecta a PPU como dispositivo mapeado em memória para a faixa 0x2000-0x3FFF
        memory.setPpuHandler(new MemoryMappedDevice() {
            @Override
            public int read(int address) {
                int reg = 0x2000 + (address & 0x7);
                return ppu.readRegister(reg);
            }

            @Override
            public void write(int address, int value) {
                int reg = 0x2000 + (address & 0x7);
                ppu.writeRegister(reg, value);
            }
        });

        // Conecta APU e controles em 0x4000-0x401F
        memory.setApuHandler(new MemoryMappedDevice() {
            @Override
            public int read(int address) {
                int decoded = 0x4000 + (address & 0x1F);
                switch (decoded) {
                    case 0x4015:
                        return apu.readRegister(decoded) & 0xFF;
                    case 0x4016:
                        int v1 = controller1.read();
                        System.out.printf("READ  $%04X = %02X (controller1)%n", decoded, v1 & 0xFF);
                        return v1;
                    case 0x4017:
                        int v2 = controller2.read();
                        System.out.printf("READ  $%04X = %02X (controller2)%n", decoded, v2 & 0xFF);
                        return v2;
                    default:
                        return 0;
                }
            }

            @Override
            public void write(int address, int value) {
                int decoded = 0x4000 + (address & 0x1F);
                switch (decoded) {
                    case 0x4016:
                        System.out.printf("WRITE $%04X <= %02X (strobe)%n", decoded, value & 0xFF);
                        controller1.write(value);
                        controller2.write(value);
                        break;
                    case 0x4017:
                        System.out.printf("WRITE $%04X <= %02X%n", decoded, value & 0xFF);
                        apu.writeRegister(decoded, value);
                        break;
                    case 0x4014:
                        System.out.printf("WRITE $%04X <= %02X (OAM DMA)%n", decoded, value & 0xFF);
                        queueOamDma(value & 0xFF);
                        break;
                    default:
                        if (decoded >= 0x4000 && decoded <= 0x4013) {
                            apu.writeRegister(decoded, value);
                        } else if (decoded == 0x4015) {
                            apu.writeRegister(decoded, value);
                        }
                        break;
                }
            }
        });
    }

    private void queueOamDma(int page) {
        int baseAddr = (page & 0xFF) << 8;
        for (int i = 0; i < 256; i++) {
            int data = memory.readByte(baseAddr + i);
            ppu.writeOAMByte(i, data);
        }
        int penalty = 513 + ((cpuCycles & 0x01) == 0 ? 1 : 0);
        dmaCyclesPending += penalty;
    }

    private void stepSystemCycles(int cpuCyclesToRun) {
        for (int c = 0; c < cpuCyclesToRun; c++) {
            for (int p = 0; p < 3; p++) {
                ppu.step();
                if (ppu.shouldTriggerNMI()) {
                    handleNMI();
                }
            }
            apu.step();
        }
    }

    private void applyDmaPenaltyCycles() {
        if (dmaCyclesPending <= 0) {
            return;
        }
        stepSystemCycles(dmaCyclesPending);
        cpuCycles += dmaCyclesPending;
        dmaCyclesPending = 0;
    }

    /**
     * Carrega uma ROM nes.NES
     */
    public void loadROM(String filePath) throws IOException {
        LeitorINES leitor = new LeitorINES(filePath);
        memory.loadPRGComEspelhamento(leitor.getPrgRom());
        MapperType mapperType = MapperType.fromId(leitor.getHeader().getMapeamento());
        PPU.MirrorMode mirror = leitor.getHeader().isIgnorarEspelhamento()
            ? PPU.MirrorMode.FOUR_SCREEN
            : ("Vertical".equalsIgnoreCase(leitor.getHeader().getEspelhamento())
                ? PPU.MirrorMode.VERTICAL
                : PPU.MirrorMode.HORIZONTAL);
        switch (mapperType) {
            case NROM:
                mapper = new NROM(leitor.getPrgRom(), leitor.getChrRom(), mirror);
                break;
            default:
                throw new IOException("Mapper não suportado: " + mapperType);
        }
        mapper.connect(ppu);
        ppu.setMapper(mapper);
        memory.setMapper(mapper);
        System.out.println("ROM carregada: " + filePath + " (mapper " + mapperType + ")");
    }

    /**
     * Reseta o emulador
     */
    public void reset() {
        cpu.reset();
        ppu.reset();
        apu.reset();
        controller1.reset();
        controller2.reset();
        cpuCycles = 0;
        System.out.println("Emulador resetado");
    }

    /**
     * Executa um frame completo (~29780.5 ciclos de CPU)
     */
    public void runFrame() {
        int cyclesPerFrame = 29781; // NTSC: ~1.789773 MHz / 60 Hz
        int cyclesRun = 0;

        // Garante que o emulador esteja rodando durante a execução de um frame
        if (!running) {
            running = true;
        }

        while (cyclesRun < cyclesPerFrame && running) {
            // Executa um ciclo de CPU
            int cpuCyclesThisStep = cpu.step();
            cpuCycles += cpuCyclesThisStep;
            cyclesRun += cpuCyclesThisStep;

            stepSystemCycles(cpuCyclesThisStep);
            applyDmaPenaltyCycles();
        }
    }

    /**
     * Executa uma única instrução da CPU
     */
    public void step() {
        int cpuCyclesThisStep = cpu.step();
        cpuCycles += cpuCyclesThisStep;

        stepSystemCycles(cpuCyclesThisStep);
        applyDmaPenaltyCycles();
    }

    /**
     * Trata interrupção NMI (VBlank)
     */
    private void handleNMI() {
        // Salva PC e status na stack
        cpu.pushWord(cpu.getPC());
        cpu.pushByte(cpu.getStatusByte());

        // Salta para o handler NMI
        cpu.setI(true);
        cpu.setPC(cpu.readWord(0xFFFA));
    }

    /**
     * Inicia a execução do emulador
     */
    public void start() {
        running = true;
        apu.startAudio();
        System.out.println("Emulador iniciado");
    }

    /**
     * Para a execução do emulador
     */
    public void stop() {
        running = false;
        apu.stopAudio();
        System.out.println("Emulador parado");
    }

    /**
     * Verifica se o emulador está rodando
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Obtém a CPU
     */
    public Cpu getCpu() {
        return cpu;
    }

    /**
     * Obtém a PPU
     */
    public PPU getPpu() {
        return ppu;
    }

    /**
     * Obtém a APU
     */
    public APU getApu() {
        return apu;
    }

    /**
     * Obtém o controlador 1
     */
    public Controller getController1() {
        return controller1;
    }

    /**
     * Obtém o controlador 2
     */
    public Controller getController2() {
        return controller2;
    }

    /**
     * Obtém o framebuffer da PPU para renderização
     */
    public int[] getFramebuffer() {
        return ppu.getFramebuffer();
    }

    /**
     * Obtém o mapa de memória
     */
    public MemoryMap getMemory() {
        return memory;
    }

    /**
     * Obtém informações de debug
     */
    public String getDebugInfo() {
        return String.format("CPU Cycles: %d | Frame: %d | PC: 0x%04X | A: 0x%02X | X: 0x%02X | Y: 0x%02X",
            cpuCycles,
            ppu.getFrame(),
            cpu.getPC(),
            cpu.getA(),
            cpu.getX(),
            cpu.getY()
        );
    }
}
