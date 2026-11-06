package com.una.socialuna;

/**
 * Punto de entrada del programa (el que se ejecuta desde IntelliJ y el que
 * queda como Main-Class del .jar).
 *
 * Existe porque, al empaquetar JavaFX en un .jar ejecutable, la clase con
 * main() NO puede extender Application: de lo contrario Java exige los
 * módulos de JavaFX y el .jar falla con "JavaFX runtime components are
 * missing". Esta clase solo delega en MainApp.
 */
public class Launcher {

    /**
     * Recibe: los argumentos de la línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
