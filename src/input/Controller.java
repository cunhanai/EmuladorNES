package input;

// Controlador nes.NES (Gamepad)

public class Controller {
    // Botões do controle nes.NES
    public static final int BUTTON_A = 0;
    public static final int BUTTON_B = 1;
    public static final int BUTTON_SELECT = 2;
    public static final int BUTTON_START = 3;
    public static final int BUTTON_UP = 4;
    public static final int BUTTON_DOWN = 5;
    public static final int BUTTON_LEFT = 6;
    public static final int BUTTON_RIGHT = 7;
    
    // Estado dos botões (true = pressionado)
    private boolean[] buttons;
    
    // Latch state para leitura serial
    private int latchState;
    private int readCount;
    private boolean strobe;

    // Construtor do controlador
    public Controller() {
        buttons = new boolean[8];
        reset();
    }
    
    // Reseta o controlador
    public void reset() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = false;
        }
        latchState = 0;
        readCount = 0;
        strobe = false;
    }
    
    // Define o estado de um botão
    public void setButton(int button, boolean pressed) {
        if (button >= 0 && button < buttons.length) {
            buttons[button] = pressed;
        }
    }
    
    // Obtém o estado de um botão
    public boolean getButton(int button) {
        if (button >= 0 && button < buttons.length) {
            return buttons[button];
        }
        return false;
    }
    
    /**
     * Escreve no registrador do controlador ($4016)
     * Bit 0 controla o strobe (latch contínuo ou leitura sequencial)
     */
    public void write(int value) {
        boolean newStrobe = (value & 0x01) != 0;

        if (newStrobe) {
            // Enquanto strobe = 1, o jogo espera ler sempre o estado atual do primeiro botão
            latchState = 0;
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i]) {
                    latchState |= (1 << i);
                }
            }
            readCount = 0;
        } else if (strobe && !newStrobe) {
            // Transição 1 -> 0: captura snapshot e inicia a sequência de leitura
            latchState = 0;
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i]) {
                    latchState |= (1 << i);
                }
            }
            readCount = 0;
        }

        strobe = newStrobe;
    }
    
    // Lê o estado do controlador
    public int read() {
        int bit = 0;

        if (strobe) {
            // Com strobe=1, sempre retorna o estado atual do botão A
            bit = buttons[BUTTON_A] ? 1 : 0;
        } else {
            if (readCount < 8) {
                bit = (latchState >> readCount) & 0x01;
                readCount++;
            } else {
                // Após 8 leituras, o hardware real tende a retornar 1
                bit = 1;
            }
        }

        // No NES real, os bits mais altos normalmente vêm como 1.
        // Mantemos o bit 0 com o valor do botão e forçamos bits 6 e 7 em 1
        // para maior compatibilidade com rotinas que testam esses bits.
        return (bit & 0x01) | 0x40 | 0x80;
    }
}
