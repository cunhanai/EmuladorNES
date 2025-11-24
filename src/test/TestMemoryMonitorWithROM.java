package test;

import Memory.MemoryAccessMonitor;
import Memory.MemoryAccessMonitor.MemoryAccess;
import nes.NES;

import java.io.IOException;
import java.util.List;

/**
 * Teste do monitor de memória com uma ROM real
 * Executa algumas instruções e mostra os acessos à memória
 */
public class TestMemoryMonitorWithROM {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java test.TestMemoryMonitorWithROM <arquivo.nes>");
            return;
        }
        
        String romPath = args[0];
        
        try {
            System.out.println("=== Teste do Monitor de Memória com ROM ===\n");
            System.out.println("Carregando ROM: " + romPath);
            
            // Cria o emulador
            NES emulador = new NES();
            emulador.loadROM(romPath);
            emulador.reset();
            
            MemoryAccessMonitor monitor = emulador.getMemory().getMonitor();
            monitor.clear(); // Limpa acessos do carregamento
            
            System.out.println("\nExecutando primeiras instruções...\n");
            
            // Executa algumas instruções
            for (int i = 0; i < 10; i++) {
                emulador.step();
            }
            
            // Mostra estatísticas
            System.out.println("=== Estatísticas de Acesso à Memória ===");
            System.out.println(monitor.getStatistics());
            System.out.println();
            
            // Mostra acessos recentes
            System.out.println("=== Últimos 50 Acessos à Memória ===");
            List<MemoryAccess> accesses = monitor.getRecentAccesses(50);
            for (MemoryAccess access : accesses) {
                System.out.println(access.toString());
            }
            
            // Análise por segmento
            System.out.println("\n=== Análise por Segmento ===");
            analyzeBySegment(accesses);
            
            // Teste com filtros
            System.out.println("\n=== Acessos à RAM (0x0000-0x07FF) ===");
            monitor.clear();
            monitor.setAddressFilter(0x0000, 0x07FF);
            
            for (int i = 0; i < 20; i++) {
                emulador.step();
            }
            
            List<MemoryAccess> ramAccesses = monitor.getRecentAccesses(50);
            for (MemoryAccess access : ramAccesses) {
                System.out.println(access.toString());
            }
            System.out.println("Total de acessos à RAM: " + ramAccesses.size());
            
            System.out.println("\n=== Acessos aos Registradores PPU (0x2000-0x2007) ===");
            monitor.clear();
            monitor.clearFilters();
            monitor.setAddressFilter(0x2000, 0x2007);
            
            for (int i = 0; i < 20; i++) {
                emulador.step();
            }
            
            List<MemoryAccess> ppuAccesses = monitor.getRecentAccesses(50);
            for (MemoryAccess access : ppuAccesses) {
                System.out.println(access.toString());
            }
            System.out.println("Total de acessos à PPU: " + ppuAccesses.size());
            
            System.out.println("\n=== Teste Concluído com Sucesso! ===");
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar ROM: " + e.getMessage());
        }
    }
    
    private static void analyzeBySegment(List<MemoryAccess> accesses) {
        int ramReads = 0, ramWrites = 0;
        int ppuReads = 0, ppuWrites = 0;
        int romReads = 0, romWrites = 0;
        int otherReads = 0, otherWrites = 0;
        
        for (MemoryAccess access : accesses) {
            int addr = access.getAddress();
            boolean isRead = access.getType() == MemoryAccessMonitor.AccessType.READ;
            
            if (addr >= 0x0000 && addr <= 0x1FFF) {
                // RAM (including mirrors)
                if (isRead) ramReads++; else ramWrites++;
            } else if (addr >= 0x2000 && addr <= 0x3FFF) {
                // PPU registers
                if (isRead) ppuReads++; else ppuWrites++;
            } else if (addr >= 0x8000 && addr <= 0xFFFF) {
                // ROM
                if (isRead) romReads++; else romWrites++;
            } else {
                // Other
                if (isRead) otherReads++; else otherWrites++;
            }
        }
        
        System.out.println("RAM (0x0000-0x1FFF):");
        System.out.println("  Leituras: " + ramReads);
        System.out.println("  Escritas: " + ramWrites);
        
        System.out.println("PPU Registers (0x2000-0x3FFF):");
        System.out.println("  Leituras: " + ppuReads);
        System.out.println("  Escritas: " + ppuWrites);
        
        System.out.println("ROM (0x8000-0xFFFF):");
        System.out.println("  Leituras: " + romReads);
        System.out.println("  Escritas: " + romWrites);
        
        if (otherReads > 0 || otherWrites > 0) {
            System.out.println("Outros:");
            System.out.println("  Leituras: " + otherReads);
            System.out.println("  Escritas: " + otherWrites);
        }
    }
}
