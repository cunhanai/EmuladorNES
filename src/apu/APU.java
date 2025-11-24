package apu;

// Processador de audio
public class APU {
    // Canais de áudio
    private PulseChannel pulse1;
    private PulseChannel pulse2;
    private TriangleChannel triangle;
    private NoiseChannel noise;
    private DMCChannel dmc;
    
    // Registradores
    private int frameCounter;
    private boolean frameIRQ;
    
    // Construtor do processamento de audio

    // Sistema de saída de áudio
    private final AudioOutput audioOutput;
    private int cpuCyclesSinceLastSample;
    private static final int CPU_CYCLES_PER_SAMPLE = 40; // ~44100 Hz para CPU de ~1.789 MHz

    // Low-pass filter for smoother audio (dois estágios)
    private float lastMixedSample = 0.0f;
    private float lastOutputSample = 0.0f;

    /**
     * Construtor da APU
     */
    public APU() {
        System.out.println("=== Criando APU ===");
        pulse1 = new PulseChannel();
        pulse2 = new PulseChannel();
        triangle = new TriangleChannel();
        noise = new NoiseChannel();
        dmc = new DMCChannel();
        audioOutput = new AudioOutput();
        reset();
        System.out.println("✓ APU criada");
    }

    /**
     * Inicia a saída de áudio
     */
    public void startAudio() {
        System.out.println("=== APU.startAudio() chamado ===");
        if (audioOutput != null) {
            audioOutput.start();
        } else {
            System.err.println("✗ audioOutput é null!");
        }
    }

    /**
     * Para a saída de áudio
     */
    public void stopAudio() {
        if (audioOutput != null) {
            audioOutput.stop();
        }
    }
    
    // Reset da APU
    public void reset() {
        frameCounter = 0;
        frameIRQ = false;
        cpuCyclesSinceLastSample = 0;
        lastMixedSample = 0.0f; // Reseta o filtro passa-baixas
        lastOutputSample = 0.0f; // Reseta o segundo estágio
        pulse1.reset();
        pulse2.reset();
        triangle.reset();
        noise.reset();
        dmc.reset();
    }
    
    //Adiciona ao ciclo da APU
    public void step() {
        frameCounter++;

        // Atualiza os canais
        pulse1.step();
        pulse2.step();
        triangle.step();
        noise.step();

        // Gera sample de áudio na taxa apropriada
        cpuCyclesSinceLastSample++;
        if (cpuCyclesSinceLastSample >= CPU_CYCLES_PER_SAMPLE) {
            cpuCyclesSinceLastSample -= CPU_CYCLES_PER_SAMPLE;

            if (audioOutput != null && audioOutput.isAvailable()) {
                float sample = getSample();
                audioOutput.addSample(sample);
            }
        }
    }
    
    // Escreve um registrador na APU
    public void writeRegister(int register, int value) {
        value &= 0xFF;
        
        // canal pulse 1 ($4000-$4003)
        if (register >= 0x4000 && register <= 0x4003) {
            pulse1.writeRegister(register - 0x4000, value);
        }
        // canal pulse 2 ($4004-$4007)
        else if (register >= 0x4004 && register <= 0x4007) {
            pulse2.writeRegister(register - 0x4004, value);
        }
        // canal triangle ($4008-$400B)
        else if (register >= 0x4008 && register <= 0x400B) {
            triangle.writeRegister(register - 0x4008, value);
        }
        // canal noise ($400C-$400F)
        else if (register >= 0x400C && register <= 0x400F) {
            noise.writeRegister(register - 0x400C, value);
        }
        // canal dmc ($4010-$4013)
        else if (register >= 0x4010 && register <= 0x4013) {
            dmc.writeRegister(register - 0x4010, value);
        }
        // status ($4015)
        else if (register == 0x4015) {
            pulse1.setEnabled((value & 0x01) != 0);
            pulse2.setEnabled((value & 0x02) != 0);
            triangle.setEnabled((value & 0x04) != 0);
            noise.setEnabled((value & 0x08) != 0);
            dmc.setEnabled((value & 0x10) != 0);
        }
        // contador de frame ($4017)
        else if (register == 0x4017) {
            frameCounter = 0;
        }
    }
    
    // Le registro da APU
    public int readRegister(int register) {
        if (register == 0x4015) {
            int status = 0;
            if (pulse1.isEnabled()) status |= 0x01;
            if (pulse2.isEnabled()) status |= 0x02;
            if (triangle.isEnabled()) status |= 0x04;
            if (noise.isEnabled()) status |= 0x08;
            if (dmc.isEnabled()) status |= 0x10;
            return status;
        }
        return 0;
    }
    
    // Pega uma sample de audio
    public float getSample() {
        // Obtém valores brutos dos canais (0-15)
        float p1 = pulse1.getSample();
        float p2 = pulse2.getSample();
        float tri = triangle.getSample();
        float noi = noise.getSample();

        // Fórmula de mixagem não-linear do NES (previne clipping)
        float pulseOut = 0.0f;
        if (p1 != 0 || p2 != 0) {
            pulseOut = 95.88f / ((8128.0f / (p1 + p2)) + 100.0f);
        }

        float tndOut = 0.0f;
        if (tri != 0 || noi != 0) {
            float triContrib = tri / 8227.0f;
            float noiseContrib = noi / 12241.0f;
            tndOut = 159.79f / ((1.0f / (triContrib + noiseContrib + 0.00001f)) + 100.0f);
        }

        // Combina os dois grupos
        float mixed = pulseOut + tndOut;

        // Primeiro estágio: filtro passa-baixas forte
        mixed = mixed * 0.25f + lastMixedSample * 0.75f;
        lastMixedSample = mixed;

        // Normaliza para range -1.0 a 1.0
        // O output da fórmula NES varia de ~0.0 a ~0.6
        float output = mixed * 2.2f - 0.8f;

        // Segundo estágio: filtro adicional para remover resíduos de crackling
        output = output * 0.5f + lastOutputSample * 0.5f;
        lastOutputSample = output;

        // Garante que está no range
        if (output > 1.0f) output = 1.0f;
        if (output < -1.0f) output = -1.0f;

        return output;
    }
    
