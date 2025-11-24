package cpu;

public interface Instruction {

    // cpu que referencia a classe cpu para ver as instruções
    void execute(Cpu cpu);

    // quantos ciclos a instrução consome
    int getCycles();

    // qual instrução é
    String getName();
}
