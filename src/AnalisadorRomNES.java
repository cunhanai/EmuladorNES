/*
 * Projeto: Emulador nes.NES (Nintendo Entertainment System)
 * Autor: Daniel Neves e Ana Julia da Cunha
 * Disciplina: Sistemas Operacionais
 * Tema: Memória e mapeamento em consoles clássicos
 */

import Memory.MemoryAccessMonitor;
import display.EmulatorWindow;
import nes.NES;

import javax.swing.SwingUtilities;
import java.io.IOException;

public class AnalisadorRomNES {
    public final static double FPS = 60.09;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java AnalisadorRomNES <arquivo.nes>");
            System.out.println("\nEmulador nes.NES com suporte a:");
            System.out.println("  - CPU 6502 completa com conjunto de instruções");
            System.out.println("  - PPU (Picture Processing Unit) para gráficos");
            System.out.println("  - APU (Audio Processing Unit) para som");
            System.out.println("  - Controles de entrada (teclado)");
            System.out.println("\nControles:");
            System.out.println("  Setas: Direcional");
            System.out.println("  Z: Botão B");
            System.out.println("  X: Botão A");
            System.out.println("  Enter: Start");
            System.out.println("  Shift: Select");
            return;
        }

        String caminhoArquivo = args[0];
        try {
            NES emulador = new NES();
            emulador.loadROM(caminhoArquivo);
            emulador.reset();

            MemoryAccessMonitor monitor = emulador.getMemory().getMonitor();
            monitor.clearFilters();

            // Criação da janela na EDT para uma UI mais responsiva
            final EmulatorWindow[] windowHolder = new EmulatorWindow[1];
            SwingUtilities.invokeAndWait(() -> {
                windowHolder[0] = new EmulatorWindow(
                    emulador.getController1(),
                    emulador.getMemory().getMonitor()
                );
            });
            EmulatorWindow window = windowHolder[0];

            emulador.start();

            System.out.println("\n=== Emulador nes.NES Iniciado ===");
            System.out.println(emulador.getDebugInfo());

            long lastTime = System.nanoTime();
            final double nsPerFrame = 1_000_000_000.0 / FPS;

            while (emulador.isRunning()) {
                long now = System.nanoTime();
                long elapsed = now - lastTime;

                if (elapsed >= nsPerFrame) {
                    // Avança exatamente um frame lógico
                    emulador.runFrame();
                    window.updateScreen(emulador.getFramebuffer());

                    // Debug leve a cada 60 frames
                    if (emulador.getPpu().getFrame() % 60 == 0) {
                        System.out.println(emulador.getDebugInfo());
                    }

                    // Atualiza o marcador de tempo preservando o passo ideal
                    lastTime += (long) nsPerFrame;
                } else {
                    // Dorme o tempo restante aproximado para o próximo frame
                    long sleepMillis = (long) ((nsPerFrame - elapsed) / 1_000_000L);
                    if (sleepMillis > 0) {
                        try {
                            Thread.sleep(sleepMillis);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } else {
                        // Se o restante é muito pequeno, cede a CPU rapidamente
                        Thread.yield();
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao executar o emulador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
