import java.time.Year;
import java.util.Locale;
import java.util.Scanner;

public class Parcial2 {

    static final String[] BANCOS = {"Banco Provincia", "Banco Nación", "Banco Hipotecario"};
    static final String[] MODALIDADES = {"Anual", "Trimestral", "Mensual"};
    static final int[] PERIODOS = {1, 4, 12}; 
    static final double TASA_MAXIMA = 1000; 
    static final Locale AR = Locale.forLanguageTag("es-AR");
    static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("        ANÁLISIS DE INVERSIÓN EN PLAZOS FIJOS               ");
        System.out.println("=============================================================");

        // 1. Solicitar al usuario los valores históricos de los últimos 3 años para cada banco
        int anioActual = Year.now().getValue();
        int[] anios = {anioActual - 3, anioActual - 2, anioActual - 1};
        double[][] tasas = new double[BANCOS.length][anios.length];

        System.out.println("Paso 1: Ingrese las tasas anuales (TNA %) de los últimos 3 años:");
        for (int b = 0; b < BANCOS.length; b++) {
            System.out.println("\n--- " + BANCOS[b] + " ---");
            for (int a = 0; a < anios.length; a++) {
                tasas[b][a] = leerTasa("  Tasa del año " + anios[a] + " (%): ");
            }
        }

        // 2. Calcular el promedio de las tasas anuales de cada banco
        double[] promedios = new double[BANCOS.length];
        for (int b = 0; b < BANCOS.length; b++) {
            double suma = 0;
            for (double t : tasas[b]) {
                suma += t;
            }
            promedios[b] = suma / tasas[b].length;
        }

        // Mostrar explícitamente los promedios calculados
        System.out.println("\n=============================================================");
        System.out.println("Paso 2: PROMEDIO DE TASAS ANUALES CALCULADO (Últimos 3 años)");
        System.out.println("=============================================================");
        for (int b = 0; b < BANCOS.length; b++) {
            System.out.printf(AR, "  - %-20s: %.2f%%%n", BANCOS[b], promedios[b]);
        }

        // 3. Simulación de inversión con montos ingresados por el usuario
        // Permite evaluar uno o varios montos sucesivamente (ideal para demostración en video)
        System.out.println("\n=============================================================");
        System.out.println("Paso 3: SIMULACIÓN DE INVERSIÓN Y RENDIMIENTOS");
        System.out.println("=============================================================");

        boolean continuar;
        int simNumero = 1;
        do {
            if (simNumero > 1) {
                System.out.println("\n=============================================================");
                System.out.println("NUEVA EVALUACIÓN DE INVERSIÓN (#" + simNumero + ")");
                System.out.println("=============================================================");
            }

            double capital = leerMonto("Ingrese el monto que desea invertir ($): ");
            mostrarRendimientosYRecomendacion(capital, promedios);

            System.out.println();
            continuar = preguntarContinuar("¿Desea ingresar otro monto de inversión? (S/N): ");
            simNumero++;
        } while (continuar);

        System.out.println("\n=============================================================");
        System.out.println("        FIN DE LA SIMULACIÓN - MUCHAS GRACIAS          ");
        System.out.println("=============================================================");

