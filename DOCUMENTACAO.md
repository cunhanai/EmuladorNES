# Documentação Completa - Emulador NES (Nintendo Entertainment System)

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Arquitetura do Sistema](#arquitetura-do-sistema)
3. [Componentes Principais](#componentes-principais)
4. [Mapeamento de Memória](#mapeamento-de-memória)
5. [Fluxo de Execução](#fluxo-de-execução)
6. [Como Usar](#como-usar)
7. [Detalhamento Técnico](#detalhamento-técnico)

---

## 📖 Visão Geral

### O que é este projeto?
Este é um **emulador funcional do console Nintendo Entertainment System (NES)**, desenvolvido em Java como projeto acadêmico para a disciplina de Sistemas Operacionais da FURB (Universidade Regional de Blumenau).

### Objetivos do Projeto
- **Educacional**: Demonstrar conceitos de sistemas operacionais através da emulação de hardware
- **Prático**: Implementar gerenciamento de memória, sincronização de processos e I/O
- **Funcional**: Executar ROMs de jogos NES reais (como Super Mario Bros)

### Autores
- Daniel Iensen Neves
- Ana Júlia da Cunha

### Contexto Acadêmico
**Disciplina**: Sistemas Operacionais - 6º Semestre  
**Tema**: Memória e mapeamento em consoles clássicos  
**Instituição**: FURB (Universidade Regional de Blumenau)

---

## 🏗️ Arquitetura do Sistema

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│                   AnalisadorRomNES                      │
│              (Classe Principal/Main Loop)               │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│                         NES                             │
│              (Integrador de Componentes)                │
└─┬──────┬──────┬──────┬──────┬──────┬───────────────────┘
  │      │      │      │      │      │
  ▼      ▼      ▼      ▼      ▼      ▼
┌───┐  ┌───┐  ┌───┐  ┌──────┐ ┌────┐ ┌──────────┐
│CPU│  │PPU│  │APU│  │Memory│ │I/O │ │ Mapper   │
└───┘  └───┘  └───┘  └──────┘ └────┘ └──────────┘
  │      │      │       │       │         │
  └──────┴──────┴───────┴───────┴─────────┘
                 │
                 ▼
        ┌────────────────┐
        │ EmulatorWindow │
        │  (Interface)   │
        └────────────────┘
```

### Tecnologias Utilizadas
- **Linguagem**: Java
- **Interface Gráfica**: Java Swing
- **Áudio**: Java Sound API
- **Formato de ROM**: iNES (.nes)

---

## 🔧 Componentes Principais

### 1. **AnalisadorRomNES** (Classe Main)
**Arquivo**: `src/AnalisadorRomNES.java`

**Responsabilidades**:
- Ponto de entrada do programa
- Gerenciamento do loop principal de emulação
- Sincronização de frames (60.09 FPS)
- Controle de timing entre CPU, PPU e APU

**Principais Métodos**:
```java
public static void main(String[] args)
```
- Carrega a ROM especificada
- Inicializa o emulador NES
- Cria a janela de visualização
- Executa o loop principal de emulação

**Loop de Emulação**:
1. Calcula tempo decorrido desde o último frame
2. Verifica se é hora de executar um novo frame (baseado em 60.09 FPS)
3. Executa `emulador.runFrame()`
4. Atualiza a tela com `window.updateScreen()`
5. Aguarda até o próximo frame

---

### 2. **NES** (Integrador Central)
**Arquivo**: `src/nes/NES.java`

**Responsabilidades**:
- Integra todos os componentes do console (CPU, PPU, APU, Memória, Controladores)
- Gerencia a comunicação entre os componentes
- Implementa OAM DMA (Direct Memory Access)
- Sincroniza ciclos de CPU, PPU e APU

**Componentes Integrados**:
- **CPU** (Processador 6502)
- **PPU** (Picture Processing Unit)
- **APU** (Audio Processing Unit)
- **MemoryMap** (Mapa de Memória)
- **Controller** (Controles 1 e 2)
- **Mapper** (Mapeador de cartuchos)

**Sincronização de Hardware**:
- **CPU**: 1 ciclo
- **PPU**: 3 ciclos por ciclo de CPU
- **APU**: 1 ciclo por ciclo de CPU

**Método Principal**:
```java
public void runFrame()
```
Executa um frame completo de emulação (~29780 ciclos de CPU).

---

### 3. **CPU** (Processador 6502)
**Arquivo**: `src/cpu/Cpu.java`

**Responsabilidades**:
- Emula o processador MOS 6502 do NES
- Executa instruções da ROM
- Gerencia registradores e flags
- Controla a pilha de execução

**Registradores**:
- **A** (Accumulator): Registrador acumulador de 8 bits
- **X, Y** (Index Registers): Registradores de índice de 8 bits
- **PC** (Program Counter): Contador de programa de 16 bits
- **SP** (Stack Pointer): Ponteiro de pilha de 8 bits (0x0100-0x01FF)
- **P** (Status Register): Flags de status

**Flags de Status (P Register)**:
- **C** (Carry): Flag de carry
- **Z** (Zero): Flag de zero
- **I** (Interrupt Disable): Desabilita interrupções
- **D** (Decimal Mode): Modo decimal (não usado no NES)
- **B** (Break): Flag de break
- **V** (Overflow): Flag de overflow
- **N** (Negative): Flag de negativo

**Método Principal**:
```java
public int step()
```
Busca e executa uma instrução, retorna o número de ciclos consumidos.

---

### 4. **InstructionSet** (Conjunto de Instruções)
**Arquivo**: `src/cpu/InstructionSet.java`

**Responsabilidades**:
- Define todas as 256 instruções possíveis do 6502
- Implementa os 13 modos de endereçamento
- Executa operações aritméticas, lógicas e de controle de fluxo

**Modos de Endereçamento**:
1. **Implicit**: Sem operando
2. **Accumulator**: Opera no acumulador
3. **Immediate**: Valor direto
4. **Zero Page**: Endereço de 8 bits (página zero)
5. **Zero Page X/Y**: Indexado por X ou Y
6. **Absolute**: Endereço de 16 bits
7. **Absolute X/Y**: Indexado por X ou Y
8. **Indirect**: Indireção
9. **Indexed Indirect (X)**: Indireção indexada
10. **Indirect Indexed (Y)**: Indexação indireta
11. **Relative**: Para branches

**Categorias de Instruções**:
- **Transferência**: LDA, LDX, LDY, STA, STX, STY, TAX, TAY, TXA, TYA
- **Stack**: PHA, PHP, PLA, PLP
- **Lógicas**: AND, ORA, EOR
- **Aritméticas**: ADC, SBC, INC, DEC, INX, DEX, INY, DEY
- **Comparação**: CMP, CPX, CPY
- **Bit**: BIT
- **Shifts**: ASL, LSR, ROL, ROR
- **Jumps**: JMP, JSR, RTS, RTI
- **Branches**: BCC, BCS, BEQ, BNE, BMI, BPL, BVC, BVS
- **Flags**: CLC, SEC, CLI, SEI, CLV, CLD, SED
- **Misc**: NOP, BRK

---

### 5. **PPU** (Picture Processing Unit)
**Arquivo**: `src/ppu/PPU.java`

**Responsabilidades**:
- Renderiza gráficos na tela (256x240 pixels)
- Gerencia sprites e backgrounds
- Controla VRAM (Video RAM)
- Gera interrupções NMI (Non-Maskable Interrupt)

**Resolução**: 256x240 pixels

**Registradores PPU** (mapeados em 0x2000-0x2007):
- **$2000 (PPUCTRL)**: Controle da PPU
- **$2001 (PPUMASK)**: Máscara de renderização
- **$2002 (PPUSTATUS)**: Status da PPU (VBlank, Sprite 0 Hit)
- **$2003 (OAMADDR)**: Endereço OAM
- **$2004 (OAMDATA)**: Dados OAM
- **$2005 (PPUSCROLL)**: Scroll
- **$2006 (PPUADDR)**: Endereço VRAM
- **$2007 (PPUDATA)**: Dados VRAM

**Memória Interna**:
- **VRAM**: 2KB para nametables
- **OAM**: 256 bytes para sprites (64 sprites de 4 bytes cada)
- **Palette**: 32 bytes para paleta de cores
- **CHR ROM/RAM**: Pattern tables (tiles gráficos)

**Ciclos de Renderização**:
- **Scanlines**: 262 linhas por frame (0-261)
- **Cycles por scanline**: 341 ciclos
- **Scanline 0-239**: Renderização visível
- **Scanline 241**: Início do VBlank (interrupção NMI)
- **Scanline 261**: Pré-render

---

### 6. **APU** (Audio Processing Unit)
**Arquivo**: `src/apu/APU.java`

**Responsabilidades**:
- Gera áudio para o console
- Gerencia 5 canais de som
- Sincroniza com a taxa de amostragem (~44100 Hz)

**Canais de Áudio**:
1. **Pulse 1**: Onda quadrada programável
2. **Pulse 2**: Onda quadrada programável
3. **Triangle**: Onda triangular
4. **Noise**: Gerador de ruído
5. **DMC**: Delta Modulation Channel (amostras)

**Registradores APU** (0x4000-0x4017):
- **$4000-$4003**: Pulse 1
- **$4004-$4007**: Pulse 2
- **$4008-$400B**: Triangle
- **$400C-$400F**: Noise
- **$4010-$4013**: DMC
- **$4015**: Status de todos os canais
- **$4017**: Frame counter

**Sistema de Áudio**:
- Taxa de amostragem: ~44100 Hz
- Filtro passa-baixas de dois estágios para suavização
- Output via Java Sound API (SourceDataLine)

---

### 7. **MemoryMap** (Mapeamento de Memória)
**Arquivo**: `src/Memory/MemoryMap.java`

**Responsabilidades**:
- Gerencia os 64KB de espaço de endereçamento da CPU
- Roteia acessos para dispositivos corretos (PPU, APU, RAM, ROM)
- Implementa mirroring de memória
- Monitora acessos à memória

**Mapa de Memória do NES** (64KB):

```
┌──────────────┬──────────────┬────────────────────────────┐
│   Endereço   │   Tamanho    │        Descrição           │
├──────────────┼──────────────┼────────────────────────────┤
│ $0000-$07FF  │   2KB        │ RAM Interna                │
│ $0800-$1FFF  │   6KB        │ Espelhamento de RAM        │
│ $2000-$2007  │   8 bytes    │ Registradores PPU          │
│ $2008-$3FFF  │   ~8KB       │ Espelhamento de PPU        │
│ $4000-$4017  │   24 bytes   │ Registradores APU/I/O      │
│ $4018-$401F  │   8 bytes    │ APU/I/O (raramente usado)  │
│ $4020-$5FFF  │   ~8KB       │ ROM de Expansão            │
│ $6000-$7FFF  │   8KB        │ SRAM (Save RAM)            │
│ $8000-$BFFF  │   16KB       │ PRG-ROM Banco 0            │
│ $C000-$FFFF  │   16KB       │ PRG-ROM Banco 1            │
└──────────────┴──────────────┴────────────────────────────┘
```

**Espelhamento (Mirroring)**:
- **RAM**: $0000-$07FF espelhada em $0800-$1FFF (3 cópias)
- **PPU**: $2000-$2007 espelhada em $2008-$3FFF
- **PRG-ROM**: ROMs de 16KB são espelhadas em $8000-$FFFF

---

### 8. **Controller** (Controle de Entrada)
**Arquivo**: `src/input/Controller.java`

**Responsabilidades**:
- Gerencia entrada do jogador
- Implementa protocolo de leitura serial do NES
- Controla strobe para captura de estado dos botões

**Botões do Controle NES**:
```
┌──────────┬────────────┐
│  Botão   │   Bit      │
├──────────┼────────────┤
│    A     │     0      │
│    B     │     1      │
│  SELECT  │     2      │
│  START   │     3      │
│    UP    │     4      │
│   DOWN   │     5      │
│   LEFT   │     6      │
│  RIGHT   │     7      │
└──────────┴────────────┘
```

**Mapeamento de Teclado**:
- **Setas**: Direcional
- **Z**: Botão B
- **X**: Botão A
- **Enter**: Start
- **Shift**: Select

**Protocolo de Leitura**:
1. CPU escreve 1 em $4016 (strobe ON)
2. CPU escreve 0 em $4016 (strobe OFF - captura estado)
3. CPU lê $4016 8 vezes (um bit por botão)

**Registradores**:
- **$4016**: Controlador 1 (read/write)
- **$4017**: Controlador 2 (read only)

---

### 9. **LeitorINES** (Leitor de ROMs)
**Arquivo**: `src/leitor/LeitorINES.java`

**Responsabilidades**:
- Lê arquivos de ROM no formato iNES (.nes)
- Extrai header, PRG-ROM, CHR-ROM
- Valida formato do arquivo

**Formato iNES**:

```
┌─────────────┬──────────┬────────────────────────────────┐
│  Offset     │ Tamanho  │          Conteúdo              │
├─────────────┼──────────┼────────────────────────────────┤
│  0-3        │ 4 bytes  │ "NES" + $1A (identificador)    │
│  4          │ 1 byte   │ Tamanho PRG-ROM (× 16KB)       │
│  5          │ 1 byte   │ Tamanho CHR-ROM (× 8KB)        │
│  6          │ 1 byte   │ Flags 6 (mapper, mirroring)    │
│  7          │ 1 byte   │ Flags 7 (mapper)               │
│  8-15       │ 8 bytes  │ Flags adicionais/padding       │
│  16-...     │ Variável │ Trainer (512 bytes, opcional)  │
│  ...        │ Variável │ PRG-ROM (16KB × N)             │
│  ...        │ Variável │ CHR-ROM (8KB × N)              │
└─────────────┴──────────┴────────────────────────────────┘
```

---

### 10. **Header** (Cabeçalho da ROM)
**Arquivo**: `src/leitor/Header.java`

**Responsabilidades**:
- Extrai informações do header iNES
- Valida formato da ROM
- Determina configuração do cartucho

**Informações Extraídas**:
- Tamanho da PRG-ROM
- Tamanho da CHR-ROM
- Número do mapper
- Tipo de espelhamento (horizontal/vertical)
- Presença de battery-backed RAM
- Presença de trainer

**Flags do Header**:

**Flag 6** (byte 6):
```
76543210
||||||||
|||||||+- Mirroring: 0=horizontal, 1=vertical
||||||+-- Battery-backed RAM em $6000-$7FFF
|||||+--- Trainer de 512 bytes presente
||||+---- Four-screen VRAM
++++----- Lower nibble do número do mapper
```

**Flag 7** (byte 7):
```
76543210
||||||||
|||||||+- VS Unisystem
||||||+-- PlayChoice-10
||||++--- iNES 2.0 identifier
++++----- Upper nibble do número do mapper
```

---

### 11. **Mapper** (Mapeadores de Cartucho)
**Arquivo**: `src/nes/memory/Mapper.java`, `NROM.java`

**Responsabilidades**:
- Permite cartuchos controlarem acesso à memória
- Implementa bank switching (troca de bancos)
- Suporta diferentes configurações de hardware

**Mappers Implementados**:
- **Mapper 0 (NROM)**: Mais simples, sem bank switching
  - PRG-ROM: 16KB ou 32KB
  - CHR-ROM: 8KB fixo

**Interface Mapper**:
```java
int cpuRead(int address)
void cpuWrite(int address, int value)
int ppuRead(int address)
void ppuWrite(int address, int value)
MirrorMode getMirrorMode()
```

---

### 12. **EmulatorWindow** (Interface Gráfica)
**Arquivo**: `src/display/EmulatorWindow.java`

**Responsabilidades**:
- Exibe a tela do emulador
- Captura entrada do teclado
- Mostra monitor de memória (opcional)
- Renderiza framebuffer da PPU

**Componentes da Interface**:
1. **Painel de Tela**: Exibe a saída da PPU (256x240, escalado 2x)
2. **Memory Viewer**: Monitor de acessos à memória (opcional)
3. **Controles de Teclado**: KeyListener para entrada

**Escala**: 2x (512×480 pixels na janela)

---

### 13. **MemoryAccessMonitor** (Monitor de Acesso)
**Arquivo**: `src/Memory/MemoryAccessMonitor.java`

**Responsabilidades**:
- Registra acessos à memória (leitura/escrita)
- Permite filtrar por segmento
- Auxilia em debugging e análise

**Tipos de Acesso**:
- **READ**: Leitura de memória
- **WRITE**: Escrita em memória

**Informações Registradas**:
- Endereço acessado
- Valor lido/escrito
- Segmento de memória
- Timestamp do acesso

---

## 🧠 Mapeamento de Memória

### Espelhamento de Memória

O NES utiliza espelhamento (mirroring) para economizar hardware:

**RAM Interna** ($0000-$1FFF):
```
$0000-$07FF: RAM física (2KB)
$0800-$0FFF: Espelho de $0000-$07FF
$1000-$17FF: Espelho de $0000-$07FF
$1800-$1FFF: Espelho de $0000-$07FF
```

**Registradores PPU** ($2000-$3FFF):
```
$2000-$2007: Registradores PPU (8 bytes)
$2008-$3FFF: Espelhos de $2000-$2007
```

### Dispositivos Mapeados em Memória

O NES usa **memory-mapped I/O** para comunicação com periféricos:

```java
// Exemplo de leitura do controlador
int value = memory.readByte(0x4016); // Lê estado do controller 1
```

Internamente, `MemoryMap` roteia para o dispositivo correto:
```java
if (address >= 0x4000 && address <= 0x401F && apuHandler != null) {
    return apuHandler.read(address);
}
```

---

## 🔄 Fluxo de Execução

### 1. Inicialização

```
1. main() inicia
2. Valida argumentos (arquivo .nes)
3. Cria instância NES
4. loadROM() carrega arquivo
   ├── LeitorINES lê arquivo
   ├── Extrai header, PRG-ROM, CHR-ROM
   ├── Determina mapper
   └── Carrega na memória
5. reset() inicializa componentes
   ├── CPU: PC = vetor de reset ($FFFC)
   ├── PPU: Limpa framebuffer
   ├── APU: Reseta canais
   └── Controllers: Estado inicial
6. Cria EmulatorWindow
7. Inicia audio (APU)
8. Loop principal começa
```

### 2. Loop Principal de Emulação

```
while (emulador.isRunning()) {
    ┌─────────────────────────────────────┐
    │ 1. Calcula tempo desde último frame│
    └───────────────┬─────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │ 2. Verifica se é hora do próximo    │
    │    frame (60.09 FPS)                │
    └───────────────┬─────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │ 3. emulador.runFrame()              │
    │    ├── CPU executa ~29780 ciclos    │
    │    ├── PPU executa ~89340 ciclos    │
    │    ├── APU executa ~29780 ciclos    │
    │    └── Gera samples de áudio        │
    └───────────────┬─────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │ 4. window.updateScreen()            │
    │    └── Renderiza framebuffer da PPU │
    └───────────────┬─────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │ 5. Debug (a cada 60 frames)         │
    └───────────────┬─────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │ 6. Sleep até próximo frame          │
    └─────────────────────────────────────┘
}
```

### 3. Execução de um Frame

```java
public void runFrame() {
    int cyclesPerFrame = 29780;
    int cyclesRun = 0;
    
    while (cyclesRun < cyclesPerFrame) {
        // 1. Verifica e aplica DMA penalty
        applyDmaPenaltyCycles();
        
        // 2. CPU executa uma instrução
        int cpuCycles = cpu.step();
        
        // 3. Para cada ciclo de CPU:
        for (int c = 0; c < cpuCycles; c++) {
            // PPU executa 3 ciclos
            for (int p = 0; p < 3; p++) {
                ppu.step();
                if (ppu.shouldTriggerNMI()) {
                    handleNMI();
                }
            }
            // APU executa 1 ciclo
            apu.step();
        }
        
        cyclesRun += cpuCycles;
    }
}
```

### 4. Ciclo de Instrução da CPU

```
1. Fetch: Lê opcode da memória no endereço PC
2. Decode: Busca instrução no InstructionSet
3. Execute: Executa a instrução
   ├── Calcula endereço efetivo (modo de endereçamento)
   ├── Lê operandos da memória (se necessário)
   ├── Executa operação
   ├── Atualiza registradores e flags
   └── Escreve resultado (se necessário)
4. Atualiza PC para próxima instrução
5. Retorna número de ciclos consumidos
```

### 5. Renderização da PPU

```
Frame completo (262 scanlines × 341 ciclos)
│
├── Scanlines 0-239: Renderização visível
│   ├── Ciclos 1-256: Renderiza pixels
│   │   ├── Busca tiles do background
│   │   ├── Busca sprites da OAM
│   │   ├── Combina background + sprites
│   │   └── Escreve pixel no framebuffer
│   └── Ciclos 257-320: Preparação sprites
│
├── Scanline 240: Post-render (idle)
│
├── Scanline 241: VBlank
│   ├── Ciclo 1: Set VBlank flag
│   └── Dispara NMI (se habilitado)
│
├── Scanlines 242-260: VBlank continua
│
└── Scanline 261: Pre-render
    └── Limpa flags (VBlank, Sprite 0 Hit)
```

### 6. Interrupções NMI

```
1. PPU sinaliza NMI (scanline 241, ciclo 1)
2. CPU verifica flag NMI
3. handleNMI() é chamado:
   ├── Push PC (endereço de retorno)
   ├── Push Status Register
   ├── Set Interrupt Disable flag
   └── PC = readWord(0xFFFA) // Vetor NMI
4. ROM executa rotina de NMI
   └── Normalmente atualiza scroll, sprites, etc.
5. RTI (Return from Interrupt)
   ├── Pop Status Register
   ├── Pop PC
   └── Continua execução
```

---

## 📚 Como Usar

### Pré-requisitos
- Java 8 ou superior
- Arquivo de ROM NES (.nes) em formato iNES

### Compilação

```bash
# Navegar até o diretório do projeto
cd "C:\Users\anaj2\OneDrive\Documentos\FURB\6 semestre\Sistemas operacionais\AnalisadorDeRom"

# Compilar todos os arquivos Java
javac -d . src/**/*.java
```

### Execução

```bash
# Executar com uma ROM
java AnalisadorRomNES "caminho/para/seu/jogo.nes"

# Exemplo com Super Mario Bros
java AnalisadorRomNES "ROM/Super Mario Bros. (Europe)/Super Mario Bros. (Europe).nes"
```

### Controles Durante o Jogo

```
┌──────────────┬────────────────┐
│   Controle   │     Tecla      │
├──────────────┼────────────────┤
│ Direcional   │ Setas (↑↓←→)   │
│ Botão A      │ X              │
│ Botão B      │ Z              │
│ Start        │ Enter          │
│ Select       │ Shift          │
└──────────────┴────────────────┘
```

### Interface

**Painel Esquerdo**: Tela do emulador (512×480 pixels)  
**Painel Direito**: Monitor de memória (opcional)
- Checkbox "Ativar monitor" para ativar/desativar
- Mostra acessos recentes à memória

---

## 🔬 Detalhamento Técnico

### Sincronização de Timing

O NES funciona com três clocks diferentes:

```
Master Clock: 21.477272 MHz
├── CPU Clock: 1.789773 MHz (÷12)
├── PPU Clock: 5.369318 MHz (÷4)
└── APU Clock: 1.789773 MHz (÷12)
```

**Taxa de Frames**: 60.0988 FPS (NTSC)

**Ciclos por Frame**:
- CPU: ~29780 ciclos
- PPU: ~89340 ciclos (3× CPU)
- APU: ~29780 ciclos (= CPU)

### OAM DMA (Direct Memory Access)

Transferência rápida de 256 bytes de RAM para OAM:

```java
// CPU escreve em $4014
memory.writeByte(0x4014, page);

// Sistema executa DMA
for (int i = 0; i < 256; i++) {
    int data = memory.readByte((page << 8) + i);
    ppu.writeOAMByte(i, data);
}

// Penalidade: 513 ou 514 ciclos de CPU
int penalty = 513 + ((cpuCycles & 1) == 0 ? 1 : 0);
```

### Geração de Áudio

```java
// APU gera samples a ~44100 Hz
CPU_CYCLES_PER_SAMPLE = 40; // 1789773 ÷ 44100

// Mixing dos 5 canais
float pulse_out = 0.00752 * (pulse1 + pulse2);
float tnd_out = 0.00851 * triangle + 0.00494 * noise + 0.00335 * dmc;
float output = pulse_out + tnd_out;

// Filtro passa-baixas (suavização)
output = 0.5f * output + 0.5f * lastSample;
```

### Renderização de Pixels

```java
// Para cada pixel visível (scanlines 0-239, cycles 1-256):

// 1. Busca tile do background
int tileId = vram[nametableAddress];
int tileData = chrRom[tileId * 16 + fineY];

// 2. Busca cor da paleta
int paletteIndex = (tileData >> (7 - fineX)) & 1;
int colorIndex = palette[paletteIndex];

// 3. Renderiza sprites (se houver)
for each sprite in OAM {
    if (sprite overlaps pixel) {
        // Prioridade: sprite ou background?
        // Sprite 0 hit detection
    }
}

// 4. Escreve no framebuffer
framebuffer[y * 256 + x] = nesColorToRGB(colorIndex);
```

### Vetores de Interrupção

Endereços especiais na memória:

```
$FFFA-$FFFB: NMI Vector (Non-Maskable Interrupt)
$FFFC-$FFFD: RESET Vector (entrada do programa)
$FFFE-$FFFF: IRQ/BRK Vector (Interrupt Request)
```

Exemplo:
```java
public void reset() {
    PC = readWord(0xFFFC); // Lê endereço inicial do programa
}

private void handleNMI() {
    pushWord(PC);
    pushByte(getStatusByte());
    I = true;
    PC = readWord(0xFFFA); // Salta para rotina NMI
}
```

### Paleta de Cores NES

O NES possui uma paleta fixa de 64 cores (0x00-0x3F):

```
Palette RAM: 32 bytes
├── $3F00-$3F0F: Background palette (4 palettes × 4 cores)
└── $3F10-$3F1F: Sprite palette (4 palettes × 4 cores)

Espelhamento:
$3F10, $3F14, $3F18, $3F1C espelham $3F00, $3F04, $3F08, $3F0C
```

---

## 🎯 Conceitos de Sistemas Operacionais Aplicados

### 1. **Gerenciamento de Memória**
- **Segmentação**: Diferentes regiões de memória com propósitos específicos
- **Memory-Mapped I/O**: Dispositivos acessados como endereços de memória
- **Mirroring**: Economia de hardware através de espelhamento
- **DMA**: Transferência de dados sem intervenção da CPU

### 2. **Sincronização e Concorrência**
- **Timing Preciso**: Sincronização de CPU, PPU e APU
- **Interrupções**: NMI para sincronização vertical (VBlank)
- **Prioridades**: Sprites vs background, CPU vs DMA

### 3. **I/O e Dispositivos**
- **Polling**: Leitura de controles
- **Buffers**: Framebuffer, audio buffer
- **Drivers**: Handlers para PPU, APU, Input

### 4. **Escalonamento**
- **Time Slicing**: Divisão de tempo entre CPU/PPU/APU
- **Real-Time**: Manutenção de 60 FPS constante
- **Latência**: Minimização de atrasos em áudio/vídeo

---

## 📊 Estatísticas do Projeto

### Arquivos Principais
- **Total de Classes**: ~20
- **Linhas de Código**: ~3000+
- **Instruções CPU**: 256 opcodes
- **Registradores PPU**: 8
- **Canais de Áudio**: 5

### Suporte
- **Mappers**: 1 (NROM/Mapper 0)
- **ROMs Testadas**: Super Mario Bros, outros jogos simples
- **Taxa de Frames**: 60.09 FPS (NTSC)
- **Resolução**: 256×240 pixels

---

## 🐛 Limitações Conhecidas

1. **Mappers**: Apenas NROM (Mapper 0) implementado
   - Não suporta jogos complexos que requerem MMC1, MMC3, etc.

2. **APU**: Implementação simplificada
   - Alguns detalhes de timing podem não ser 100% precisos
   - Sweep units e length counters simplificados

3. **PPU**: Renderização básica
   - Scrolling pode ter pequenos glitches
   - Alguns efeitos especiais não implementados

4. **Performance**: Pode variar dependendo do hardware
   - Recomendado: CPU moderna para manter 60 FPS

---

## 🔮 Possíveis Melhorias Futuras

1. **Mappers Adicionais**
   - MMC1 (Mapper 1)
   - MMC3 (Mapper 4)
   - UxROM (Mapper 2)

2. **APU Aprimorada**
   - Sweep units completos
   - Length counters precisos
   - Filtros de áudio melhores

3. **PPU Melhorada**
   - Scrolling mais preciso
   - Sprite overflow correto
   - Background/sprite priorities completos

4. **Save States**
   - Salvar/carregar estado do emulador
   - Rewind/fast-forward

5. **Debugger**
   - Disassembler de 6502
   - Breakpoints
   - Visualizador de VRAM

6. **Interface**
   - Menu para carregar ROMs
   - Configuração de controles
   - Filtros de vídeo (scanlines, CRT shader)

---

## 📖 Referências

### Documentação Técnica
- [NESdev Wiki](https://www.nesdev.org/wiki/Nesdev_Wiki) - Documentação completa do NES
- [6502 Reference](http://www.6502.org/) - Processador 6502
- [iNES Format](https://www.nesdev.org/wiki/INES) - Formato de ROMs

### Recursos de Aprendizado
- [NES Emulator Development Guide](https://bugzmanov.github.io/nes_ebook/)
- [Easy 6502](https://skilldrick.github.io/easy6502/) - Tutorial de Assembly 6502

### Ferramentas Úteis
- [FCEUX](http://fceux.com/) - Emulador com debugger
- [Mesen](https://www.mesen.ca/) - Emulador moderno com ferramentas de desenvolvimento

---

## 👥 Contribuições e Contato

Este projeto foi desenvolvido como trabalho acadêmico por:

- **Daniel Neves**
- **Ana Julia da Cunha**

**Instituição**: FURB (Universidade Regional de Blumenau)  
**Disciplina**: Sistemas Operacionais  
**Semestre**: 6º Semestre  
**Ano**: 2025

---

## 📄 Licença

Este projeto é de cunho educacional e foi desenvolvido para fins acadêmicos.

**Nota**: As ROMs de jogos NES são propriedade de seus respectivos donos. Este emulador não inclui ROMs comerciais. Use apenas ROMs que você possui legalmente ou ROMs de domínio público/homebrew.

---

## 🏁 Conclusão

Este emulador demonstra diversos conceitos fundamentais de Sistemas Operacionais aplicados em um contexto prático e interessante. Através da emulação do Nintendo Entertainment System, exploramos:

- Gerenciamento de memória segmentada
- Sincronização de processos em tempo real
- Dispositivos de I/O mapeados em memória
- Interrupções e tratamento de eventos
- Escalonamento e timing preciso

O projeto serve como uma ferramenta educacional valiosa para entender como sistemas complexos gerenciam recursos limitados de hardware de forma eficiente.

---

**Última Atualização**: 23 de Novembro de 2025

