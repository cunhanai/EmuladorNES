package cpu;

import java.util.HashMap;
import java.util.Map;

/**
 * Define o conjunto de instruções conhecidas da CPU 6502.
 * Contém o mapeamento entre opcode e a instrução correspondente.
 */
public class InstructionSet {
    private final Map<Integer, Instruction> instructionMap = new HashMap<>();

    public InstructionSet() {
        registerInstructions();
    }

    // Registro das instruções da CPU 6502
    private void registerInstructions() {
        // LDA - Load Accumulator
        registerLDA();
        // LDX - Load X Register
        registerLDX();
        // LDY - Load Y Register
        registerLDY();
        // STA - Store Accumulator
        registerSTA();
        // STX - Store X Register
        registerSTX();
        // STY - Store Y Register
        registerSTY();
        // TAX, TAY, TXA, TYA - Transfer registers
        registerTransfers();
        // INX, INY, DEX, DEY - Increment/Decrement
        registerIncDec();
        // ADC - Add with Carry
        registerADC();
        // SBC - Subtract with Carry
        registerSBC();
        // AND, ORA, EOR - Logical operations
        registerLogical();
        // CMP, CPX, CPY - Compare
        registerCompare();
        // BIT - Bit Test
        registerBIT();
        // Branches
        registerBranches();
        // JMP, JSR, RTS - Jumps and subroutines
        registerJumps();
        // Stack operations
        registerStack();
        // Flag operations
        registerFlags();
        // NOP
        registerNOP();
        // BRK, RTI
        registerInterrupts();
        // INC, DEC - Memory increment/decrement
        registerMemIncDec();
        // Shifts and Rotates
        registerShifts();
    }

