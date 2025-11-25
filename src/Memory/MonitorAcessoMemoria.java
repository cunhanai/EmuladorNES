package Memory;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitor de acessos à memória em tempo real
 * Registra todas as operações de leitura e escrita
 */
public class MonitorAcessoMemoria {
    
    public enum AccessType {
        READ, WRITE
    }
    
    public static class MemoryAccess {
        private final long timestamp;
        private final int address;
        private final int value;
        private final AccessType type;
        private final String segmentName;
        
        public MemoryAccess(int address, int value, AccessType type, String segmentName) {
            this.timestamp = System.nanoTime();
            this.address = address;
            this.value = value;
            this.type = type;
            this.segmentName = segmentName;
        }
        
        public long getTimestamp() { return timestamp; }
        public int getAddress() { return address; }
        public int getValue() { return value; }
        public AccessType getType() { return type; }
        public String getSegmentName() { return segmentName; }
        
        @Override
        public String toString() {
            return String.format("[%s] 0x%04X %s 0x%02X (%s)",
                type == AccessType.READ ? "R" : "W",
                address,
                type == AccessType.READ ? "<-" : "->",
                value,
                segmentName != null ? segmentName : "Unknown"
            );
        }
    }
    
    private final List<MemoryAccess> accessHistory;
    private final int maxHistorySize;
    private boolean enabled;
    private long totalReads;
    private long totalWrites;
    
    // Filtros opcionais
    private Integer filterAddressStart;
    private Integer filterAddressEnd;
    private AccessType filterType;
    
    public MonitorAcessoMemoria() {
        this(10000); // Mantém últimos 10000 acessos
    }
    
    public MonitorAcessoMemoria(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        this.accessHistory = new ArrayList<>();
        this.enabled = true;
        this.totalReads = 0;
        this.totalWrites = 0;
    }
    
    // Registra um acesso de leitura
    public void recordRead(int address, int value, String segmentName) {
        if (!enabled) return;
        
        totalReads++;
        
        // Aplica filtros se configurados
        if (shouldFilter(address, AccessType.READ)) {
            return;
        }
        
        synchronized (accessHistory) {
            MemoryAccess access = new MemoryAccess(address, value, AccessType.READ, segmentName);
            accessHistory.add(access);
            
            // Mantém o tamanho do histórico limitado
            if (accessHistory.size() > maxHistorySize) {
                accessHistory.remove(0);
            }
        }
    }
    
    // Registra um acesso de leitura
    public void recordWrite(int address, int value, String segmentName) {
        if (!enabled) return;
        
        totalWrites++;
        
        // Aplica filtros se configurados
        if (shouldFilter(address, AccessType.WRITE)) {
            return;
        }
        
        synchronized (accessHistory) {
            MemoryAccess access = new MemoryAccess(address, value, AccessType.WRITE, segmentName);
            accessHistory.add(access);
            
            // Mantém o tamanho do histórico limitado
            if (accessHistory.size() > maxHistorySize) {
                accessHistory.remove(0);
            }
        }
    }
    
    // Verifica se o acesso deve ser filtrado
    private boolean shouldFilter(int address, AccessType type) {
        if (filterType != null && filterType != type) {
            return true;
        }
        
        if (filterAddressStart != null && address < filterAddressStart) {
            return true;
        }
        
        if (filterAddressEnd != null && address > filterAddressEnd) {
            return true;
        }
        
        return false;
    }
    
    // Obtem o histórico de acessos
    public List<MemoryAccess> getAccessHistory() {
        synchronized (accessHistory) {
            return new ArrayList<>(accessHistory);
        }
    }
    
    // Obtem os últimos N acessos
    public List<MemoryAccess> getRecentAccesses(int count) {
        synchronized (accessHistory) {
            int size = accessHistory.size();
            int start = Math.max(0, size - count);
            return new ArrayList<>(accessHistory.subList(start, size));
        }
    }
    
    // Limpa o histórico
    public void clear() {
        synchronized (accessHistory) {
            accessHistory.clear();
        }
        totalReads = 0;
        totalWrites = 0;
    }
    
    // Ativa ou desativa o monitor
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    // Configura filtro por faixa de endereços
    public void setAddressFilter(Integer start, Integer end) {
        this.filterAddressStart = start;
        this.filterAddressEnd = end;
    }
    
    // Configura filtro por tipo de acesso
    public void setTypeFilter(AccessType type) {
        this.filterType = type;
    }
    
    // Limpa todos os filtros
    public void limparFiltros() {
        this.filterAddressStart = null;
        this.filterAddressEnd = null;
        this.filterType = null;
    }
    
    // Obtém estatísticas
    public long getTotalReads() {
        return totalReads;
    }
    
    public long getTotalWrites() {
        return totalWrites;
    }
    
    public int getHistorySize() {
        synchronized (accessHistory) {
            return accessHistory.size();
        }
    }
    
    // Obtém estatísticas resumidas
    public String getStatistics() {
        return String.format("Total: %d reads, %d writes | History: %d/%d",
            totalReads, totalWrites, getHistorySize(), maxHistorySize);
    }
}
