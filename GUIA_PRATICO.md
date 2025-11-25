# 📖 Guia Prático de Uso - Emulador NES

## 📋 Índice
1. [Primeiros Passos](#primeiros-passos)
2. [Executando Jogos](#executando-jogos)
3. [Usando o Monitor de Memória](#usando-o-monitor-de-memória)
4. [Exemplos de Código](#exemplos-de-código)
5. [Troubleshooting](#troubleshooting)
6. [FAQ](#faq)

---

## 🚀 Primeiros Passos

### 1. Verificar Instalação do Java

Abra o PowerShell e execute:

```powershell
java -version
```

Você deve ver algo como:
```
java version "1.8.0_XXX" ou superior
```

Se não tiver Java instalado, baixe em: https://www.java.com/

### 2. Estrutura de Diretórios

Certifique-se de que seu projeto está organizado assim:

```
AnalisadorDeRom/
├── src/
│   ├── AnalisadorRomNES.java
│   ├── apu/
│   ├── cpu/
│   ├── display/
│   ├── input/
│   ├── leitor/
│   ├── Memory/
│   ├── nes/
│   └── ppu/
├── ROM/
│   └── (seus arquivos .nes aqui)
├── README.md
├── DOCUMENTACAO.md
└── GUIA_TECNICO.md
```

### 3. Compilar o Projeto

No PowerShell, navegue até o diretório do projeto:

```powershell
cd "C:\Users\anaj2\OneDrive\Documentos\FURB\6 semestre\Sistemas operacionais\AnalisadorDeRom"
```

Compile todos os arquivos:

```powershell
javac -d . src\AnalisadorRomNES.java src\apu\*.java src\cpu\*.java src\display\*.java src\input\*.java src\leitor\*.java src\Memory\*.java src\nes\*.java src\nes\memory\*.java src\ppu\*.java
```

Ou, se preferir um comando mais simples (requer PowerShell 5.1+):

```powershell
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { javac -d . $_.FullName }
```

Você verá arquivos `.class` sendo criados na raiz e subdiretórios.

---

## 🎮 Executando Jogos

### Executar Super Mario Bros

```powershell
java AnalisadorRomNES "ROM\Super Mario Bros. (Europe)\Super Mario Bros. (Europe).nes"
```

### Executar outro jogo

```powershell
java AnalisadorRomNES "caminho\para\seu\jogo.nes"
```

### Saída Esperada

Quando você executar o emulador, verá algo assim no console:

```
=== Criando APU ===
✓ APU criada
=== APU.startAudio() chamado ===
✓ Áudio iniciado

=== Emulador nes.NES Iniciado ===
Frame: 0 | CPU: PC=$C000 A=$00 X=$00 Y=$00 SP=$FD [--B-I--]
PPU: Scanline=0 Cycle=0

Frame: 60 | CPU: PC=$C123 A=$45 X=$12 Y=$00 SP=$FB [N-----C]
PPU: Scanline=120 Cycle=234

...
```

### Interface Visual

Uma janela aparecerá com:
- **Lado Esquerdo**: Tela do emulador (512×480 pixels)
- **Lado Direito**: Monitor de memória (desativado por padrão)

---

## 🔍 Usando o Monitor de Memória

### Ativar o Monitor

1. Execute o emulador normalmente
2. Na janela que abrir, encontre o checkbox "Ativar monitor" no canto superior direito
3. Marque a caixa para ativar

### O que o Monitor Mostra

O monitor exibe acessos recentes à memória em tempo real:

```
┌────────────────────────────────────────┐
│ □ Ativar monitor                       │
├────────────────────────────────────────┤
│ WRITE $2000 = $80 (PPU Registers)      │
│ WRITE $2001 = $1E (PPU Registers)      │
│ READ  $4016 = $41 (APU & I/O)          │
│ WRITE $2005 = $00 (PPU Registers)      │
│ WRITE $2006 = $20 (PPU Registers)      │
│ ...                                    │
└────────────────────────────────────────┘
```

### Filtrar por Segmento

Para monitorar apenas um segmento específico, você pode modificar o código:

```java
// No AnalisadorRomNES.java, após criar o emulador:
MemoryAccessMonitor monitor = emulador.getMemory().getMonitor();
monitor.clearFilters();
monitor.addFilter("PPU Registers");  // Apenas registradores PPU
// ou
monitor.addFilter("APU & I/O");      // Apenas APU e I/O
```

---

## 💻 Exemplos de Código

### Exemplo 1: Carregar e Executar uma ROM

```java
import display.TelaEmulador;
import nes.NES;

public class MinimalEmulator {
    public static void main(String[] args) {
        try {
            // 1. Criar emulador
            NES emulator = new NES();

            // 2. Carregar ROM
            emulator.loadROM("ROM/MeuJogo.nes");

            // 3. Reset
            emulator.reset();

            // 4. Criar janela
            TelaEmulador window = new TelaEmulador(
                    emulator.getController1(),
                    emulator.getMemoria().getMonitor()
            );

            // 5. Iniciar emulador
            emulator.start();

            // 6. Loop principal
            while (emulator.isRunning()) {
                emulator.runFrame();
                window.updateScreen(emulator.getFramebuffer());

                // Esperar próximo frame (~16.6ms)
                Thread.sleep(16);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Exemplo 2: Monitorar Registradores da CPU

```java
import nes.NES;
import cpu.Cpu;

public class CPUMonitor {
    public static void main(String[] args) throws Exception {
        NES nes = new NES();
        nes.loadROM("ROM/teste.nes");
        nes.reset();
        nes.start();
        
        Cpu cpu = nes.getCpu();
        
        // Executar 100 frames
        for (int frame = 0; frame < 100; frame++) {
            nes.runFrame();
            
            // A cada 10 frames, mostrar estado da CPU
            if (frame % 10 == 0) {
                System.out.printf("Frame %d:\n", frame);
                System.out.printf("  PC: $%04X\n", cpu.getPC());
                System.out.printf("  A:  $%02X\n", cpu.getA());
                System.out.printf("  X:  $%02X\n", cpu.getX());
                System.out.printf("  Y:  $%02X\n", cpu.getY());
                System.out.printf("  SP: $%02X\n", cpu.getSP());
                System.out.printf("  Flags: %c%c-%c%c%c%c%c\n",
                    cpu.getN() ? 'N' : '-',
                    cpu.getV() ? 'V' : '-',
                    cpu.getB() ? 'B' : '-',
                    cpu.getD() ? 'D' : '-',
                    cpu.getI() ? 'I' : '-',
                    cpu.getZ() ? 'Z' : '-',
                    cpu.getC() ? 'C' : '-'
                );
                System.out.println();
            }
        }
    }
}
```

### Exemplo 3: Simular Entrada do Controlador

```java
import display.TelaEmulador;
import nes.NES;
import input.Controller;

public class ControllerTest {
    public static void main(String[] args) throws Exception {
        NES nes = new NES();
        nes.loadROM("ROM/MeuJogo.nes");
        nes.reset();

        Controller controller = nes.getController1();
        TelaEmulador window = new TelaEmulador(
                controller,
                nes.getMemoria().getMonitor()
        );

        nes.start();

        // Simular sequência de entrada
        Thread.sleep(1000); // Espera 1 segundo

        // Pressiona Start
        controller.setButton(Controller.BUTTON_START, true);
        nes.runFrame();
        controller.setButton(Controller.BUTTON_START, false);

        Thread.sleep(1000);

        // Pressiona A 10 vezes
        for (int i = 0; i < 10; i++) {
            controller.setButton(Controller.BUTTON_A, true);
            nes.runFrame();
            controller.setButton(Controller.BUTTON_A, false);
            nes.runFrame();
            Thread.sleep(100);
        }

        // Continua execução normal
        while (nes.isRunning()) {
            nes.runFrame();
            window.updateScreen(nes.getFramebuffer());
            Thread.sleep(16);
        }
    }
}
```

### Exemplo 4: Capturar Screenshot

```java
import nes.NES;
import ppu.PPU;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ScreenshotCapture {
    public static void main(String[] args) throws Exception {
        NES nes = new NES();
        nes.loadROM("ROM/MeuJogo.nes");
        nes.reset();
        nes.start();
        
        // Executar alguns frames
        for (int i = 0; i < 300; i++) {  // ~5 segundos
            nes.runFrame();
        }
        
        // Capturar framebuffer
        int[] framebuffer = nes.getFramebuffer();
        
        // Criar imagem
        BufferedImage image = new BufferedImage(
            PPU.SCREEN_WIDTH,
            PPU.SCREEN_HEIGHT,
            BufferedImage.TYPE_INT_RGB
        );
        
        // Copiar pixels
        for (int y = 0; y < PPU.SCREEN_HEIGHT; y++) {
            for (int x = 0; x < PPU.SCREEN_WIDTH; x++) {
                int pixel = framebuffer[y * PPU.SCREEN_WIDTH + x];
                image.setRGB(x, y, pixel);
            }
        }
        
        // Salvar
        ImageIO.write(image, "PNG", new File("screenshot.png"));
        System.out.println("Screenshot salvo: screenshot.png");
    }
}
```

### Exemplo 5: Dump de Memória

```java
import nes.NES;
import Memory.MemoryMap;

import java.io.FileWriter;
import java.io.PrintWriter;

public class MemoryDump {
    public static void main(String[] args) throws Exception {
        NES nes = new NES();
        nes.loadROM("ROM/MeuJogo.nes");
        nes.reset();
        nes.start();

        // Executar alguns frames
        for (int i = 0; i < 60; i++) {
            nes.runFrame();
        }

        // Fazer dump da memória
        MemoryMap memory = nes.getMemoria();
        PrintWriter writer = new PrintWriter(new FileWriter("memory_dump.txt"));

        // Dump de Zero Page ($0000-$00FF)
        writer.println("=== ZERO PAGE ===");
        dumpMemoryRange(memory, writer, 0x0000, 0x00FF);

        // Dump de Stack ($0100-$01FF)
        writer.println("\n=== STACK ===");
        dumpMemoryRange(memory, writer, 0x0100, 0x01FF);

        // Dump de RAM ($0200-$07FF)
        writer.println("\n=== RAM ===");
        dumpMemoryRange(memory, writer, 0x0200, 0x07FF);

        writer.close();
        System.out.println("Memory dump salvo: memory_dump.txt");
    }

    private static void dumpMemoryRange(MemoryMap memory, PrintWriter writer,
                                        int start, int end) {
        for (int addr = start; addr <= end; addr += 16) {
            writer.printf("$%04X: ", addr);

            // Hex dump
            for (int i = 0; i < 16 && (addr + i) <= end; i++) {
                writer.printf("%02X ", memory.readByte(addr + i));
            }

            // ASCII representation
            writer.print(" | ");
            for (int i = 0; i < 16 && (addr + i) <= end; i++) {
                int b = memory.readByte(addr + i);
                char c = (b >= 32 && b < 127) ? (char) b : '.';
                writer.print(c);
            }

            writer.println();
        }
    }
}
```

### Exemplo 6: Teste de Performance

```java
import nes.NES;

public class PerformanceTest {
    public static void main(String[] args) throws Exception {
        NES nes = new NES();
        nes.loadROM("ROM/MeuJogo.nes");
        nes.reset();
        nes.start();
        
        int framesToRun = 3600;  // 1 minuto a 60 FPS
        long startTime = System.nanoTime();
        
        for (int i = 0; i < framesToRun; i++) {
            nes.runFrame();
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        double seconds = duration / 1_000_000_000.0;
        double fps = framesToRun / seconds;
        double speed = (fps / 60.09) * 100.0;
        
        System.out.printf("=== PERFORMANCE TEST ===\n");
        System.out.printf("Frames executados: %d\n", framesToRun);
        System.out.printf("Tempo decorrido: %.2f segundos\n", seconds);
        System.out.printf("FPS médio: %.2f\n", fps);
        System.out.printf("Velocidade: %.1f%% (100%% = tempo real)\n", speed);
        
        if (speed >= 95.0) {
            System.out.println("✓ Performance EXCELENTE!");
        } else if (speed >= 80.0) {
            System.out.println("✓ Performance BOA");
        } else if (speed >= 60.0) {
            System.out.println("⚠ Performance ACEITÁVEL");
        } else {
            System.out.println("✗ Performance RUIM - otimização necessária");
        }
    }
}
```

---

## 🔧 Troubleshooting

### Problema: "Uso: java AnalisadorRomNES <arquivo.nes>"

**Causa**: Você não especificou o arquivo .nes

**Solução**:
```powershell
# Correto:
java AnalisadorRomNES "ROM\MeuJogo.nes"

# Errado:
java AnalisadorRomNES
```

### Problema: "Erro ao ler o arquivo" ou "FileNotFoundException"

**Causa**: Caminho do arquivo incorreto

**Soluções**:
1. Verifique se o arquivo existe:
```powershell
Test-Path "ROM\MeuJogo.nes"
```

2. Use caminho absoluto:
```powershell
java AnalisadorRomNES "C:\caminho\completo\para\MeuJogo.nes"
```

3. Verifique espaços no caminho (use aspas):
```powershell
java AnalisadorRomNES "ROM\Super Mario Bros. (Europe)\Super Mario Bros. (Europe).nes"
```

### Problema: "ROM nes.NES inválida: não é arquivo nes.NES"

**Causa**: Arquivo não está em formato iNES

**Soluções**:
1. Verifique se o arquivo tem extensão `.nes`
2. Verifique se o arquivo não está corrompido
3. Tente baixar a ROM de outra fonte
4. Use uma ferramenta para verificar o formato (ex: [Mesen](https://www.mesen.ca/))

### Problema: Tela preta / Nada aparece

**Possíveis Causas e Soluções**:

1. **PPU não está renderizando**
   - Verifique se a ROM carregou corretamente
   - Aguarde alguns segundos (alguns jogos demoram para iniciar)

2. **CHR-ROM não carregada**
   - Verifique se o jogo tem CHR-ROM (alguns usam CHR-RAM)
   - Confira os logs no console

3. **Mapper não suportado**
   - Este emulador suporta apenas Mapper 0 (NROM)
   - Verifique qual mapper seu jogo usa

### Problema: Controles não respondem

**Soluções**:

1. **Clique na tela do emulador**
   - A janela precisa ter foco para capturar teclas

2. **Verifique o mapeamento**
   - Tente diferentes teclas
   - Confira o código em `EmulatorWindow.java`

3. **Teste com código**:
```java
// Adicione em EmulatorWindow.keyPressed():
System.out.println("Tecla pressionada: " + e.getKeyCode());
```

### Problema: Sem áudio

**Soluções**:

1. **Verifique se o áudio iniciou**
   - Procure por "✓ Áudio iniciado" no console

2. **Verifique volume do sistema**
   - Certifique-se que o volume não está mudo

3. **Reinicie o emulador**
   - Às vezes o Java Sound API precisa de restart

### Problema: Performance ruim / Lento

**Soluções**:

1. **Desative o monitor de memória**
   - Desmarca "Ativar monitor"

2. **Reduza logging**
   - Comente `System.out.println()` em lugares críticos

3. **Feche outros programas**
   - Libere recursos do sistema

4. **Use Java mais recente**
   - JDK 11+ tem melhor JIT

### Problema: "ClassNotFoundException" ao executar

**Causa**: Arquivos não compilados ou compilados no lugar errado

**Solução**:
```powershell
# Limpar e recompilar
Remove-Item *.class -Recurse
javac -d . src\**\*.java
```

### Problema: Gráficos corrompidos / glitches

**Causas possíveis**:
1. Scrolling incorreto
2. Mapper incompatível
3. Timing da PPU

**Solução**:
- Infelizmente, isso requer debugging avançado
- Verifique se outros jogos funcionam corretamente
- Pode ser uma limitação do emulador atual

---

## ❓ FAQ (Perguntas Frequentes)

### Quais jogos funcionam?

Apenas jogos que usam **Mapper 0 (NROM)**. Exemplos:
- Super Mario Bros
- Donkey Kong
- Pac-Man
- Balloon Fight
- Ice Climber
- Excitebike

### Como sei qual mapper um jogo usa?

Use uma ferramenta como [FCEUX](http://fceux.com/) ou verifique em [NESdev Wiki](https://wiki.nesdev.org/w/index.php/Mapper).

### Posso jogar com controle/gamepad?

Atualmente, apenas teclado é suportado. Para adicionar suporte a gamepad, você precisaria:
1. Usar uma biblioteca como JInput
2. Mapear botões do gamepad para `Controller.setButton()`

### Como salvar o progresso?

Save states não estão implementados. Para adicionar:
1. Serializar estado de CPU, PPU, APU, Memory
2. Salvar em arquivo
3. Restaurar quando necessário

### Posso gravar vídeos da gameplay?

Sim, mas precisa implementar:
1. Captura de framebuffer a cada frame
2. Codificação de vídeo (ex: usando FFmpeg via ProcessBuilder)
3. Gravação de áudio simultânea

### O emulador é preciso?

É funcional mas não 100% preciso. Algumas diferenças:
- Timing pode variar ligeiramente
- Alguns edge cases não implementados
- APU simplificada
- Apenas Mapper 0 suportado

### Posso distribuir este emulador?

É um projeto educacional. Para distribuição:
1. Inclua licença apropriada
2. NÃO distribua com ROMs comerciais
3. Dê créditos aos autores originais

### Como contribuir com melhorias?

Ideias de contribuição:
1. Implementar mais mappers
2. Melhorar precisão da PPU
3. Adicionar debugger
4. Otimizar performance
5. Criar testes unitários

### Onde aprender mais sobre NES?

Recursos recomendados:
- [NESdev Wiki](https://www.nesdev.org/) - Documentação completa
- [Easy 6502](https://skilldrick.github.io/easy6502/) - Tutorial de Assembly
- [NES Emulator Book](https://bugzmanov.github.io/nes_ebook/) - Guia de emulação
- [/r/EmuDev](https://www.reddit.com/r/EmuDev/) - Comunidade de desenvolvedores

---

## 🎓 Exercícios Práticos

### Exercício 1: Modificar Taxa de FPS

**Objetivo**: Fazer o emulador rodar a 30 FPS ao invés de 60 FPS

**Dica**: Modifique a constante `FPS` em `AnalisadorRomNES.java`

### Exercício 2: Adicionar Contador de Frames

**Objetivo**: Mostrar número de frames na barra de título da janela

**Dica**: Modifique `EmulatorWindow.updateScreen()` e use `setTitle()`

### Exercício 3: Implementar Pause

**Objetivo**: Pausar/despausar com tecla P

**Dica**: 
1. Adicione variável `boolean paused` em `NES`
2. Modifique `isRunning()` para considerar pause
3. Adicione tratamento de tecla P em `EmulatorWindow`

### Exercício 4: Log de Instruções

**Objetivo**: Salvar todas as instruções executadas em arquivo

**Dica**: Em `CPU.step()`, grave `PC`, `opcode` e `nome da instrução`

### Exercício 5: Criar Cheat de Vidas Infinitas

**Objetivo**: Detectar quando vidas diminuem e resetar

**Dica**:
1. Encontre endereço de memória das vidas (ex: $075A para Mario)
2. Monitore com `MemoryAccessMonitor`
3. Quando valor mudar, reescreva valor original

---

## 📝 Comandos Úteis (PowerShell)

```powershell
# Compilar tudo
javac -d . src\**\*.java

# Executar com Super Mario
java AnalisadorRomNES "ROM\Super Mario Bros. (Europe)\Super Mario Bros. (Europe).nes"

# Limpar arquivos compilados
Get-ChildItem -Include *.class -Recurse | Remove-Item

# Recompilar tudo
Get-ChildItem -Include *.class -Recurse | Remove-Item; javac -d . src\**\*.java

# Executar com output redirecionado para arquivo
java AnalisadorRomNES "ROM\MeuJogo.nes" > output.log 2>&1

# Contar linhas de código
(Get-ChildItem -Path src -Filter *.java -Recurse | Get-Content).Count

# Listar todos os arquivos Java
Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object FullName
```

---

## 🎯 Próximos Passos

Agora que você entende o básico:

1. **Experimente diferentes ROMs** (Mapper 0 apenas)
2. **Explore o código** e entenda como funciona
3. **Modifique e teste** suas próprias mudanças
4. **Leia a documentação completa** (DOCUMENTACAO.md)
5. **Consulte a referência técnica** (GUIA_TECNICO.md)

---

**Boa sorte e divirta-se emulando! 🎮**

---

**Última Atualização**: 23 de Novembro de 2025  
**Versão**: 1.0