    private void registerLDA() {
        // LDA Immediate
        instructionMap.put(0xA9, createInstruction("LDA", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA Zero Page
        instructionMap.put(0xA5, createInstruction("LDA", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA Zero Page,X
        instructionMap.put(0xB5, createInstruction("LDA", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA Absolute
        instructionMap.put(0xAD, createInstruction("LDA", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA Absolute,X
        instructionMap.put(0xBD, createInstruction("LDA", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA Absolute,Y
        instructionMap.put(0xB9, createInstruction("LDA", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA (Indirect,X)
        instructionMap.put(0xA1, createInstruction("LDA", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LDA (Indirect),Y
        instructionMap.put(0xB1, createInstruction("LDA", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
    }

    private void registerLDX() {
        // LDX Immediate
        instructionMap.put(0xA2, createInstruction("LDX", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setX(value);
            cpu.updateZN(value);
        }));
        
        // LDX Zero Page
        instructionMap.put(0xA6, createInstruction("LDX", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setX(value);
            cpu.updateZN(value);
        }));
        
        // LDX Zero Page,Y
        instructionMap.put(0xB6, createInstruction("LDX", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getY()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setX(value);
            cpu.updateZN(value);
        }));
        
        // LDX Absolute
        instructionMap.put(0xAE, createInstruction("LDX", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setX(value);
            cpu.updateZN(value);
        }));
        
        // LDX Absolute,Y
        instructionMap.put(0xBE, createInstruction("LDX", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setX(value);
            cpu.updateZN(value);
        }));
    }

    private void registerLDY() {
        // LDY Immediate
        instructionMap.put(0xA0, createInstruction("LDY", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setY(value);
            cpu.updateZN(value);
        }));
        
        // LDY Zero Page
        instructionMap.put(0xA4, createInstruction("LDY", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setY(value);
            cpu.updateZN(value);
        }));
        
        // LDY Zero Page,X
        instructionMap.put(0xB4, createInstruction("LDY", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setY(value);
            cpu.updateZN(value);
        }));
        
        // LDY Absolute
        instructionMap.put(0xAC, createInstruction("LDY", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setY(value);
            cpu.updateZN(value);
        }));
        
        // LDY Absolute,X
        instructionMap.put(0xBC, createInstruction("LDY", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setY(value);
            cpu.updateZN(value);
        }));
    }

    private void registerSTA() {
        // STA Zero Page
        instructionMap.put(0x85, createInstruction("STA", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA Zero Page,X
        instructionMap.put(0x95, createInstruction("STA", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA Absolute
        instructionMap.put(0x8D, createInstruction("STA", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA Absolute,X
        instructionMap.put(0x9D, createInstruction("STA", 5, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA Absolute,Y
        instructionMap.put(0x99, createInstruction("STA", 5, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA (Indirect,X)
        instructionMap.put(0x81, createInstruction("STA", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            cpu.writeByte(addr, cpu.getA());
        }));
        
        // STA (Indirect),Y
        instructionMap.put(0x91, createInstruction("STA", 6, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            cpu.writeByte(addr, cpu.getA());
        }));
    }

    private void registerSTX() {
        // STX Zero Page
        instructionMap.put(0x86, createInstruction("STX", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getX());
        }));
        
        // STX Zero Page,Y
        instructionMap.put(0x96, createInstruction("STX", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getY()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getX());
        }));
        
        // STX Absolute
        instructionMap.put(0x8E, createInstruction("STX", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            cpu.writeByte(addr, cpu.getX());
        }));
    }

    private void registerSTY() {
        // STY Zero Page
        instructionMap.put(0x84, createInstruction("STY", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getY());
        }));
        
        // STY Zero Page,X
        instructionMap.put(0x94, createInstruction("STY", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            cpu.writeByte(addr, cpu.getY());
        }));
        
        // STY Absolute
        instructionMap.put(0x8C, createInstruction("STY", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            cpu.writeByte(addr, cpu.getY());
        }));
    }

    private void registerTransfers() {
        // TAX - Transfer A to X
        instructionMap.put(0xAA, createInstruction("TAX", 2, cpu -> {
            cpu.setX(cpu.getA());
            cpu.updateZN(cpu.getX());
        }));
        
        // TAY - Transfer A to Y
        instructionMap.put(0xA8, createInstruction("TAY", 2, cpu -> {
            cpu.setY(cpu.getA());
            cpu.updateZN(cpu.getY());
        }));
        
        // TXA - Transfer X to A
        instructionMap.put(0x8A, createInstruction("TXA", 2, cpu -> {
            cpu.setA(cpu.getX());
            cpu.updateZN(cpu.getA());
        }));
        
        // TYA - Transfer Y to A
        instructionMap.put(0x98, createInstruction("TYA", 2, cpu -> {
            cpu.setA(cpu.getY());
            cpu.updateZN(cpu.getA());
        }));
        
        // TSX - Transfer SP to X
        instructionMap.put(0xBA, createInstruction("TSX", 2, cpu -> {
            cpu.setX(cpu.getSP());
            cpu.updateZN(cpu.getX());
        }));
        
        // TXS - Transfer X to SP
        instructionMap.put(0x9A, createInstruction("TXS", 2, cpu -> {
            cpu.setSP(cpu.getX());
        }));
    }

    private void registerIncDec() {
        // INX
        instructionMap.put(0xE8, createInstruction("INX", 2, cpu -> {
            cpu.setX((cpu.getX() + 1) & 0xFF);
            cpu.updateZN(cpu.getX());
        }));
        
        // INY
        instructionMap.put(0xC8, createInstruction("INY", 2, cpu -> {
            cpu.setY((cpu.getY() + 1) & 0xFF);
            cpu.updateZN(cpu.getY());
        }));
        
        // DEX
        instructionMap.put(0xCA, createInstruction("DEX", 2, cpu -> {
            cpu.setX((cpu.getX() - 1) & 0xFF);
            cpu.updateZN(cpu.getX());
        }));
        
        // DEY
        instructionMap.put(0x88, createInstruction("DEY", 2, cpu -> {
            cpu.setY((cpu.getY() - 1) & 0xFF);
            cpu.updateZN(cpu.getY());
        }));
    }

    private void registerADC() {
        // ADC Immediate
        instructionMap.put(0x69, createInstruction("ADC", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            adc(cpu, value);
        }));
        
        // ADC Zero Page
        instructionMap.put(0x65, createInstruction("ADC", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC Zero Page,X
        instructionMap.put(0x75, createInstruction("ADC", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC Absolute
        instructionMap.put(0x6D, createInstruction("ADC", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC Absolute,X
        instructionMap.put(0x7D, createInstruction("ADC", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC Absolute,Y
        instructionMap.put(0x79, createInstruction("ADC", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC (Indirect,X)
        instructionMap.put(0x61, createInstruction("ADC", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
        
        // ADC (Indirect),Y
        instructionMap.put(0x71, createInstruction("ADC", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            adc(cpu, value);
        }));
    }

    private void registerSBC() {
        // SBC Immediate
        instructionMap.put(0xE9, createInstruction("SBC", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            sbc(cpu, value);
        }));
        
        // SBC Zero Page
        instructionMap.put(0xE5, createInstruction("SBC", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC Zero Page,X
        instructionMap.put(0xF5, createInstruction("SBC", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC Absolute
        instructionMap.put(0xED, createInstruction("SBC", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC Absolute,X
        instructionMap.put(0xFD, createInstruction("SBC", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC Absolute,Y
        instructionMap.put(0xF9, createInstruction("SBC", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC (Indirect,X)
        instructionMap.put(0xE1, createInstruction("SBC", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
        
        // SBC (Indirect),Y
        instructionMap.put(0xF1, createInstruction("SBC", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            sbc(cpu, value);
        }));
    }

    private void registerLogical() {
        // AND Immediate
        instructionMap.put(0x29, createInstruction("AND", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));
        
        // AND Zero Page
        instructionMap.put(0x25, createInstruction("AND", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));
        
        // AND Absolute
        instructionMap.put(0x2D, createInstruction("AND", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));
        
        // AND Zero Page,X (0x35)
        instructionMap.put(0x35, createInstruction("AND", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));

        // AND Absolute,X (0x3D)
        instructionMap.put(0x3D, createInstruction("AND", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));

        // AND Absolute,Y (0x39)
        instructionMap.put(0x39, createInstruction("AND", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));

        // AND (Indirect,X) (0x21)
        instructionMap.put(0x21, createInstruction("AND", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));

        // AND (Indirect),Y (0x31)
        instructionMap.put(0x31, createInstruction("AND", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() & value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA Immediate
        instructionMap.put(0x09, createInstruction("ORA", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));
        
        // ORA (Indirect,X) (0x01)
        instructionMap.put(0x01, createInstruction("ORA", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA Zero Page
        instructionMap.put(0x05, createInstruction("ORA", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));
        
        // ORA Absolute
        instructionMap.put(0x0D, createInstruction("ORA", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA (Indirect),Y (0x11)
        instructionMap.put(0x11, createInstruction("ORA", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA Zero Page,X (0x15)
        instructionMap.put(0x15, createInstruction("ORA", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA Absolute,Y (0x19)
        instructionMap.put(0x19, createInstruction("ORA", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // ORA Absolute,X (0x1D)
        instructionMap.put(0x1D, createInstruction("ORA", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR Immediate
        instructionMap.put(0x49, createInstruction("EOR", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR (Indirect,X) (0x41)
        instructionMap.put(0x41, createInstruction("EOR", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR Zero Page
        instructionMap.put(0x45, createInstruction("EOR", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));
        
        // EOR Absolute
        instructionMap.put(0x4D, createInstruction("EOR", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR (Indirect),Y (0x51)
        instructionMap.put(0x51, createInstruction("EOR", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR Zero Page,X (0x55)
        instructionMap.put(0x55, createInstruction("EOR", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR Absolute,Y (0x59)
        instructionMap.put(0x59, createInstruction("EOR", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // EOR Absolute,X (0x5D)
        instructionMap.put(0x5D, createInstruction("EOR", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setA(cpu.getA() ^ value);
            cpu.updateZN(cpu.getA());
        }));

        // SLO (Indirect,X) (0x03) - opcode ilegal: ASL M; ORA M
        instructionMap.put(0x03, createInstruction("SLO", 8, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);

            // ASL em memória
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.writeByte(addr, value);

            // ORA com acumulador
            cpu.setA(cpu.getA() | value);
            cpu.updateZN(cpu.getA());
        }));
    }

    private void registerCompare() {
        // CMP Immediate
        instructionMap.put(0xC9, createInstruction("CMP", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            compare(cpu, cpu.getA(), value);
        }));
        
        // CMP Zero Page
        instructionMap.put(0xC5, createInstruction("CMP", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));
        
        // CMP Absolute
        instructionMap.put(0xCD, createInstruction("CMP", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));
        
        // CMP Zero Page,X (0xD5)
        instructionMap.put(0xD5, createInstruction("CMP", 4, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));

        // CMP Absolute,X (0xDD)
        instructionMap.put(0xDD, createInstruction("CMP", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));

        // CMP Absolute,Y (0xD9)
        instructionMap.put(0xD9, createInstruction("CMP", 4, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getY()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));

        // CMP (Indirect,X) (0xC1)
        instructionMap.put(0xC1, createInstruction("CMP", 6, cpu -> {
            int zp = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int addr = cpu.readWord(zp);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));

        // CMP (Indirect),Y (0xD1)
        instructionMap.put(0xD1, createInstruction("CMP", 5, cpu -> {
            int zp = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int addr = (cpu.readWord(zp) + cpu.getY()) & 0xFFFF;
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getA(), value);
        }));

        // CPX Immediate (0xE0)
        instructionMap.put(0xE0, createInstruction("CPX", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            compare(cpu, cpu.getX(), value);
        }));

        // CPX Zero Page (0xE4)
        instructionMap.put(0xE4, createInstruction("CPX", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getX(), value);
        }));

        // CPX Absolute (0xEC)
        instructionMap.put(0xEC, createInstruction("CPX", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getX(), value);
        }));

        // CPY Immediate (0xC0)
        instructionMap.put(0xC0, createInstruction("CPY", 2, cpu -> {
            int value = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            compare(cpu, cpu.getY(), value);
        }));

        // CPY Zero Page (0xC4)
        instructionMap.put(0xC4, createInstruction("CPY", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getY(), value);
        }));

        // CPY Absolute (0xCC)
        instructionMap.put(0xCC, createInstruction("CPY", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            compare(cpu, cpu.getY(), value);
        }));
    }

    private void registerBIT() {
        // BIT Zero Page
        instructionMap.put(0x24, createInstruction("BIT", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setZ((cpu.getA() & value) == 0);
            cpu.setV((value & 0x40) != 0);
            cpu.setN((value & 0x80) != 0);
        }));
        
        // BIT Absolute
        instructionMap.put(0x2C, createInstruction("BIT", 4, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setZ((cpu.getA() & value) == 0);
            cpu.setV((value & 0x40) != 0);
            cpu.setN((value & 0x80) != 0);
        }));
    }

    private void registerBranches() {
        // BCC - Branch if Carry Clear
        instructionMap.put(0x90, createInstruction("BCC", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (!cpu.getC()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BCS - Branch if Carry Set
        instructionMap.put(0xB0, createInstruction("BCS", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (cpu.getC()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BEQ - Branch if Equal (Zero set)
        instructionMap.put(0xF0, createInstruction("BEQ", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (cpu.getZ()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BNE - Branch if Not Equal (Zero clear)
        instructionMap.put(0xD0, createInstruction("BNE", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (!cpu.getZ()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BMI - Branch if Minus (Negative set)
        instructionMap.put(0x30, createInstruction("BMI", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (cpu.getN()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BPL - Branch if Plus (Negative clear)
        instructionMap.put(0x10, createInstruction("BPL", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (!cpu.getN()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BVC - Branch if Overflow Clear
        instructionMap.put(0x50, createInstruction("BVC", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (!cpu.getV()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
        
        // BVS - Branch if Overflow Set
        instructionMap.put(0x70, createInstruction("BVS", 2, cpu -> {
            byte offset = (byte)cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            if (cpu.getV()) {
                cpu.setPC((cpu.getPC() + offset) & 0xFFFF);
            }
        }));
    }

    private void registerJumps() {
        // JMP Absolute
        instructionMap.put(0x4C, createInstruction("JMP", 3, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(addr);
        }));
        
        // JMP Indirect
        instructionMap.put(0x6C, createInstruction("JMP", 5, cpu -> {
            int ptr = cpu.readWord(cpu.getPC());
            int addr = cpu.readWord(ptr);
            cpu.setPC(addr);
        }));
        
        // JSR - Jump to Subroutine
        instructionMap.put(0x20, createInstruction("JSR", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            cpu.pushWord(cpu.getPC() - 1);
            cpu.setPC(addr);
        }));
        
        // RTS - Return from Subroutine
        instructionMap.put(0x60, createInstruction("RTS", 6, cpu -> {
            int addr = cpu.popWord();
            cpu.setPC((addr + 1) & 0xFFFF);
        }));
    }

    private void registerStack() {
        // PHA - Push Accumulator
        instructionMap.put(0x48, createInstruction("PHA", 3, cpu -> {
            cpu.pushByte(cpu.getA());
        }));
        
        // PLA - Pull Accumulator
        instructionMap.put(0x68, createInstruction("PLA", 4, cpu -> {
            cpu.setA(cpu.popByte());
            cpu.updateZN(cpu.getA());
        }));
        
        // PHP - Push Processor Status
        instructionMap.put(0x08, createInstruction("PHP", 3, cpu -> {
            cpu.pushByte(cpu.getStatusByte() | 0x10);
        }));
        
        // PLP - Pull Processor Status
        instructionMap.put(0x28, createInstruction("PLP", 4, cpu -> {
            cpu.setStatusByte(cpu.popByte());
        }));
    }

    private void registerFlags() {
        // CLC - Clear Carry
        instructionMap.put(0x18, createInstruction("CLC", 2, cpu -> {
            cpu.setC(false);
        }));
        
        // SEC - Set Carry
        instructionMap.put(0x38, createInstruction("SEC", 2, cpu -> {
            cpu.setC(true);
        }));
        
        // CLI - Clear Interrupt Disable
        instructionMap.put(0x58, createInstruction("CLI", 2, cpu -> {
            cpu.setI(false);
        }));
        
        // SEI - Set Interrupt Disable
        instructionMap.put(0x78, createInstruction("SEI", 2, cpu -> {
            cpu.setI(true);
        }));
        
        // CLD - Clear Decimal
        instructionMap.put(0xD8, createInstruction("CLD", 2, cpu -> {
            cpu.setD(false);
        }));
        
        // SED - Set Decimal
        instructionMap.put(0xF8, createInstruction("SED", 2, cpu -> {
            cpu.setD(true);
        }));
        
        // CLV - Clear Overflow
        instructionMap.put(0xB8, createInstruction("CLV", 2, cpu -> {
            cpu.setV(false);
        }));
    }

    private void registerNOP() {
        // NOP oficial (0xEA)
        instructionMap.put(0xEA, createInstruction("NOP", 2, cpu -> {
            // Não faz nada
        }));

        // Tratar alguns opcodes ilegais comuns como NOPs de tamanho adequado
        // 0x02, 0x04, 0x07 vistos no log
        instructionMap.put(0x02, createInstruction("NOP", 2, cpu -> {
            // NOP de 2 bytes (opcode + operando imediato)
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
        }));

        instructionMap.put(0x04, createInstruction("NOP", 3, cpu -> {
            // NOP $zp (2 bytes)
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
        }));

        instructionMap.put(0x07, createInstruction("NOP", 5, cpu -> {
            // Tratar SLO zp como NOP de 2 bytes
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
        }));

        // 0x0C - NOP absoluto ilegal tratado como NOP de 3 bytes (4 ciclos)
        instructionMap.put(0x0C, createInstruction("NOP", 4, cpu -> {
            // Consome operando absoluto (2 bytes) sem efeitos colaterais
            cpu.setPC((cpu.getPC() + 2) & 0xFFFF);
        }));

        // Série de NOPs ilegais com operandos usados em ROMs comuns
        // NOP absoluto,X: 0x1C (3 bytes, 4 ciclos)
        instructionMap.put(0x1C, createInstruction("NOP", 4, cpu -> {
            cpu.setPC((cpu.getPC() + 2) & 0xFFFF);
        }));

        // NOP absoluto,X adicionais: 0x3C, 0x5C, 0x7C, 0xDC, 0xFC
        int[] absXNops = {0x3C, 0x5C, 0x7C, 0xDC, 0xFC};
        for (int opcode : absXNops) {
            instructionMap.put(opcode, createInstruction("NOP", 4, cpu -> {
                cpu.setPC((cpu.getPC() + 2) & 0xFFFF);
            }));
        }

        // NOPs com zero page / zero page,X (2 bytes)
        // 0x14, 0x34, 0x54, 0x74, 0xD4, 0xF4
        int[] zpXNops = {0x14, 0x34, 0x54, 0x74, 0xD4, 0xF4};
        for (int opcode : zpXNops) {
            instructionMap.put(opcode, createInstruction("NOP", 4, cpu -> {
                cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
            }));
        }

        // 0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xFA – NOPs de 1 byte (implied)
        int[] impliedNops = {0x1A, 0x3A, 0x5A, 0x7A, 0xDA, 0xFA};
        for (int opcode : impliedNops) {
            instructionMap.put(opcode, createInstruction("NOP", 2, cpu -> {
                // não altera PC (já foi incrementado pelo fetch)
            }));
        }

        // 0x80, 0x82 – NOPs de 2 bytes (imediato)
        instructionMap.put(0x80, createInstruction("NOP", 2, cpu -> {
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
        }));
        instructionMap.put(0x82, createInstruction("NOP", 2, cpu -> {
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
        }));

        // 0xA7 (LAX zp) – tratar como LDA zp + carregar X
        instructionMap.put(0xA7, createInstruction("LAX", 3, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setA(value);
            cpu.setX(value);
            cpu.updateZN(value);
        }));

        // 0x1F – SLO Absolute,X tratado como NOP de 3 bytes (7 ciclos)
        instructionMap.put(0x1F, createInstruction("NOP", 7, cpu -> {
            cpu.setPC((cpu.getPC() + 2) & 0xFFFF);
        }));
    }

    private void registerInterrupts() {
        // BRK - Break
        instructionMap.put(0x00, createInstruction("BRK", 7, cpu -> {
            cpu.setPC((cpu.getPC() + 1) & 0xFFFF);
            cpu.pushWord(cpu.getPC());
            cpu.pushByte(cpu.getStatusByte() | 0x10);
            cpu.setI(true);
            cpu.setPC(cpu.readWord(0xFFFE));
        }));

        // RTI - Return from Interrupt
        instructionMap.put(0x40, createInstruction("RTI", 6, cpu -> {
            cpu.setStatusByte(cpu.popByte());
            cpu.setPC(cpu.popWord());
        }));
    }

    private void registerMemIncDec() {
        // INC Zero Page
        instructionMap.put(0xE6, createInstruction("INC", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = (cpu.readByte(addr) + 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // INC Zero Page,X
        instructionMap.put(0xF6, createInstruction("INC", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = (cpu.readByte(addr) + 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // INC Absolute
        instructionMap.put(0xEE, createInstruction("INC", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = (cpu.readByte(addr) + 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // INC Absolute,X
        instructionMap.put(0xFE, createInstruction("INC", 7, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = (cpu.readByte(addr) + 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // DEC Zero Page
        instructionMap.put(0xC6, createInstruction("DEC", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = (cpu.readByte(addr) - 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // DEC Zero Page,X
        instructionMap.put(0xD6, createInstruction("DEC", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = (cpu.readByte(addr) - 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // DEC Absolute
        instructionMap.put(0xCE, createInstruction("DEC", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = (cpu.readByte(addr) - 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // DEC Absolute,X
        instructionMap.put(0xDE, createInstruction("DEC", 7, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = (cpu.readByte(addr) - 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
    }

    private void registerShifts() {
        // ASL Accumulator
        instructionMap.put(0x0A, createInstruction("ASL", 2, cpu -> {
            int value = cpu.getA();
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // ASL Zero Page
        instructionMap.put(0x06, createInstruction("ASL", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // ASL Absolute
        instructionMap.put(0x0E, createInstruction("ASL", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ASL Zero Page,X
        instructionMap.put(0x16, createInstruction("ASL", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ASL Absolute,X
        instructionMap.put(0x1E, createInstruction("ASL", 7, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x80) != 0);
            value = (value << 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // LSR Accumulator
        instructionMap.put(0x4A, createInstruction("LSR", 2, cpu -> {
            int value = cpu.getA();
            cpu.setC((value & 0x01) != 0);
            value = (value >> 1) & 0xFF;
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // LSR Zero Page
        instructionMap.put(0x46, createInstruction("LSR", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x01) != 0);
            value = (value >> 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // LSR Absolute
        instructionMap.put(0x4E, createInstruction("LSR", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x01) != 0);
            value = (value >> 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // LSR Zero Page,X (0x56)
        instructionMap.put(0x56, createInstruction("LSR", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            cpu.setC((value & 0x01) != 0);
            value = (value >> 1) & 0xFF;
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROL Accumulator
        instructionMap.put(0x2A, createInstruction("ROL", 2, cpu -> {
            int value = cpu.getA();
            int newCarry = (value & 0x80) != 0 ? 1 : 0;
            value = ((value << 1) | (cpu.getC() ? 1 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // ROL Zero Page
        instructionMap.put(0x26, createInstruction("ROL", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x80) != 0 ? 1 : 0;
            value = ((value << 1) | (cpu.getC() ? 1 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
        
        // ROL Absolute
        instructionMap.put(0x2E, createInstruction("ROL", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x80) != 0 ? 1 : 0;
            value = ((value << 1) | (cpu.getC() ? 1 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROL Zero Page,X (0x36)
        instructionMap.put(0x36, createInstruction("ROL", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x80) != 0 ? 1 : 0;
            value = ((value << 1) | (cpu.getC() ? 1 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROR Accumulator
        instructionMap.put(0x6A, createInstruction("ROR", 2, cpu -> {
            int value = cpu.getA();
            int newCarry = (value & 0x01) != 0 ? 1 : 0;
            value = ((value >> 1) | (cpu.getC() ? 0x80 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.setA(value);
            cpu.updateZN(value);
        }));
        
        // ROR Zero Page
        instructionMap.put(0x66, createInstruction("ROR", 5, cpu -> {
            int addr = cpu.readByte(cpu.getPC());
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x01) != 0 ? 1 : 0;
            value = ((value >> 1) | (cpu.getC() ? 0x80 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROR Absolute (0x6E)
        instructionMap.put(0x6E, createInstruction("ROR", 6, cpu -> {
            int addr = cpu.readWord(cpu.getPC());
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x01) != 0 ? 1 : 0;
            value = ((value >> 1) | (cpu.getC() ? 0x80 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROR Zero Page,X (0x76)
        instructionMap.put(0x76, createInstruction("ROR", 6, cpu -> {
            int addr = (cpu.readByte(cpu.getPC()) + cpu.getX()) & 0xFF;
            cpu.setPC(cpu.getPC() + 1);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x01) != 0 ? 1 : 0;
            value = ((value >> 1) | (cpu.getC() ? 0x80 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));

        // ROR Absolute,X (0x7E)
        instructionMap.put(0x7E, createInstruction("ROR", 7, cpu -> {
            int addr = (cpu.readWord(cpu.getPC()) + cpu.getX()) & 0xFFFF;
            cpu.setPC(cpu.getPC() + 2);
            int value = cpu.readByte(addr);
            int newCarry = (value & 0x01) != 0 ? 1 : 0;
            value = ((value >> 1) | (cpu.getC() ? 0x80 : 0)) & 0xFF;
            cpu.setC(newCarry != 0);
            cpu.writeByte(addr, value);
            cpu.updateZN(value);
        }));
    }

    // Funções auxiliares
    private void adc(Cpu cpu, int value) {
        int a = cpu.getA();
        int carry = cpu.getC() ? 1 : 0;
        int result = a + value + carry;
        
        cpu.setC(result > 0xFF);
        cpu.setV(((a ^ result) & (value ^ result) & 0x80) != 0);
        cpu.setA(result & 0xFF);
        cpu.updateZN(cpu.getA());
    }

    private void sbc(Cpu cpu, int value) {
        adc(cpu, value ^ 0xFF);
    }

    private void compare(Cpu cpu, int reg, int value) {
        int result = reg - value;
        cpu.setC(reg >= value);
        cpu.updateZN(result & 0xFF);
    }

    // Helper para criar instruções
    private Instruction createInstruction(String name, int cycles, InstructionExecutor executor) {
        return new Instruction() {
            @Override
            public void execute(Cpu cpu) {
                executor.execute(cpu);
            }

            @Override
            public int getCycles() {
                return cycles;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    @FunctionalInterface
    private interface InstructionExecutor {
        void execute(Cpu cpu);
    }

    public Instruction getInstruction(int opcode) {
        return instructionMap.get(opcode);
    }
}