    // Classes internas para os canais de áudio
    
    private static class PulseChannel {
        private boolean enabled;
        private final int[] registers = new int[4];
        private int phase;
        private int timer;
        private int timerPeriod;
        private int dutyCycle;
        private int volume;

        // Duty cycle patterns (8 steps each)
        private static final int[][] DUTY_TABLE = {
            {0, 1, 0, 0, 0, 0, 0, 0}, // 12.5%
            {0, 1, 1, 0, 0, 0, 0, 0}, // 25%
            {0, 1, 1, 1, 1, 0, 0, 0}, // 50%
            {1, 0, 0, 1, 1, 1, 1, 1}  // 25% negated
        };

        void reset() {
            enabled = false;
            phase = 0;
            timer = 0;
            timerPeriod = 0;
            dutyCycle = 0;
            volume = 0;
            for (int i = 0; i < registers.length; i++) {
                registers[i] = 0;
            }
        }

        void writeRegister(int offset, int value) {
            if (offset < registers.length) {
                registers[offset] = value;

                // Parse registers
                if (offset == 0) {
                    dutyCycle = (value >> 6) & 0x03;
                    volume = value & 0x0F;
                } else if (offset == 2) {
                    timerPeriod = (timerPeriod & 0x700) | value;
                } else if (offset == 3) {
                    timerPeriod = (timerPeriod & 0xFF) | ((value & 0x07) << 8);
                    phase = 0; // Reset phase
                }
            }
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        void step() {
            if (timer > 0) {
                timer--;
            } else {
                timer = timerPeriod;
                phase = (phase + 1) & 0x07;
            }
        }

        float getSample() {
            if (!enabled || volume == 0 || timerPeriod < 8) {
                return 0.0f;
            }

            // Retorna valor bruto (0-15) para mixagem apropriada
            int output = DUTY_TABLE[dutyCycle][phase];
            return output * volume; // 0-15
        }
    }

    private static class TriangleChannel {
        private boolean enabled;
        private final int[] registers = new int[4];
        private int phase;
        private int timer;
        private int timerPeriod;

        // Triangle wave sequence (32 steps)
        private static final int[] TRIANGLE_TABLE = {
            15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1,  0,
             0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
        };

        void reset() {
            enabled = false;
            phase = 0;
            timer = 0;
            timerPeriod = 0;
            for (int i = 0; i < registers.length; i++) {
                registers[i] = 0;
            }
        }

        void writeRegister(int offset, int value) {
            if (offset < registers.length) {
                registers[offset] = value;

                if (offset == 2) {
                    timerPeriod = (timerPeriod & 0x700) | value;
                } else if (offset == 3) {
                    timerPeriod = (timerPeriod & 0xFF) | ((value & 0x07) << 8);
                }
            }
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        void step() {
            if (timer > 0) {
                timer--;
            } else {
                timer = timerPeriod;
                phase = (phase + 1) & 0x1F;
            }
        }

        float getSample() {
            if (!enabled || timerPeriod < 2) {
                return 0.0f;
            }

            // Retorna valor bruto da sequência (0-15) para mixagem apropriada
            return TRIANGLE_TABLE[phase]; // 0-15
        }
    }

    private static class NoiseChannel {
        private boolean enabled;
        private final int[] registers = new int[4];
        private int shiftRegister = 1;
        private int timer;
        private int timerPeriod;
        private int volume;

        private static final int[] NOISE_PERIOD_TABLE = {
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
        };

        void reset() {
            enabled = false;
            shiftRegister = 1;
            timer = 0;
            timerPeriod = 0;
            volume = 0;
            for (int i = 0; i < registers.length; i++) {
                registers[i] = 0;
            }
        }

        void writeRegister(int offset, int value) {
            if (offset < registers.length) {
                registers[offset] = value;

                if (offset == 0) {
                    volume = value & 0x0F;
                } else if (offset == 2) {
                    int period = value & 0x0F;
                    timerPeriod = NOISE_PERIOD_TABLE[period];
                }
            }
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        void step() {
            if (timer > 0) {
                timer--;
            } else {
                timer = timerPeriod;
                // Linear feedback shift register
                int feedback = (shiftRegister & 1) ^ ((shiftRegister >> 1) & 1);
                shiftRegister = (shiftRegister >> 1) | (feedback << 14);
            }
        }

        float getSample() {
            if (!enabled || volume == 0) {
                return 0.0f;
            }

            // Return raw value for proper mixing
            int output = (shiftRegister & 1) == 0 ? volume : 0;
            return output;
        }
    }

    private static class DMCChannel {
        private boolean enabled;
        private final int[] registers = new int[4];

        void reset() {
            enabled = false;
            for (int i = 0; i < registers.length; i++) {
                registers[i] = 0;
            }
        }

        void writeRegister(int offset, int value) {
            if (offset < registers.length) {
                registers[offset] = value;
            }
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        float getSample() {
            // retorna silencio
            return 0.0f;
        }
    }
}

