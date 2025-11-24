package test;

import Memory.MemoryMap;
import cpu.Cpu;

/**
 * Simple tests for the 6502 CPU
 */
public class TestCPU {
    public static void main(String[] args) {
        System.out.println("=== Teste da CPU 6502 ===\n");
        
        MemoryMap memory = new MemoryMap();
        Cpu cpu = new Cpu(memory);
        
        // Carrega programa de teste
        byte[] testProgram = {
            (byte)0xA9, 0x42,  // 0x8000: LDA #$42
            (byte)0xAA,        // 0x8002: TAX
            (byte)0xE8,        // 0x8003: INX
            0x18,              // 0x8004: CLC
            0x69, 0x10,        // 0x8005: ADC #$10
            (byte)0x85, 0x00,  // 0x8007: STA $00
            (byte)0xA9, 0x00,  // 0x8009: LDA #$00
            (byte)0xA5, 0x00,  // 0x800B: LDA $00
            0x48,              // 0x800D: PHA
            (byte)0xA9, (byte)0xFF, // 0x800E: LDA #$FF
            0x68,              // 0x8010: PLA
            (byte)0xA9, 0x01,  // 0x8011: LDA #$01
            (byte)0xD0, 0x02,  // 0x8013: BNE +2
            (byte)0xEA,        // 0x8015: NOP
            (byte)0xEA,        // 0x8016: NOP
            (byte)0xA9, (byte)0xAA, // 0x8017: LDA #$AA
        };
        
        memory.loadTestProgram(0x8000, testProgram);
        
        // Reset vector
        byte[] resetVector = {0x00, (byte)0x80}; // Aponta para 0x8000
        memory.loadTestProgram(0xFFFC, resetVector);
        
        // Teste 1: LDA Immediate
        System.out.println("Teste 1: LDA #$42");
        
        cpu.reset();
        System.out.printf("Antes: A=0x%02X, PC=0x%04X\n", cpu.getA(), cpu.getPC());
        cpu.step();
        System.out.printf("Depois: A=0x%02X, PC=0x%04X, Z=%s, N=%s\n", 
            cpu.getA(), cpu.getPC(), cpu.getZ(), cpu.getN());
        System.out.println("✓ Esperado: A=0x42, Z=false, N=false\n");
        
        // Teste 2: TAX (Transfer A to X)
        System.out.println("Teste 2: TAX");
        System.out.printf("Antes: A=0x%02X, X=0x%02X\n", cpu.getA(), cpu.getX());
        cpu.step();
        System.out.printf("Depois: A=0x%02X, X=0x%02X\n", cpu.getA(), cpu.getX());
        System.out.println("✓ Esperado: X=0x42\n");
        
        // Teste 3: INX (Increment X)
        System.out.println("Teste 3: INX");
        System.out.printf("Antes: X=0x%02X\n", cpu.getX());
        cpu.step();
        System.out.printf("Depois: X=0x%02X\n", cpu.getX());
        System.out.println("✓ Esperado: X=0x43\n");
        
        // Teste 4: ADC (Add with Carry)
        System.out.println("Teste 4: CLC e ADC #$10");
        cpu.step(); // CLC
        System.out.printf("Antes ADC: A=0x%02X, C=%s\n", cpu.getA(), cpu.getC());
        cpu.step(); // ADC
        System.out.printf("Depois ADC: A=0x%02X, C=%s\n", cpu.getA(), cpu.getC());
        System.out.println("✓ Esperado: A=0x52 (0x42 + 0x10)\n");
        
        // Teste 5: STA (Store A) e LDA (Load A)
        System.out.println("Teste 5: STA $00, LDA #$00, LDA $00");
        cpu.step(); // STA
        System.out.printf("Gravado na memória $00: 0x%02X\n", memory.readByte(0x00));
        cpu.step(); // LDA #$00
        System.out.printf("A zerado: A=0x%02X\n", cpu.getA());
        cpu.step(); // LDA $00
        System.out.printf("Lido da memória $00: A=0x%02X\n", cpu.getA());
        System.out.println("✓ Esperado: A=0x52\n");
        
        // Teste 6: Stack operations
        System.out.println("Teste 6: PHA (Push A), LDA #$FF, PLA (Pull A)");
        int spBefore = cpu.getSP();
        cpu.step(); // PHA
        System.out.printf("Após PHA: SP=0x%02X (antes: 0x%02X)\n", cpu.getSP(), spBefore);
        cpu.step(); // LDA #$FF
        System.out.printf("A modificado: A=0x%02X\n", cpu.getA());
        cpu.step(); // PLA
        System.out.printf("Após PLA: A=0x%02X, SP=0x%02X\n", cpu.getA(), cpu.getSP());
        System.out.println("✓ Esperado: A=0x52, SP restaurado\n");
        
        // Teste 7: Branch
        System.out.println("Teste 7: LDA #$01, BNE +2 (pula 2 NOPs), LDA #$AA");
        cpu.step(); // LDA #$01
        int pcBefore = cpu.getPC();
        cpu.step(); // BNE
        System.out.printf("Após BNE: PC=0x%04X (esperado: 0x%04X)\n", 
            cpu.getPC(), pcBefore + 2);
        cpu.step(); // LDA #$AA
        System.out.printf("Valor carregado: A=0x%02X\n", cpu.getA());
        System.out.println("✓ Esperado: A=0xAA\n");
        
        System.out.println("=== Todos os testes da CPU concluídos! ===");
        System.out.println("Total de ciclos executados: " + cpu.getTotalCycles());
    }
}
