package display;

import input.Controller;
import ppu.PPU;
import Memory.MonitorAcessoMemoria;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

// Janela principal do emulador e controles de teclado
public class TelaEmulador extends JFrame implements KeyListener {
    private static final int SCALE = 2; // Escala de renderização
    
    private BufferedImage screen;
    private JLabel screenLabel;
    private Controller controller;
    private MemoryViewer memoryViewer;
    private JPanel screenPanel;
    private JCheckBox monitorToggle;

    // Construtor da janela do emulador
    public TelaEmulador(Controller controller, MonitorAcessoMemoria monitor) {
        this.controller = controller;
        
        // Cria a imagem de exibição
        screen = new BufferedImage(PPU.SCREEN_WIDTH, PPU.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        // Configura a janela
        setTitle("Emulador - Analisador de ROM com Monitor de Memória");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Painel esquerdo: Tela do emulador
        screenPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Desenha a imagem da PPU escalada para caber no painel
                int targetWidth = PPU.SCREEN_WIDTH * SCALE;
                int targetHeight = PPU.SCREEN_HEIGHT * SCALE;
                g.drawImage(screen, 0, 0, targetWidth, targetHeight, null);
            }
        };
        screenPanel.setBorder(BorderFactory.createTitledBorder("Tela do Emulador"));
        screenPanel.setPreferredSize(new Dimension(PPU.SCREEN_WIDTH * SCALE, PPU.SCREEN_HEIGHT * SCALE));
        screenPanel.setMinimumSize(new Dimension(PPU.SCREEN_WIDTH * SCALE, PPU.SCREEN_HEIGHT * SCALE));

        // Mantém um JLabel simples apenas por compatibilidade, mas o desenho principal é feito no paintComponent
        screenLabel = new JLabel();
        screenPanel.add(screenLabel, BorderLayout.CENTER);
        add(screenPanel, BorderLayout.CENTER);

        // Painel direito: Monitor de memória com controle de ativação
        memoryViewer = new MemoryViewer(monitor);
        JPanel rightPanel = new JPanel(new BorderLayout());
        monitorToggle = new JCheckBox("Ativar monitor");
        monitorToggle.setSelected(false); // desativado por padrão
        monitorToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = (e.getStateChange() == ItemEvent.SELECTED);
                memoryViewer.setEnabled(enabled);
            }
        });
        rightPanel.add(monitorToggle, BorderLayout.NORTH);
        rightPanel.add(memoryViewer, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        pack();

        // Em vez de adicionar o KeyListener na janela, adicionamos diretamente no painel da tela
        // para garantir que os eventos de teclado sejam capturados quando o usuário clicar na área do jogo.
        // removeKeyListener(this); // garantia extra de não ter listener duplicado
        screenPanel.addKeyListener(this);
        screenPanel.setFocusable(true);
        screenPanel.requestFocusInWindow();

        // Mantém a possibilidade de a janela receber foco, mas o foco principal é o painel
        setFocusable(true);

        // Garante que o painel de tela possa recuperar foco quando clicado
        screenPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                screenPanel.requestFocusInWindow();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Atualiza o framebuffer da tela
    public void updateScreen(int[] framebuffer) {
        screen.setRGB(0, 0, PPU.SCREEN_WIDTH, PPU.SCREEN_HEIGHT, framebuffer, 0, PPU.SCREEN_WIDTH);
        
        // Só atualiza o monitor se ele estiver habilitado
        if (memoryViewer != null && memoryViewer.isEnabled()) {
            memoryViewer.update();
        }

        // Repaint apenas o painel de tela para reduzir trabalho da UI
        if (screenPanel != null) {
            screenPanel.repaint();
        } else {
            repaint();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed: " + e.getKeyCode());
        // Mapeia teclas do teclado para botões do nes.NES
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X: // A
                System.out.println("Botão A pressionado");
                controller.setButton(Controller.BUTTON_A, true);
                break;
            case KeyEvent.VK_Z: // B
                System.out.println("Botão B pressionado");
                controller.setButton(Controller.BUTTON_B, true);
                break;
            case KeyEvent.VK_SHIFT: // Select
                System.out.println("Select pressionado");
                controller.setButton(Controller.BUTTON_SELECT, true);
                break;
            case KeyEvent.VK_ENTER: // Start
                System.out.println("Start pressionado");
                controller.setButton(Controller.BUTTON_START, true);
                break;
            case KeyEvent.VK_UP:
                controller.setButton(Controller.BUTTON_UP, true);
                break;
            case KeyEvent.VK_DOWN:
                controller.setButton(Controller.BUTTON_DOWN, true);
                break;
            case KeyEvent.VK_LEFT:
                controller.setButton(Controller.BUTTON_LEFT, true);
                break;
            case KeyEvent.VK_RIGHT:
                controller.setButton(Controller.BUTTON_RIGHT, true);
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Libera os botões quando a tecla é solta
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X: // A
                controller.setButton(Controller.BUTTON_A, false);
                break;
            case KeyEvent.VK_Z: // B
                controller.setButton(Controller.BUTTON_B, false);
                break;
            case KeyEvent.VK_SHIFT: // Select
                controller.setButton(Controller.BUTTON_SELECT, false);
                break;
            case KeyEvent.VK_ENTER: // Start
                controller.setButton(Controller.BUTTON_START, false);
                break;
            case KeyEvent.VK_UP:
                controller.setButton(Controller.BUTTON_UP, false);
                break;
            case KeyEvent.VK_DOWN:
                controller.setButton(Controller.BUTTON_DOWN, false);
                break;
            case KeyEvent.VK_LEFT:
                controller.setButton(Controller.BUTTON_LEFT, false);
                break;
            case KeyEvent.VK_RIGHT:
                controller.setButton(Controller.BUTTON_RIGHT, false);
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Não utilizado
    }
}
