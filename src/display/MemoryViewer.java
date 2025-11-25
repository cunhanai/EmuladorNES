package display;

import Memory.MonitorAcessoMemoria;
import Memory.MonitorAcessoMemoria.MemoryAccess;
import Memory.MonitorAcessoMemoria.AccessType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;


// Visualizador de acessos à memória em tempo real
public class MemoryViewer extends JPanel {
    
    private final MonitorAcessoMemoria monitor;
    private final JTable accessTable;
    private final DefaultTableModel tableModel;
    private final JLabel statsLabel;
    private final JCheckBox enabledCheckbox;
    private final JComboBox<String> filterCombo;
    
    private static final int MAX_DISPLAY_ROWS = 100;
    private long lastUpdateTime = 0;

    // Aumenta o intervalo de atualização para reduzir impacto na UI
    private static final long UPDATE_INTERVAL_MS = 250; // Atualiza a cada 250ms

    public MemoryViewer(MonitorAcessoMemoria monitor) {
        this.monitor = monitor;

        // Tabela de acessos
        String[] columnNames = {"Tipo", "Endereço", "Valor", "Segmento", "Tempo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Monitor de Memória em Tempo Real"));
        
        // Painel de controle no topo
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        enabledCheckbox = new JCheckBox("Ativar Monitor", false);
        enabledCheckbox.addActionListener(e -> monitor.setEnabled(enabledCheckbox.isSelected()));
        controlPanel.add(enabledCheckbox);
        
        // Garante que o monitor comece desativado por padrão
        monitor.setEnabled(false);

        JButton clearButton = new JButton("Limpar");
        clearButton.addActionListener(e -> {
            monitor.clear();
            tableModel.setRowCount(0);
            updateStats();
        });
        controlPanel.add(clearButton);
        
        controlPanel.add(new JLabel("Filtro:"));
        filterCombo = new JComboBox<>(new String[]{
            "Todos",
            "Apenas Leituras",
            "Apenas Escritas",
            "RAM (0x0000-0x07FF)",
            "PPU (0x2000-0x3FFF)",
            "APU/IO (0x4000-0x401F)",
            "ROM (0x8000-0xFFFF)"
        });
        filterCombo.addActionListener(e -> applyFilter());
        controlPanel.add(filterCombo);
        
        add(controlPanel, BorderLayout.NORTH);

        accessTable = new JTable(tableModel);
        accessTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        accessTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        accessTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        accessTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        accessTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        accessTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // Renderizador de cores
        accessTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String type = (String) table.getValueAt(row, 0);
                    if ("LEITURA".equals(type)) {
                        c.setBackground(new Color(230, 245, 255)); // Azul claro
                    } else {
                        c.setBackground(new Color(255, 245, 230)); // Laranja claro
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(accessTable);
        scrollPane.setPreferredSize(new Dimension(550, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel de estatísticas no rodapé
        statsLabel = new JLabel(monitor.getStatistics());
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statsLabel, BorderLayout.SOUTH);
    }
    
    // Aplica filtro de cor
    private void applyFilter() {
        String selected = (String) filterCombo.getSelectedItem();
        
        monitor.limparFiltros();
        
        switch (selected) {
            case "Apenas Leituras":
                monitor.setTypeFilter(AccessType.READ);
                break;
            case "Apenas Escritas":
                monitor.setTypeFilter(AccessType.WRITE);
                break;
            case "RAM (0x0000-0x07FF)":
                monitor.setAddressFilter(0x0000, 0x07FF);
                break;
            case "PPU (0x2000-0x3FFF)":
                monitor.setAddressFilter(0x2000, 0x3FFF);
                break;
            case "APU/IO (0x4000-0x401F)":
                monitor.setAddressFilter(0x4000, 0x401F);
                break;
            case "ROM (0x8000-0xFFFF)":
                monitor.setAddressFilter(0x8000, 0xFFFF);
                break;
        }
        
        // Limpa a tabela para aplicar o novo filtro
        tableModel.setRowCount(0);
    }
    
    /**
     * Atualiza a visualização com acessos recentes
     */
    public void update() {
        if (!monitor.isEnabled()) {
            return; // Não atualiza UI se o monitor estiver desativado
        }

        long currentTime = System.currentTimeMillis();
        
        // Limita a taxa de atualização para não sobrecarregar a UI
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return;
        }
        
        lastUpdateTime = currentTime;
        
        // Obtém acessos recentes
        List<MemoryAccess> recentAccesses = monitor.getRecentAccesses(MAX_DISPLAY_ROWS);
        
        // Atualiza tabela apenas se houver novos acessos
        int currentRows = tableModel.getRowCount();
        int newAccessesCount = recentAccesses.size() - currentRows;
        
        if (newAccessesCount > 0) {
            // Adiciona apenas os novos acessos
            int startIndex = Math.max(0, recentAccesses.size() - newAccessesCount);
            
            for (int i = startIndex; i < recentAccesses.size(); i++) {
                MemoryAccess access = recentAccesses.get(i);
                
                Object[] row = {
                    access.getType() == AccessType.READ ? "LEITURA" : "ESCRITA",
                    String.format("0x%04X", access.getAddress()),
                    String.format("0x%02X", access.getValue()),
                    access.getSegmentName(),
                    formatTimestamp(access.getTimestamp())
                };
                
                tableModel.addRow(row);
            }
            
            // Remove linhas antigas se exceder o limite
            while (tableModel.getRowCount() > MAX_DISPLAY_ROWS) {
                tableModel.removeRow(0);
            }
            
            // Auto-scroll para a última linha
            int lastRow = accessTable.getRowCount() - 1;
            if (lastRow >= 0) {
                accessTable.scrollRectToVisible(accessTable.getCellRect(lastRow, 0, true));
            }
        }
        
        updateStats();
    }
    
    // Atualiza estatísticas exibidas
    private void updateStats() {
        statsLabel.setText(monitor.getStatistics());
    }
    
    // Formata timestamp
    private String formatTimestamp(long nanoTime) {
        // Converte para microssegundos relativos
        long microSeconds = nanoTime / 1000;
        long displayTime = microSeconds % 1000000; // Últimos 1000000 microsegundos
        return String.format("%06d μs", displayTime);
    }
}
