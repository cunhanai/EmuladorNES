# 🔧 Guia Técnico Rápido - Emulador NES

## 📋 Índice Rápido
1. [Principais Classes e Métodos](#principais-classes-e-métodos)
2. [Registradores e Endereços](#registradores-e-endereços)
3. [Instruções CPU Mais Usadas](#instruções-cpu-mais-usadas)
4. [Timing e Sincronização](#timing-e-sincronização)
5. [Debugging](#debugging)

---

## 🎯 Principais Classes e Métodos

### AnalisadorRomNES (Main)
```java
// Ponto de entrada
public static void main(String[] args)

// Constantes importantes
public final static double FPS = 60.09;
```

### NES (Integrador)
```java
// Carrega uma ROM
public void loadROM(String filePath) throws IOException

// Reseta o sistema
public void reset()

// Executa um frame completo (~1/60 segundo)
public void runFrame()

// Inicia emulação
public void start()

// Para emulação
public void stop()

// Obtém informações de debug
public String getDebugInfo()

// Acesso aos componentes
public Cpu getCpu()
public PPU getPpu()
public APU getApu()
public MemoryMap getMemory()
public Controller getController1()
public Controller getController2()
public int[] getFramebuffer()
```

### CPU (Processador 6502)
```java
// Executa uma instrução
public int step() // Retorna ciclos consumidos

// Reset da CPU
public void reset()

// Acesso a registradores
public int getA()       // Acumulador
public void setA(int a)
public int getX()       // Índice X
public int getY()       // Índice Y
public int getPC()      // Program Counter
public int getSP()      // Stack Pointer

// Acesso a flags
public boolean getC()   // Carry
public boolean getZ()   // Zero
public boolean getI()   // Interrupt Disable
public boolean getD()   // Decimal
public boolean getB()   // Break
public boolean getV()   // Overflow
public boolean getN()   // Negative

// Memória
public int readByte(int address)
public void writeByte(int address, int value)
public int readWord(int address)

// Stack
public void pushByte(int value)
public void pushWord(int value)
public int popByte()
public int popWord()

// Flags
public void updateZN(int value)
public int getStatusByte()
public void setStatusByte(int status)
```

### PPU (Gráficos)
```java
// Executa um ciclo da PPU
public void step()

// Reset da PPU
public void reset()

// Leitura/escrita de registradores
public int readRegister(int register)
public void writeRegister(int register, int value)

// CHR-ROM/RAM
public void loadChrRom(byte[] chr)
public int readChr(int address)
public void writeChr(int address, int value)

// OAM (sprites)
public void writeOAMByte(int address, int value)

// Estado
public boolean shouldTriggerNMI()
public void clearNMI()
public int[] getFramebuffer()
public long getFrame()

// Configuração
public void setMirrorMode(MirrorMode mode)
public void connectMapper(Mapper mapper)
```

### APU (Áudio)
```java
// Executa um ciclo da APU
public void step()

// Reset da APU
public void reset()

// Controle de áudio
public void startAudio()
public void stopAudio()

// Registradores
public void writeRegister(int register, int value)
public int readRegister(int register)

// Geração de sample
public float getSample()
```

### MemoryMap (Memória)
```java
// Leitura/escrita
public int readByte(int address)
public void writeByte(int address, int value)

// Carregamento de ROM
public void loadPRGComEspelhamento(byte[] prgRom)

// Handlers de dispositivos
public void setPpuHandler(MemoryMappedDevice handler)
public void setApuHandler(MemoryMappedDevice handler)
public void setMapper(Mapper mapper)

// Informações
public List<MemorySegment> getSegments()
public MemoryAccessMonitor getMonitor()
```

### Controller (Entrada)
```java
// Reset
public void reset()

// Estado dos botões
public void setButton(int button, boolean pressed)
public boolean getButton(int button)

// Protocolo de leitura
public void write(int value)  // Strobe
public int read()             // Leitura serial

// Constantes de botões
public static final int BUTTON_A = 0;
public static final int BUTTON_B = 1;
public static final int BUTTON_SELECT = 2;
public static final int BUTTON_START = 3;
public static final int BUTTON_UP = 4;
public static final int BUTTON_DOWN = 5;
public static final int BUTTON_LEFT = 6;
public static final int BUTTON_RIGHT = 7;
```

### LeitorINES (Carregador de ROM)
```java
// Construtor (já carrega e processa)
public LeitorINES(String nomeArquivoRom) throws IOException

// Acesso aos dados
public byte[] getPrgRom()
public byte[] getChrRom()
public Header getHeader()
```

---

## 📍 Registradores e Endereços

### Mapa de Memória Completo

```
┌──────────────┬─────────┬──────────────────────────────────┐
│   Endereço   │ Tamanho │           Descrição              │
├──────────────┼─────────┼──────────────────────────────────┤
│ $0000-$00FF  │  256B   │ Zero Page (acesso rápido)        │
│ $0100-$01FF  │  256B   │ Stack                            │
│ $0200-$07FF  │  1.5KB  │ RAM geral                        │
│ $0800-$1FFF  │  6KB    │ Espelho de $0000-$07FF (×3)      │
├──────────────┼─────────┼──────────────────────────────────┤
│ $2000        │  1B     │ PPUCTRL   (controle PPU)         │
│ $2001        │  1B     │ PPUMASK   (máscara render)       │
│ $2002        │  1B     │ PPUSTATUS (status PPU)           │
│ $2003        │  1B     │ OAMADDR   (endereço OAM)         │
│ $2004        │  1B     │ OAMDATA   (dados OAM)            │
│ $2005        │  1B     │ PPUSCROLL (scroll)               │
│ $2006        │  1B     │ PPUADDR   (endereço VRAM)        │
│ $2007        │  1B     │ PPUDATA   (dados VRAM)           │
│ $2008-$3FFF  │  ~8KB   │ Espelho de $2000-$2007           │
├──────────────┼─────────┼──────────────────────────────────┤
│ $4000-$4003  │  4B     │ APU Pulse 1                      │
│ $4004-$4007  │  4B     │ APU Pulse 2                      │
│ $4008-$400B  │  4B     │ APU Triangle                     │
│ $400C-$400F  │  4B     │ APU Noise                        │
│ $4010-$4013  │  4B     │ APU DMC                          │
│ $4014        │  1B     │ OAMDMA (DMA para sprites)        │
│ $4015        │  1B     │ APU Status                       │
│ $4016        │  1B     │ Controller 1                     │
│ $4017        │  1B     │ Controller 2 / APU Frame Counter │
├──────────────┼─────────┼──────────────────────────────────┤
│ $4020-$5FFF  │  ~8KB   │ Expansion ROM                    │
│ $6000-$7FFF  │  8KB    │ SRAM (battery-backed)            │
│ $8000-$BFFF  │  16KB   │ PRG-ROM Lower Bank               │
│ $C000-$FFFF  │  16KB   │ PRG-ROM Upper Bank               │
└──────────────┴─────────┴──────────────────────────────────┘
```

### Vetores de Interrupção

```
$FFFA-$FFFB: NMI Vector    (VBlank)
$FFFC-$FFFD: RESET Vector  (Power-on/Reset)
$FFFE-$FFFF: IRQ Vector    (Interrupções)
```

### Registradores PPU Detalhados

#### $2000 - PPUCTRL
```
7  bit  0
---- ----
VPHB SINN
|||| ||||
|||| ||++- Base nametable (0=$2000, 1=$2400, 2=$2800, 3=$2C00)
|||| |+--- VRAM increment (0=+1, 1=+32)
|||| +---- Sprite pattern table (0=$0000, 1=$1000)
|||+------ Background pattern table (0=$0000, 1=$1000)
||+------- Sprite size (0=8x8, 1=8x16)
|+-------- PPU master/slave
+--------- NMI enable (0=off, 1=on)
```

#### $2001 - PPUMASK
```
7  bit  0
---- ----
BGRs bMmG
|||| ||||
|||| |||+- Greyscale
|||| ||+-- Show background in leftmost 8 pixels
|||| |+--- Show sprites in leftmost 8 pixels
|||| +---- Show background
|||+------ Show sprites
||+------- Emphasize red
|+-------- Emphasize green
+--------- Emphasize blue
```

#### $2002 - PPUSTATUS
```
7  bit  0
---- ----
VSO- ----
|||| ||||
|||+-++++- (não usado)
||+------- Sprite overflow
|+-------- Sprite 0 hit
+--------- VBlank started (cleared on read)
```

### Registradores APU

```
$4000-$4003: Pulse 1
  $4000: Volume/Duty
  $4001: Sweep
  $4002: Timer Low
  $4003: Timer High/Length

$4004-$4007: Pulse 2 (mesma estrutura)

$4008-$400B: Triangle
  $4008: Linear Counter
  $400A: Timer Low
  $400B: Timer High/Length

$400C-$400F: Noise
  $400C: Volume
  $400E: Period
  $400F: Length

$4010-$4013: DMC
  $4010: Flags/Rate
  $4011: Direct Load
  $4012: Sample Address
  $4013: Sample Length

$4015: Status/Enable
  Read:  IF-D NT21 (flags)
  Write: ---D NT21 (enable channels)

$4017: Frame Counter
```

---

## 💾 Instruções CPU Mais Usadas

### Carregamento/Armazenamento
```
LDA #$10    ; A = $10 (Immediate)
LDA $00     ; A = [$00] (Zero Page)
LDA $8000   ; A = [$8000] (Absolute)
LDA $00,X   ; A = [$00 + X] (Zero Page,X)
LDA $8000,X ; A = [$8000 + X] (Absolute,X)
LDA ($00,X) ; A = [[$00 + X]] (Indexed Indirect)
LDA ($00),Y ; A = [[$00] + Y] (Indirect Indexed)

LDX #$10    ; X = $10
LDY #$10    ; Y = $10

STA $00     ; [$00] = A
STX $00     ; [$00] = X
STY $00     ; [$00] = Y
```

### Transferências
```
TAX         ; X = A
TAY         ; Y = A
TXA         ; A = X
TYA         ; A = Y
TXS         ; SP = X
TSX         ; X = SP
```

### Stack
```
PHA         ; Push A
PHP         ; Push Status
PLA         ; Pop A
PLP         ; Pop Status
```

### Aritmética
```
ADC #$10    ; A = A + $10 + Carry
SBC #$10    ; A = A - $10 - (1-Carry)
INC $00     ; [$00]++
DEC $00     ; [$00]--
INX         ; X++
INY         ; Y++
DEX         ; X--
DEY         ; Y--
```

### Lógicas
```
AND #$0F    ; A = A & $0F
ORA #$F0    ; A = A | $F0
EOR #$FF    ; A = A ^ $FF
```

### Shifts/Rotates
```
ASL A       ; A = A << 1 (Arithmetic Shift Left)
LSR A       ; A = A >> 1 (Logical Shift Right)
ROL A       ; Rotate Left (through Carry)
ROR A       ; Rotate Right (through Carry)
```

### Comparação
```
CMP #$10    ; Compare A com $10 (afeta C, Z, N)
CPX #$10    ; Compare X com $10
CPY #$10    ; Compare Y com $10
BIT $00     ; Test bits (afeta Z, V, N)
```

### Jumps e Branches
```
JMP $8000   ; PC = $8000
JSR $8000   ; Call subroutine
RTS         ; Return from subroutine
RTI         ; Return from interrupt

BEQ label   ; Branch if Equal (Z=1)
BNE label   ; Branch if Not Equal (Z=0)
BCS label   ; Branch if Carry Set (C=1)
BCC label   ; Branch if Carry Clear (C=0)
BMI label   ; Branch if Minus (N=1)
BPL label   ; Branch if Plus (N=0)
BVS label   ; Branch if Overflow Set (V=1)
BVC label   ; Branch if Overflow Clear (V=0)
```

### Flags
```
CLC         ; Clear Carry (C=0)
SEC         ; Set Carry (C=1)
CLI         ; Clear Interrupt Disable (I=0)
SEI         ; Set Interrupt Disable (I=1)
CLV         ; Clear Overflow (V=0)
CLD         ; Clear Decimal (D=0)
SED         ; Set Decimal (D=1)
```

### Misc
```
NOP         ; No Operation
BRK         ; Software Interrupt
```

---

## ⏱️ Timing e Sincronização

### Frequências

```java
// Master Clock (oscilador principal)
21.477272 MHz

// CPU Clock
1.789773 MHz (÷12 do master)
~559 ns por ciclo

// PPU Clock
5.369318 MHz (÷4 do master)
~186 ns por ciclo (3× mais rápido que CPU)

// APU Clock
1.789773 MHz (= CPU)
```

### Frame Timing (NTSC)

```
Taxa de refresh: 60.0988 FPS
Período por frame: ~16.639 ms

Ciclos por frame:
  CPU: 29780 ciclos
  PPU: 89340 ciclos (3× CPU)
  APU: 29780 ciclos

Scanlines por frame: 262
Ciclos por scanline: 341
```

### Scanline Breakdown

```
Scanline   0-239: Renderização visível (240 linhas)
Scanline   240:   Post-render (idle)
Scanline   241:   VBlank (início) - NMI disparado aqui
Scanline 242-260: VBlank (continua)
Scanline   261:   Pre-render (limpa flags)
```

### Timing de Instrução (exemplos)

```
LDA #imm    2 ciclos
LDA zpg     3 ciclos
LDA abs     4 ciclos
LDA abs,X   4-5 ciclos (+ 1 se cruza página)
LDA (ind),Y 5-6 ciclos (+ 1 se cruza página)

JMP abs     3 ciclos
JSR abs     6 ciclos
RTS         6 ciclos

BEQ rel     2-3 ciclos (+ 1 se branch, + 1 se cruza página)
```

### Sincronização no Código

```java
// Loop principal em AnalisadorRomNES.java
final double nsPerFrame = 1_000_000_000.0 / 60.09;

while (emulador.isRunning()) {
    long now = System.nanoTime();
    long elapsed = now - lastTime;
    
    if (elapsed >= nsPerFrame) {
        emulador.runFrame();  // ~29780 CPU cycles
        window.updateScreen();
        lastTime += (long) nsPerFrame;
    }
}

// Em NES.java - runFrame()
int cyclesPerFrame = 29780;
int cyclesRun = 0;

while (cyclesRun < cyclesPerFrame) {
    int cpuCycles = cpu.step();  // 1 instrução
    
    // Para cada ciclo de CPU
    for (int c = 0; c < cpuCycles; c++) {
        // PPU roda 3 vezes por ciclo de CPU
        for (int p = 0; p < 3; p++) {
            ppu.step();
        }
        // APU roda 1 vez por ciclo de CPU
        apu.step();
    }
    
    cyclesRun += cpuCycles;
}
```

---

## 🐛 Debugging

### Informações de Debug

```java
// Obtém info completa do sistema
String info = nes.getDebugInfo();

// Saída típica:
// Frame: 12345 | CPU: PC=$8123 A=$45 X=$00 Y=$FF SP=$FD [NV-BDIZC]
// PPU: SL=120 CY=234 | APU: Channels active
```

### Registradores CPU

```java
Cpu cpu = nes.getCpu();

System.out.printf("PC: $%04X\n", cpu.getPC());
System.out.printf("A:  $%02X\n", cpu.getA());
System.out.printf("X:  $%02X\n", cpu.getX());
System.out.printf("Y:  $%02X\n", cpu.getY());
System.out.printf("SP: $%02X\n", cpu.getSP());

// Flags
System.out.printf("Flags: %s%s%s%s%s%s%s\n",
    cpu.getN() ? "N" : "-",
    cpu.getV() ? "V" : "-",
    cpu.getD() ? "D" : "-",
    cpu.getB() ? "B" : "-",
    cpu.getI() ? "I" : "-",
    cpu.getZ() ? "Z" : "-",
    cpu.getC() ? "C" : "-"
);
```

### Estado da PPU

```java
PPU ppu = nes.getPpu();

System.out.printf("Frame: %d\n", ppu.getFrame());
System.out.printf("Scanline: %d\n", ppu.getScanline());
System.out.printf("Cycle: %d\n", ppu.getCycle());

// Verificar NMI
if (ppu.shouldTriggerNMI()) {
    System.out.println("NMI pendente!");
}
```

### Monitor de Memória

```java
MemoryAccessMonitor monitor = nes.getMemory().getMonitor();

// Ativar monitoramento de segmento específico
monitor.addFilter("PPU Registers");

// Obter acessos recentes
List<MemoryAccess> recent = monitor.getRecentAccesses(10);

for (MemoryAccess access : recent) {
    System.out.printf("%s $%04X = $%02X (%s)\n",
        access.getType(),
        access.getAddress(),
        access.getValue(),
        access.getSegmentName()
    );
}
```

### Dump de Memória

```java
MemoryMap memory = nes.getMemory();

// Dump de Zero Page
System.out.println("Zero Page:");
for (int i = 0; i < 0x100; i += 16) {
    System.out.printf("$%04X: ", i);
    for (int j = 0; j < 16; j++) {
        System.out.printf("%02X ", memory.readByte(i + j));
    }
    System.out.println();
}

// Dump de Stack
System.out.println("\nStack:");
for (int i = 0x100; i < 0x200; i += 16) {
    System.out.printf("$%04X: ", i);
    for (int j = 0; j < 16; j++) {
        System.out.printf("%02X ", memory.readByte(i + j));
    }
    System.out.println();
}
```

### Breakpoints Simples

```java
// Adicionar checagem em CPU.step()
public int step() {
    if (PC == 0x8100) {  // Breakpoint
        System.out.println("Breakpoint hit at $8100");
        System.out.println(getDebugInfo());
        // Opcional: pausar execução
        // running = false;
    }
    
    // ... resto do código
}
```

### Tracer de Instruções

```java
// Em CPU.step(), antes de executar:
public int step() {
    int opcode = memory.readByte(PC);
    Instruction inst = instructionSet.getInstruction(opcode);
    
    // Log da instrução
    System.out.printf("$%04X: %02X %-10s A:%02X X:%02X Y:%02X\n",
        PC, opcode, inst.getName(),
        A, X, Y
    );
    
    // Executa
    PC = (PC + 1) & 0xFFFF;
    inst.execute(this);
    // ...
}
```

---

## 🔍 Dicas de Debugging

### Problemas Comuns

1. **Tela preta**
   - Verificar se PPU está renderizando (PPUMASK)
   - Verificar se CHR-ROM foi carregada
   - Verificar se NMI está funcionando

2. **Jogo não responde**
   - Verificar leitura de controles ($4016)
   - Verificar strobe do controller
   - Verificar se input está sendo processado

3. **Gráficos corrompidos**
   - Verificar scrolling (PPUSCROLL, PPUADDR)
   - Verificar mirroring de VRAM
   - Verificar pattern tables (CHR)

4. **Sem áudio**
   - Verificar se APU.startAudio() foi chamado
   - Verificar se canais estão habilitados ($4015)
   - Verificar se samples estão sendo gerados

5. **Performance ruim**
   - Reduzir logging/debug
   - Verificar se está mantendo 60 FPS
   - Otimizar loop principal

### Logs Úteis

```java
// Ativar logs específicos
System.setProperty("debug.cpu", "true");
System.setProperty("debug.ppu", "true");
System.setProperty("debug.apu", "true");
System.setProperty("debug.memory", "true");
```

---

## 📊 Referência Rápida de Valores

### Opcode Examples

```
$00 = BRK       $20 = JSR abs   $40 = RTI       $60 = RTS
$08 = PHP       $28 = PLP       $48 = PHA       $68 = PLA
$10 = BPL rel   $30 = BMI rel   $50 = BVC rel   $70 = BVS rel
$18 = CLC       $38 = SEC       $58 = CLI       $78 = SEI
$90 = BCC rel   $B0 = BCS rel   $D0 = BNE rel   $F0 = BEQ rel

$A9 = LDA #imm  $AA = TAX       $A0 = LDY #imm  $A2 = LDX #imm
$85 = STA zpg   $86 = STX zpg   $84 = STY zpg
$8D = STA abs   $8E = STX abs   $8C = STY abs

$E8 = INX       $C8 = INY       $CA = DEX       $88 = DEY
$E6 = INC zpg   $C6 = DEC zpg
$EE = INC abs   $CE = DEC abs

$29 = AND #imm  $09 = ORA #imm  $49 = EOR #imm
$69 = ADC #imm  $E9 = SBC #imm
$C9 = CMP #imm  $E0 = CPX #imm  $C0 = CPY #imm

$4C = JMP abs   $6C = JMP (ind)
$EA = NOP
```

### Paleta NES (primeiras 16 cores)

```
$00 = #545454 (cinza escuro)
$01 = #001E74 (azul escuro)
$02 = #081090 (azul)
$03 = #300088 (roxo escuro)
$04 = #440064 (roxo)
$05 = #5C0030 (magenta escuro)
$06 = #540400 (vermelho escuro)
$07 = #3C1800 (marrom)
$08 = #202A00 (verde oliva)
$09 = #083A00 (verde escuro)
$0A = #004000 (verde)
$0B = #003C00 (verde azulado)
$0C = #00323C (ciano escuro)
$0D = #000000 (preto)
$0E = #000000 (preto)
$0F = #000000 (preto)
```

---

**Última Atualização**: 23 de Novembro de 2025  
**Versão**: 1.0

