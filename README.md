# 🎮 Emulador NES - Analisador de ROM

<div align="center">

![NES](https://img.shields.io/badge/Nintendo-NES-red?style=for-the-badge&logo=nintendo)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Funcional-success?style=for-the-badge)

**Emulador funcional do Nintendo Entertainment System desenvolvido em Java**

*Projeto Acadêmico - Sistemas Operacionais - FURB*

[Documentação Completa](DOCUMENTACAO.md) | [Sobre](#sobre) | [Como Usar](#como-usar)

</div>

---

## 📖 Sobre

Este é um **emulador completo do NES (Nintendo Entertainment System)** desenvolvido como projeto acadêmico para a disciplina de Sistemas Operacionais. O projeto demonstra conceitos fundamentais de SO através da emulação de hardware clássico.

### 🎯 Objetivos

- ✅ Implementar gerenciamento de memória segmentada
- ✅ Demonstrar sincronização de processos em tempo real
- ✅ Simular dispositivos de I/O mapeados em memória
- ✅ Implementar sistema de interrupções (NMI)
- ✅ Executar ROMs de jogos NES reais

### 👥 Autores

- **Daniel Neves**
- **Ana Julia da Cunha**

**Instituição**: FURB (Universidade Regional de Blumenau)  
**Disciplina**: Sistemas Operacionais - 6º Semestre  
**Tema**: Memória e mapeamento em consoles clássicos

---

## ⚙️ Componentes Implementados

### 🖥️ Hardware Emulado

| Componente | Descrição | Status |
|------------|-----------|--------|
| **CPU 6502** | Processador principal com 256 instruções | ✅ Completo |
| **PPU** | Picture Processing Unit (256×240 pixels) | ✅ Funcional |
| **APU** | Audio Processing Unit (5 canais) | ✅ Funcional |
| **Memory Map** | 64KB de espaço de endereçamento | ✅ Completo |
| **Controllers** | 2 controles com 8 botões cada | ✅ Completo |
| **Mapper 0** | NROM (cartuchos simples) | ✅ Completo |

### 🎮 Funcionalidades

- ✅ Execução de ROMs formato iNES (.nes)
- ✅ Renderização de gráficos em tempo real (60 FPS)
- ✅ Áudio com 5 canais (Pulse, Triangle, Noise, DMC)
- ✅ Entrada via teclado
- ✅ Monitor de acessos à memória
- ✅ Interface gráfica com Swing

---

## 🚀 Como Usar

### Pré-requisitos

- Java 8 ou superior instalado
- Arquivo de ROM NES (.nes) em formato iNES

### Compilação

```bash
# Navegar até o diretório do projeto
cd "AnalisadorDeRom"

# Compilar todos os arquivos
javac -d . src/**/*.java
```

### Execução

```bash
# Executar com uma ROM
java AnalisadorRomNES "caminho/para/jogo.nes"

# Exemplo:
java AnalisadorRomNES "ROM/Super Mario Bros. (Europe)/Super Mario Bros. (Europe).nes"
```

### 🎮 Controles

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
$4000-$4017  │ APU e I/O (Controles)
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

### Ciclos por Frame

- **CPU**: ~29,780 ciclos
- **PPU**: ~89,340 ciclos (3× CPU)
- **APU**: ~29,780 ciclos

---

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
├── ROM/                          # ROMs de teste
├── DOCUMENTACAO.md               # Documentação completa
└── README.md                     # Este arquivo
```

---

## 🎮 Jogos Testados

| Jogo | Mapper | Status |
|------|--------|--------|
| Super Mario Bros | 0 (NROM) | ✅ Funcional |
| Donkey Kong | 0 (NROM) | ✅ Funcional |
| Pac-Man | 0 (NROM) | ✅ Funcional |

---

## 🐛 Limitações Conhecidas

- **Mappers**: Apenas NROM (Mapper 0) implementado
- **APU**: Alguns detalhes de timing simplificados
- **PPU**: Scrolling pode ter pequenos glitches em jogos complexos
- **Performance**: Requer CPU moderna para manter 60 FPS estável

---

## 🔮 Melhorias Futuras

- [ ] Implementar mais mappers (MMC1, MMC3, UxROM)
- [ ] Melhorar precisão da APU
- [ ] Implementar save states
- [ ] Adicionar debugger com disassembler
- [ ] Suporte a PAL (50 Hz)
- [ ] Configuração personalizável de controles

---

## 📚 Recursos e Referências

### Documentação Técnica
- [NESdev Wiki](https://www.nesdev.org/wiki/Nesdev_Wiki) - Documentação oficial
- [6502.org](http://www.6502.org/) - Referência do processador
- [iNES Format](https://www.nesdev.org/wiki/INES) - Formato de ROMs

### Ferramentas
- [FCEUX](http://fceux.com/) - Emulador com debugger
- [Mesen](https://www.mesen.ca/) - Emulador moderno

### Aprendizado
- [Easy 6502](https://skilldrick.github.io/easy6502/) - Tutorial Assembly
- [NES Emulator Book](https://bugzmanov.github.io/nes_ebook/) - Guia de desenvolvimento

---

## 📖 Documentação Completa

Para informações detalhadas sobre a implementação, arquitetura e funcionamento interno, consulte a [**Documentação Completa**](DOCUMENTACAO.md).

A documentação inclui:
- Explicação detalhada de cada componente
- Diagramas de fluxo de execução
- Especificações técnicas completas
- Exemplos de código comentados
- Conceitos de SO aplicados

---

## 📄 Licença

Este projeto é de cunho **educacional** e foi desenvolvido para fins acadêmicos.

**Nota Importante**: As ROMs de jogos NES são propriedade de seus respectivos donos. Este emulador não inclui ROMs comerciais. Use apenas ROMs que você possui legalmente ou ROMs de domínio público/homebrew.

---

## 🙏 Agradecimentos

- Professor da disciplina de Sistemas Operacionais - FURB
- Comunidade NESdev pela documentação técnica
- Desenvolvedores de emuladores open-source que serviram de referência

---

<div align="center">

**Desenvolvido com ❤️ para aprender Sistemas Operacionais**

FURB - Universidade Regional de Blumenau  
2025

</div>

