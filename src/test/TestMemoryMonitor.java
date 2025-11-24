package test;

import Memory.MemoryMap;
import Memory.MemoryAccessMonitor;
import Memory.MemoryAccessMonitor.MemoryAccess;
import cpu.Cpu;

import java.util.List;

/**
 * Teste do monitor de acessos à memória
 */
public class TestMemoryMonitor {
    public static void main(String[] args) {
        System.out.println("=== Teste do Monitor de Memória ===\n");
        
        MemoryMap memory = new MemoryMap();
        MemoryAccessMonitor monitor = memory.getMonitor();
        Cpu cpu = new Cpu(memory);
        
        // Limpa o monitor
        monitor.clear();
        
        // Carrega programa de teste
        byte[] testProgram = {
            (byte)0xA9, 0x42,  // LDA #$42
            (byte)0x85, 0x10,  // STA $10
            (byte)0xA5, 0x10,  // LDA $10
            (byte)0x8D, 0x00, 0x20,  // STA $2000 (PPU register)
        };
        
        memory.loadTestProgram(0x8000, testProgram);
        
        // Reset vector
        byte[] resetVector = {0x00, (byte)0x80};
        memory.loadTestProgram(0xFFFC, resetVector);
        
        System.out.println("Executando programa de teste...\n");
        cpu.reset();
        
        System.out.println("Antes da execução:");
        System.out.println("  Histórico: " + monitor.getHistorySize() + " acessos");
        System.out.println("  " + monitor.getStatistics());
        
        // Executa algumas instruções
        cpu.step(); // LDA #$42
        cpu.step(); // STA $10
        cpu.step(); // LDA $10
        cpu.step(); // STA $2000
        
        System.out.println("\nDepois da execução:");
        System.out.println("  " + monitor.getStatistics());
        System.out.println("\nÚltimos 20 acessos à memória:");
        
        List<MemoryAccess> recentAccesses = monitor.getRecentAccesses(20);
        for (MemoryAccess access : recentAccesses) {
            System.out.println("  " + access.toString());
        }
        
        // Testa filtros
        System.out.println("\n=== Teste de Filtros ===\n");
        
        monitor.clear();
        monitor.setTypeFilter(MemoryAccessMonitor.AccessType.WRITE);
        
        cpu.reset();
        cpu.step(); // LDA #$42
        cpu.step(); // STA $10
        cpu.step(); // LDA $10
        cpu.step(); // STA $2000
        
        System.out.println("Filtro: Apenas escritas");
        System.out.println("  " + monitor.getStatistics());
        System.out.println("  Histórico filtrado: " + monitor.getHistorySize() + " acessos");
        
        List<MemoryAccess> writeAccesses = monitor.getRecentAccesses(10);
        for (MemoryAccess access : writeAccesses) {
            System.out.println("  " + access.toString());
        }
        
        // Testa filtro por endereço
        System.out.println("\n=== Teste de Filtro por Endereço ===\n");
        
        monitor.clear();
        monitor.clearFilters();
        monitor.setAddressFilter(0x2000, 0x3FFF); // Apenas PPU
        
        cpu.reset();
        cpu.step(); // LDA #$42
        cpu.step(); // STA $10
        cpu.step(); // LDA $10
        cpu.step(); // STA $2000
        
        System.out.println("Filtro: Apenas região PPU (0x2000-0x3FFF)");
        System.out.println("  " + monitor.getStatistics());
        System.out.println("  Histórico filtrado: " + monitor.getHistorySize() + " acessos");
        
        List<MemoryAccess> ppuAccesses = monitor.getRecentAccesses(10);
        for (MemoryAccess access : ppuAccesses) {
            System.out.println("  " + access.toString());
        }
        
        // Testa desabilitar monitor
        System.out.println("\n=== Teste de Desabilitar Monitor ===\n");
        
        monitor.clear();
        monitor.clearFilters();
        long readsBefore = monitor.getTotalReads();
        long writesBefore = monitor.getTotalWrites();
        
        monitor.setEnabled(false);
        
        cpu.reset();
        cpu.step();
        cpu.step();
        
        System.out.println("Monitor desabilitado");
        System.out.println("  Histórico: " + monitor.getHistorySize() + " acessos");
        System.out.println("  Leituras totais: " + monitor.getTotalReads() + " (antes: " + readsBefore + ")");
        System.out.println("  Escritas totais: " + monitor.getTotalWrites() + " (antes: " + writesBefore + ")");
        
        monitor.setEnabled(true);
        System.out.println("\nMonitor reabilitado");
        
        cpu.step();
        cpu.step();
        
        System.out.println("  Histórico: " + monitor.getHistorySize() + " acessos");
        System.out.println("  " + monitor.getStatistics());
        
        System.out.println("\n=== Teste do Monitor de Memória Concluído com Sucesso! ===");
    }
}
