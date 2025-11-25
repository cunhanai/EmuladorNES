package cpu;

import Memory.MemoryMap;

/**
 * Implementação da CPU 6502 do nes.NES
 * Inclui registradores, flags, e ciclo de execução
 */
public class Cpu {
    private int A;  // Acumulador
    private int X;  // Registrador de indice X
    private int Y;  // Registrador de indice Y
    private int SP; // Stack Pointer (0x0100 - 0x01FF)
    
    private int PC; // Program Counter
    
    // Flags de status (P register)
    private boolean C; // Carry
    private boolean Z; // Zero
    private boolean I; // Interrupt Disable
    private boolean D; // Decimal Mode
    private boolean B; // Break
    private boolean V; // Overflow
    private boolean N; // Negative
    
    // Referência ao mapa de memória
    private MemoryMap memory;
    
    // Conjunto de instruções
    private InstructionSet instructionSet;
    
    // Ciclos totais executados
    private long totalCycles;
    
    // Construtor da CPU
    public Cpu(MemoryMap memory) {
        this.memory = memory;
        this.instructionSet = new InstructionSet();
        reset();
    }
    
    // Reset da CPU
    public void reset() {
        A = 0;
        X = 0;
        Y = 0;
        SP = 0xFD;
        PC = readWord(0xFFFC); // Vetor de reset
        
        C = false;
        Z = false;
        I = true;
        D = false;
        B = false;
        V = false;
        N = false;
        
        totalCycles = 0;
    }
    
    // Busca pelo opcode e executa a instrução correspondente
    public int step() {
        int opcode = memory.readByte(PC);
        PC = (PC + 1) & 0xFFFF;
        
        Instruction instruction = instructionSet.getInstruction(opcode);
        if (instruction != null) {
            instruction.execute(this);
            int cycles = instruction.getCycles();
            totalCycles += cycles;
            return cycles;
        } else {
            System.err.printf("Opcode desconhecido: 0x%02X no endereço 0x%04X\n", opcode, PC - 1);
            return 0;
        }
    }
    
    // Lê um byte da memória
    public int readByte(int address) {
        return memory.readByte(address & 0xFFFF);
    }
    
    // Escreve um byte na memória
    public void writeByte(int address, int value) {
        memory.writeByte(address & 0xFFFF, value & 0xFF);
    }
    
    // Lê uma word (2 bytes) da memória (little-endian)
    public int readWord(int address) {
        int low = readByte(address);
        int high = readByte(address + 1);
        return (high << 8) | low;
    }
    
    // Atualiza as flags Z e N com base em um valor
    public void updateZN(int value) {
        Z = (value & 0xFF) == 0;
        N = (value & 0x80) != 0;
    }
    
    // Push de um byte na stack
    public void pushByte(int value) {
        memory.writeByte(0x0100 + SP, value & 0xFF);
        SP = (SP - 1) & 0xFF;
    }
    
    // Push de uma word na stack
    public void pushWord(int value) {
        pushByte((value >> 8) & 0xFF);
        pushByte(value & 0xFF);
    }
    
    // Pop de um byte da stack
    public int popByte() {
        SP = (SP + 1) & 0xFF;
        return memory.readByte(0x0100 + SP);
    }
    
    // Pop de uma word da stack
    public int popWord() {
        int low = popByte();
        int high = popByte();
        return (high << 8) | low;
    }
    
    // Retorna o registrador de status como um byte
    public int getStatusByte() {
        int status = 0x20; // Bit 5 sempre 1
        if (C) status |= 0x01;
        if (Z) status |= 0x02;
        if (I) status |= 0x04;
        if (D) status |= 0x08;
        if (B) status |= 0x10;
        if (V) status |= 0x40;
        if (N) status |= 0x80;
        return status;
    }
    
    // Define o registrador de status a partir de um byte
    public void setStatusByte(int status) {
        C = (status & 0x01) != 0;
        Z = (status & 0x02) != 0;
        I = (status & 0x04) != 0;
        D = (status & 0x08) != 0;
        B = (status & 0x10) != 0;
        V = (status & 0x40) != 0;
        N = (status & 0x80) != 0;
    }
    
    // Getters e Setters
    public int getA() { return A; }
    public void setA(int a) { A = a & 0xFF; }
    
    public int getX() { return X; }
    public void setX(int x) { X = x & 0xFF; }
    
    public int getY() { return Y; }
    public void setY(int y) { Y = y & 0xFF; }
    
    public int getPC() { return PC; }
    public void setPC(int pc) { PC = pc & 0xFFFF; }
    
    public int getSP() { return SP; }
    public void setSP(int sp) { SP = sp & 0xFF; }
    
    public boolean getC() { return C; }
    public void setC(boolean c) { C = c; }
    
    public boolean getZ() { return Z; }
    public void setZ(boolean z) { Z = z; }
    
    public boolean getI() { return I; }
    public void setI(boolean i) { I = i; }
    
    public boolean getD() { return D; }
    public void setD(boolean d) { D = d; }
    
    public boolean getB() { return B; }
    public void setB(boolean b) { B = b; }
    
    public boolean getV() { return V; }
    public void setV(boolean v) { V = v; }
    
    public boolean getN() { return N; }
    public void setN(boolean n) { N = n; }
    
    public long getTotalCycles() { return totalCycles; }
}