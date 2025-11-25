# 🎮 Emulador NES - Analisador de ROM

<div align="center">

![NES](https://img.shields.io/badge/Nintendo-NES-red?style=for-the-badge&logo=nintendo)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Funcional-success?style=for-the-badge)

**Emulador parcial do Nintendo Entertainment System desenvolvido em Java**

*Projeto Acadêmico - Sistemas Operacionais - FURB*

[Documentação Completa](DOCUMENTACAO.md) | [Sobre](#sobre) | [Como Usar](#como-usar)

</div>

---

## 📖 Sobre

Este é um **emulador parcial do NES (Nintendo Entertainment System)** desenvolvido como projeto acadêmico para a 
disciplina de Sistemas Operacionais. O projeto demonstra alguns conceitos fundamentais de SO através da emulação de um
hardware clássico de 8-bit.

### 🎯 Objetivos

- ✅ Implementar gerênciamento de memória segmentada
- ✅ Demonstrar sincronização de processos em tempo real
- ✅ Simular dispositivos de I/O mapeados em memória
- ✅ Implementar sistema de interrupções (NMI)
- ✅ Executar ROMs de jogos NES reais

### 👥 Autores

- **Ana Júlia da Cunha**
- **Daniel Iensen Neves**

**Instituição**: FURB (Universidade Regional de Blumenau)  
**Disciplina**: Sistemas Operacionais - 6º Semestre

---

## ⚙️ Componentes Implementados

### 🖥️ Hardware Emulado

| Componente | Descrição                                                            |
|------------|----------------------------------------------------------------------|
| **CPU 6502** | Processador principal com 256 instruções                             |
| **PPU** | Picture Processing Unit (256×240 pixels), responsável pelos gráficos |
| **APU** | Audio Processing Unit (5 canais), responsável pelos áudios           |
| **Memory Map** | 64KB de espaço de endereçamento                                      |
| **Controllers** | Mapeamento dos controles do jogo para o teclado                      |

### 🎮 Funcionalidades

- ✅ Execução de ROMs formato iNES (`.nes`)
- ✅ Renderização de gráficos em tempo real (60.09 FPS)
- ✅ Áudio com 5 canais (Pulse, Triangle, Noise, DMC)
- ✅ Entrada via teclado
- ✅ Monitor de acessos à memória
- ✅ Interface gráfica com Swing

---

## 🚀 Como Usar

### Pré-requisitos

- Java 8 ou superior instalado
- Arquivo de ROM NES (.nes) em formato iNES
- IDEA IntelliJ (mais estável)

### Execução

- Compilar o projeto

```bash
# Executar com uma ROM
java AnalisadorRomNES "caminho/para/jogo.nes"

# Exemplo:
java AnalisadorRomNES "Super Mario Bros. (Europe).nes"
```

### 🎮 Mapeamento dos controles

```
┌────────────────┬─────────────┐
│  Controle NES  │   Teclado   │
├────────────────┼─────────────┤
│ Direcional     │ ↑ ↓ ← →     │
│ Botão A        │ X           │
│ Botão B        │ Z           │
│ Start          │ Enter       │
│ Select         │ Shift       │
└────────────────┴─────────────┘
```

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────┐
│      AnalisadorRomNES           │
│      (Loop Principal)           │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│            NES                  │
│    (Integrador Central)         │
└─┬────┬────┬────┬────┬──────────┘
  │    │    │    │    │
  ▼    ▼    ▼    ▼    ▼
┌───┐┌───┐┌───┐┌────┐┌──────┐
│CPU││PPU││APU││Mem ││Input │
└───┘└───┘└───┘└────┘└──────┘
```

### Mapa de Memória (64KB)

```
$0000-$07FF  │ RAM Interna (2KB)
$0800-$1FFF  │ Espelhamento de RAM
$2000-$2007  │ Registradores PPU
$2008-$3FFF  │ Espelhamento de PPU
$4000-$4017  │ APU e I/O
$4018-$401F  │ Funcionalidades de APU e I/O normalmentr desabilitadas
$4020-$5FFF  │ ROM de Expansão
$6000-$7FFF  │ SRAM (Save RAM)
$8000-$FFFF  │ PRG-ROM (Código do jogo)
```

---

## 🔬 Conceitos de SO Demonstrados

### 1. Gerenciamento de Memória
- Segmentação de memória
- Memory-mapped I/O
- Mirroring (espelhamento)
- DMA (Direct Memory Access)

### 2. Sincronização
- Timing preciso entre CPU/PPU/APU
- Interrupções (NMI) para sincronização vertical
- Frame rate constante (60.09 FPS)

### 3. I/O e Dispositivos
- Controladores mapeados em memória
- Buffers de vídeo e áudio
- Handlers para dispositivos

### 4. Processos em Tempo Real
- Escalonamento de ciclos
- Latência mínima
- Prioridades (sprites vs background)

---

## 📊 Especificações Técnicas

### Hardware NES Original

| Componente | Especificação |
|------------|---------------|
| CPU | MOS 6502 @ 1.79 MHz |
| PPU | Custom @ 5.37 MHz |
| RAM | 2KB interna |
| VRAM | 2KB para nametables |
| Resolução | 256×240 pixels |
| Paleta | 64 cores fixas |
| Taxa de Frames | 60.0988 FPS (NTSC) |
| Canais de Áudio | 5 (2 Pulse, Triangle, Noise, DMC) |

## 📁 Estrutura do Projeto

```
AnalisadorDeRom/
├── src/
│   ├── AnalisadorRomNES.java    # Classe principal
│   ├── apu/                      # Audio Processing Unit
│   ├── cpu/                      # Processador 6502
│   ├── display/                  # Interface gráfica
│   ├── input/                    # Controles
│   ├── leitor/                   # Leitor de ROMs
│   ├── Memory/                   # Sistema de memória
│   ├── nes/                      # Integração NES
│   │   └── memory/               # Mappers
│   └── ppu/                      # Picture Processing Unit
└── ROM/                          # ROMs de teste
```

---

## 🐛 Limitações Conhecidas

- **Mappers**: Apenas NROM (Mapper 0) implementado
- **APU**: Alguns detalhes de timing simplificados
- **PPU**: Scrolling pode ter pequenos glitches, cores não estão fiéis

---