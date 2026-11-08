package com.una.socialuna.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Utilidades de seguridad para las contraseñas.
 *
 * Cada contraseña se guarda como "salt:hash":
 *  - salt: 16 bytes aleatorios generados con SecureRandom (distinto por usuario).
 *  - hash: SHA-256 calculado sobre el salt y luego la contraseña.
 * Ambos valores se escriben en hexadecimal, separados por dos puntos, para que
 * el resultado se pueda guardar en el CSV sin escapes.
 *
 * El salt hace que dos usuarios con la misma contraseña tengan hashes distintos,
 * y evita que se puedan comparar hashes entre sí (tablas precalculadas).
 */
public final class HashUtil {

    private static final String ALGORITMO = "SHA-256";
    private static final int BYTES_SALT = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private HashUtil() {
    }

    /**
     * Genera un hash nuevo para una contraseña, con un salt aleatorio.
     * Recibe: la contraseña en texto plano (no vacía).
     * Retorna: el texto "salt:hash" listo para guardarse en usuarios.csv.
     *
     * @throws IllegalArgumentException si la contraseña es null o está vacía
     */
    public static String hashear(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] salt = new byte[BYTES_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(contrasena, salt);
        return HEX.formatHex(salt) + ":" + HEX.formatHex(hash);
    }

    /**
     * Comprueba si una contraseña corresponde al "salt:hash" guardado.
     * Recibe: la contraseña en texto plano y el valor guardado en el CSV.
     * Retorna: true si coinciden; false si no coinciden o el valor guardado
     *          tiene un formato inválido (así un CSV dañado no rompe el login).
     */
    public static boolean verificar(String contrasena, String saltHash) {
        if (contrasena == null || saltHash == null) {
            return false;
        }
        int separador = saltHash.indexOf(':');
        if (separador <= 0 || separador == saltHash.length() - 1) {
            return false;
        }
        try {
            byte[] salt = HEX.parseHex(saltHash.substring(0, separador));
            byte[] hashGuardado = HEX.parseHex(saltHash.substring(separador + 1));
            byte[] hashCalculado = calcular(contrasena, salt);
            // Comparación en tiempo constante para no filtrar información.
            return MessageDigest.isEqual(hashGuardado, hashCalculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcular(String contrasena, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO);
            digest.update(salt);
            return digest.digest(contrasena.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("El JDK no soporta " + ALGORITMO, e);
        }
    }
}
