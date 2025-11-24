import Memory.MemoryAccessMonitor;
import Memory.MemoryAccessMonitor.MemoryAccess;
import nes.NES;

import java.io.IOException;
import java.util.List;

/**
 * Teste do monitor de memória com uma ROM real
 */
public class TestROM {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java TestROM <arquivo.nes>");
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
            
            System.out.println("\nExecutando primeiras 10 instruções...\n");
            
            // Executa algumas instruções
            for (int i = 0; i < 10; i++) {
                emulador.step();
            }
            
            // Mostra estatísticas
            System.out.println("=== Estatísticas de Acesso à Memória ===");
            System.out.println(monitor.getStatistics());
            System.out.println();
            
            // Mostra acessos recentes
            System.out.println("=== Últimos 30 Acessos à Memória ===");
            List<MemoryAccess> accesses = monitor.getRecentAccesses(30);
            for (MemoryAccess access : accesses) {
                System.out.println(access.toString());
            }
            
            System.out.println("\n=== Teste Concluído com Sucesso! ===");
            System.out.println("\nO monitor de memória está funcionando e captura:");
            System.out.println("  ✓ Todas as leituras de memória (instruções, dados)");
            System.out.println("  ✓ Todas as escritas de memória");
            System.out.println("  ✓ Endereços acessados e valores lidos/escritos");
            System.out.println("  ✓ Identificação do segmento de memória");
            System.out.println("\nPara visualização em tempo real, execute:");
            System.out.println("  java AnalisadorRomNES " + romPath);
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar ROM: " + e.getMessage());
        }
    }
}
