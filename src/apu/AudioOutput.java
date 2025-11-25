package apu;

import javax.sound.sampled.*;

/**
 * Sistema de saída de áudio usando Java Sound API
 * Gerencia um buffer de áudio e reproduz samples da APU
 */
public class AudioOutput {
    private static final int SAMPLE_RATE = 44100; // Hz
    private static final int BUFFER_SIZE = 4096; // Reduced for lower latency

    private SourceDataLine line;
    private byte[] buffer;
    private int bufferPosition;
    private boolean running;

    /**
     * Construtor do sistema de áudio
     */
    public AudioOutput() {
        try {
            AudioFormat format = new AudioFormat(
                SAMPLE_RATE,  // Sample rate
                16,           // Bits per sample
                1,            // Mono
                true,         // Signed
                false         // Little-endian
            );

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);

            // Use larger internal buffer to prevent underruns
            line.open(format, BUFFER_SIZE * 8);

            buffer = new byte[BUFFER_SIZE * 2]; // 16-bit = 2 bytes per sample
            bufferPosition = 0;

            System.out.println("Sistema de áudio inicializado: " + SAMPLE_RATE + " Hz");
        } catch (LineUnavailableException e) {
            System.err.println("Erro ao inicializar áudio: " + e.getMessage());
            line = null;
        }
    }

    /**
     * Inicia a reprodução de áudio
     */
    public void start() {
        System.out.println("=== Tentando iniciar reprodução de áudio ===");
        if (line == null) {
            System.err.println("✗ Não é possível iniciar áudio: linha não inicializada");
            return;
        }
        if (!running) {
            line.start();
            running = true;
            System.out.println("✓ Reprodução de áudio iniciada");
            System.out.println("  - Buffer disponível: " + line.available() + " bytes");
            System.out.println("  - Buffer size: " + line.getBufferSize() + " bytes");
        } else {
            System.out.println("! Áudio já está em execução");
        }
    }

    /**
     * Para a reprodução de áudio
     */
    public void stop() {
        if (line != null && running) {
            // Envia samples restantes antes de parar
            if (bufferPosition > 0) {
                line.write(buffer, 0, bufferPosition);
                bufferPosition = 0;
            }
            line.drain(); // Aguarda reprodução completa
            line.stop();
            running = false;
            System.out.println("Reprodução de áudio parada");
        }
    }

    /**
     * Adiciona um sample de áudio ao buffer
     * @param sample valor float entre -1.0 e 1.0
     */
    public void addSample(float sample) {
        if (line == null || !running) {
            return;
        }

        // Soft clipping to prevent harsh distortion
        if (sample > 1.0f) {
            sample = 1.0f;
        } else if (sample < -1.0f) {
            sample = -1.0f;
        }

        // Converte para 16-bit signed
        short value = (short) (sample * 32767.0f);

        // Little-endian
        buffer[bufferPosition++] = (byte) (value & 0xFF);
        buffer[bufferPosition++] = (byte) ((value >> 8) & 0xFF);

        // Se o buffer está cheio, envia para a linha de áudio
        if (bufferPosition >= buffer.length) {
            flushBuffer();
            bufferPosition = 0;
        }
    }

    /**
     * Envia o buffer para a linha de áudio, aguardando se necessário
     */
    private void flushBuffer() {
        if (line == null || !running) {
            return;
        }

        // Aguarda até que haja espaço suficiente no buffer da linha de áudio
        // Isso sincroniza automaticamente a emulação com a reprodução de áudio
        while (line.available() < buffer.length && running) {
            try {
                // Aguarda um pouco antes de verificar novamente
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // Escreve o buffer completo
        int written = line.write(buffer, 0, buffer.length);
        if (written < buffer.length) {
            System.err.println("Audio buffer underrun! Written: " + written + "/" + buffer.length);
        }
    }

    /**
     * Fecha o sistema de áudio
     */
    public void close() {
        if (line != null) {
            stop();
            line.drain();
            line.close();
            System.out.println("Sistema de áudio fechado");
        }
    }

    /**
     * Obtém a taxa de amostragem
     */
    public int getSampleRate() {
        return SAMPLE_RATE;
    }

    /**
     * Verifica se o áudio está disponível
     */
    public boolean isAvailable() {
        return line != null;
    }
}

