/*
 * Projeto: Emulador nes.NES (Nintendo Entertainment System)
 * Autor: Daniel Neves e Ana Julia da Cunha
 * Disciplina: Sistemas Operacionais
 * Tema: Memória e mapeamento em consoles clássicos
 */

import Memory.MonitorAcessoMemoria;
import display.TelaEmulador;
import nes.NES;

import javax.swing.SwingUtilities;

public class AnalisadorRomNES {
    public final static double FPS = 60.09;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java AnalisadorRomNES <arquivo.nes>");
            return;
        }

        String caminhoArquivo = args[0];

        try {
            var emulador = new NES();
            emulador.loadROM(caminhoArquivo);
            emulador.reset();

            MonitorAcessoMemoria monitor = emulador.getMemoria().getMonitor();
            monitor.limparFiltros();

            final TelaEmulador[] telasEmulador = new TelaEmulador[1];

            SwingUtilities.invokeAndWait(() -> {
                telasEmulador[0] = new TelaEmulador(
                    emulador.getController1(),
                    emulador.getMemoria().getMonitor()
                );
            });

            TelaEmulador janela = telasEmulador[0];

            emulador.start();

            System.out.println("\n=== Emulador nes.NES Iniciado ===");
            System.out.println(emulador.getDebugInfo());

            long lastTime = System.nanoTime();
            final double nsPerFrame = 1_000_000_000.0 / FPS;

            iniciarLoopPrincipal(emulador, lastTime, nsPerFrame, janela);

        } catch (Exception e) {
            System.err.println("Erro ao executar o emulador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void iniciarLoopPrincipal(NES emulador, long lastTime, double nsPerFrame, TelaEmulador janela) {
        while (emulador.isRunning()) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;

            if (elapsed >= nsPerFrame) {
                // Avança exatamente um frame lógico
                emulador.runFrame();
                janela.updateScreen(emulador.getFramebuffer());

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
    }
}