        sc.close();
    }

    /**
     * Calcula y presenta en pantalla los rendimientos por modalidad y banco,
     * junto con la opción más rentable recomendada para el capital ingresado.
     *
     * @param capital Monto a invertir ingresado por el usuario.
     * @param promedios Vector con las tasas promedios de cada banco.
     */
    static void mostrarRendimientosYRecomendacion(double capital, double[] promedios) {
        // Evaluar rendimientos reinvirtiendo el capital ganado según cada modalidad
        double[][] rendimientos = new double[BANCOS.length][MODALIDADES.length];
        for (int b = 0; b < BANCOS.length; b++) {
            for (int m = 0; m < MODALIDADES.length; m++) {
                rendimientos[b][m] = calcularRendimiento(capital, promedios[b], PERIODOS[m]);
            }
        }

        // Mostrar en pantalla los rendimientos y montos acumulados por modalidad y banco
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("RENDIMIENTOS Y MONTOS TOTALES POR MODALIDAD");
        System.out.println("Capital evaluado: " + moneda(capital));
        System.out.println("-------------------------------------------------------------");
        for (int b = 0; b < BANCOS.length; b++) {
            System.out.printf(AR, "%n--- %s (TNA promedio: %.2f%%) ---%n", BANCOS[b], promedios[b]);
            for (int m = 0; m < MODALIDADES.length; m++) {
                double ganancia = rendimientos[b][m];
                double montoTotal = capital + ganancia;
                System.out.printf(AR, "  %-12s -> Rendimiento: %-14s | Monto final: %s%n",
                        MODALIDADES[m] + ":", moneda(ganancia), moneda(montoTotal));
            }
        }

        // Determinar la opción más rentable (buscando el rendimiento máximo)
        double maxRendimiento = rendimientos[0][0];
        for (int b = 0; b < BANCOS.length; b++) {
            for (int m = 0; m < MODALIDADES.length; m++) {
                if (rendimientos[b][m] > maxRendimiento) {
                    maxRendimiento = rendimientos[b][m];
                }
            }
        }

        // Mostrar recomendación (incluyendo empates si los hubiera)
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("RECOMENDACIÓN FINAL: OPCIÓN MÁS RENTABLE");
        System.out.println("-------------------------------------------------------------");
        System.out.println("Opción(es) recomendada(s) para invertir " + moneda(capital) + ":");
        for (int b = 0; b < BANCOS.length; b++) {
            for (int m = 0; m < MODALIDADES.length; m++) {
                // Margen de tolerancia para punto flotante (0.01 centavos)
                if (Math.abs(rendimientos[b][m] - maxRendimiento) < 0.01) {
                    System.out.printf(AR, "  * Banco: %-18s | Modalidad: %s%n", BANCOS[b], MODALIDADES[m]);
                }
            }
        }
        System.out.printf(AR, "%nRendimiento obtenido (ganancia): %s%n", moneda(maxRendimiento));
        System.out.printf(AR, "Monto final acumulado           : %s%n", moneda(capital + maxRendimiento));
        System.out.println("-------------------------------------------------------------");
    }

    /**
     * Calcula el rendimiento anual reinvirtiendo el capital ganado al finalizar cada periodo.
     * Simula iterativamente la capitalización de intereses período a período (consigna).
     *
     * @param capitalInicial Capital inicial a invertir.
     * @param tnaPorcentaje Tasa Nominal Anual en porcentaje (ej: 70 para 70%).
     * @param periodos Cantidad de períodos en el año (1 = anual, 4 = trimestral, 12 = mensual).
     * @return Ganancia neta generada (Monto final - Capital inicial).
     */
    static double calcularRendimiento(double capitalInicial, double tnaPorcentaje, int periodos) {
        double tasaPeriodo = (tnaPorcentaje / 100.0) / periodos;
        double saldoAcumulado = capitalInicial;

        // Bucle que simula la reinversión al finalizar cada periodo
        for (int i = 1; i <= periodos; i++) {
            double gananciaPeriodo = saldoAcumulado * tasaPeriodo;
            saldoAcumulado += gananciaPeriodo; // Reinversión del rendimiento obtenido
        }

        return saldoAcumulado - capitalInicial;
    }

    /**
     * Solicita y valida el ingreso de un monto monetario positivo a invertir.
     * Acepta números enteros (ej: 850000), decimales (ej: 850000.50 o 850000,50)
     * y con formato de miles (ej: 850.000 o 1.000.000).
     */
    static double leerMonto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = leerLinea().replace("$", "").trim();

            try {
                double valor;
                if (entrada.matches("\\d{1,3}(\\.\\d{3})+(,\\d+)?")) {
                    // Formato con punto de miles (ej: 850.000 o 1.000.000,50)
                    String limpia = entrada.replace(".", "").replace(',', '.');
                    valor = Double.parseDouble(limpia);
                } else if (entrada.matches("\\d{1,3}(,\\d{3})+(\\.\\d+)?")) {
                    // Formato con coma de miles (ej: 850,000 o 1,000,000.50)
                    String limpia = entrada.replace(",", "");
                    valor = Double.parseDouble(limpia);
                } else if (entrada.matches("\\d+([.,]\\d+)?")) {
                    // Formato estándar (ej: 850000 o 850000.50 o 850000,50)
                    valor = Double.parseDouble(entrada.replace(',', '.'));
                } else {
                    System.out.println("  Error: ingrese un monto positivo válido (ej: 850000 o 1000000).");
                    continue;
                }

                if (valor <= 0) {
                    System.out.println("  Error: el monto a invertir debe ser mayor a 0.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("  Error: no se pudo interpretar el número ingresado. Intente nuevamente.");
            }
        }
    }

    /**
     * Solicita y valida el ingreso de una tasa porcentual positiva.
     */
    static double leerTasa(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = leerLinea();
            if (!entrada.matches("\\d+([.,]\\d+)?")) {
                System.out.println("  Error: ingrese un número positivo válido (ej: 75 o 75.5).");
                continue;
            }
            double valor = Double.parseDouble(entrada.replace(',', '.'));
            if (valor <= 0 || valor > TASA_MAXIMA) {
                System.out.println("  Error: la tasa debe ser mayor a 0 y menor o igual a " + (int) TASA_MAXIMA + "%.");
                continue;
            }
            return valor;
        }
    }

    /**
     * Solicita confirmación al usuario para continuar con otra simulación (S/N).
     */
    static boolean preguntarContinuar(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String respuesta = leerLinea().trim().toLowerCase();
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                return true;
            }
            if (respuesta.equals("n") || respuesta.equals("no")) {
                return false;
            }
            System.out.println("  Error: ingrese 'S' para Sí o 'N' para No.");
        }
    }

    /**
     * Lee una línea de texto de forma segura desde la consola.
     */
    static String leerLinea() {
        if (!sc.hasNextLine()) {
            System.out.println("\nEntrada finalizada. Saliendo del programa.");
            System.exit(0);
        }
        return sc.nextLine().trim();
    }

    /**
     * Formatea un valor monetario con formato de moneda en pesos argentinos ($1.234,56).
     */
    static String moneda(double valor) {
        return String.format(AR, "$%,.2f", valor);
    }
}