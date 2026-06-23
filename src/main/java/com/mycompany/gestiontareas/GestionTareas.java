package com.mycompany.gestiontareas;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Sistema basico de gestion de tareas para un equipo de trabajo.
 *
 * Estructuras de datos utilizadas:
 *  - ArrayList<Tarea>  -> almacena TODAS las tareas registradas (historico).
 *  - Queue<Tarea>      -> tareas pendientes, en el orden en que fueron ingresadas (FIFO).
 *  - Stack<Tarea>      -> tareas finalizadas recientemente (LIFO, la ultima en
 *                         finalizar es la primera que se muestra).
 */
public class GestionTareas {

    // ---------- Clase interna que representa una tarea ----------
    static class Tarea {
        private String nombre;
        private String responsable;
        private int prioridad;       // 1 (alta) - 5 (baja)
        private String estado;       // "PENDIENTE" o "FINALIZADA"
        private String fechaEntrega; // formato: dd/mm/aaaa

        public Tarea(String nombre, String responsable, int prioridad, String fechaEntrega) {
            this.nombre = nombre;
            this.responsable = responsable;
            this.prioridad = prioridad;
            this.fechaEntrega = fechaEntrega;
            this.estado = "PENDIENTE";
        }

        public String getNombre() { return nombre; }

        public void marcarFinalizada() { this.estado = "FINALIZADA"; }

        @Override
        public String toString() {
            return String.format("Tarea: %-20s | Responsable: %-15s | Prioridad: %d | Estado: %-10s | Entrega: %s",
                    nombre, responsable, prioridad, estado, fechaEntrega);
        }
    }

    // ---------- Estructuras de datos principales ----------
    private static ArrayList<Tarea> listaTareas   = new ArrayList<>();
    private static Queue<Tarea>     colaPendientes = new LinkedList<>();
    private static Stack<Tarea>     pilaFinalizadas = new Stack<>();
    private static Scanner sc = new Scanner(System.in);

    // =====================================================================
    //  MAIN
    // =====================================================================
    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcionMenu();
            switch (opcion) {
                case 1: registrarTarea();            break;
                case 2: listarTodasLasTareas();      break;
                case 3: marcarTareaFinalizada();     break;
                case 4: consultarTareasPendientes(); break;
                case 5: mostrarUltimasFinalizadas(); break;
                case 6: buscarTareaPorNombre();      break;
                case 7:
                    salir = true;
                    System.out.println("\nSaliendo del sistema. Hasta pronto.");
                    break;
                default:
                    System.out.println("\nOpcion invalida. Por favor seleccione una opcion del 1 al 7.");
            }
        }
        sc.close();
    }

    // =====================================================================
    //  MENU
    // =====================================================================
    private static void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE GESTION DE TAREAS =====");
        System.out.println("1. Registrar nueva tarea");
        System.out.println("2. Listar todas las tareas");
        System.out.println("3. Marcar tarea como finalizada");
        System.out.println("4. Consultar tareas pendientes");
        System.out.println("5. Mostrar ultimas tareas finalizadas");
        System.out.println("6. Buscar tarea por nombre");
        System.out.println("7. Salir");
        System.out.print("Seleccione una opcion: ");
    }

    private static int leerOpcionMenu() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("\nEntrada invalida, debe ingresar un numero.");
            return -1;
        }
    }

    // =====================================================================
    //  METODOS AUXILIARES DE VALIDACION (con bucle do-while)
    // =====================================================================

    /**
     * Solicita un texto no vacio, repitiendo la pregunta hasta obtener uno valido.
     */
    private static String leerTextoNoVacio(String etiqueta) {
        String valor = "";
        do {
            System.out.print(etiqueta);
            try {
                valor = sc.nextLine().trim();
                if (valor.isEmpty()) {
                    System.out.println("  [!] Este campo no puede estar vacio. Intente de nuevo.");
                }
            } catch (Exception e) {
                System.out.println("  [!] Error al leer la entrada. Intente de nuevo.");
                valor = "";
            }
        } while (valor.isEmpty());
        return valor;
    }

    /**
     * Solicita una prioridad entre 1 y 5, repitiendo hasta obtener un valor valido.
     */
    private static int leerPrioridad() {
        int prioridad = 0;
        boolean valido = false;
        do {
            System.out.print("Prioridad (1 = alta, 5 = baja): ");
            try {
                prioridad = Integer.parseInt(sc.nextLine().trim());
                if (prioridad >= 1 && prioridad <= 5) {
                    valido = true;
                } else {
                    System.out.println("  [!] La prioridad debe ser un numero entre 1 y 5. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Debe ingresar un numero entero entre 1 y 5. Intente de nuevo.");
            }
        } while (!valido);
        return prioridad;
    }

    /**
     * Solicita una fecha en formato dd/mm/aaaa, validando que:
     *  - Tenga exactamente el formato dd/mm/aaaa.
     *  - Sea una fecha real (LocalDate lo verifica, por ejemplo 30/02 no existe).
     *  - No sea una fecha pasada (debe ser hoy o posterior al dia de hoy).
     * Repite la pregunta hasta obtener una fecha valida.
     */
    private static String leerFecha() {
        String fecha = "";
        boolean valido = false;
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoy = LocalDate.now();

        do {
            System.out.print("Fecha de entrega (dd/mm/aaaa): ");
            try {
                fecha = sc.nextLine().trim();

                // Verificar longitud y posicion de las barras antes de parsear
                if (fecha.length() != 10 || fecha.charAt(2) != '/' || fecha.charAt(5) != '/') {
                    System.out.println("  [!] Formato incorrecto. Use dd/mm/aaaa (ejemplo: 30/06/2026). Intente de nuevo.");
                    continue;
                }

                // Parsear la fecha — LocalDate valida automaticamente que el dia/mes existan
                LocalDate fechaIngresada = LocalDate.parse(fecha, formato);

                // Verificar que no sea una fecha pasada
                if (fechaIngresada.isBefore(hoy)) {
                    System.out.println("  [!] La fecha " + fecha + " ya paso. "
                            + "Debe ingresar una fecha igual o posterior a hoy ("
                            + hoy.format(formato) + "). Intente de nuevo.");
                } else {
                    valido = true;
                }

            } catch (DateTimeParseException e) {
                System.out.println("  [!] Fecha invalida (verifique el dia y mes). Use dd/mm/aaaa. Intente de nuevo.");
            }
        } while (!valido);
        return fecha;
    }

    // =====================================================================
    //  OPERACIONES DEL MENU
    // =====================================================================

    // ---------- 1. Registrar tarea (INSERTAR en lista y cola) ----------
    private static void registrarTarea() {
        System.out.println("\n-- Registrar nueva tarea --");

        // Solicitar nombre y verificar que no exista ya una tarea con ese nombre
        String nombre = "";
        boolean nombreValido = false;
        do {
            nombre = leerTextoNoVacio("Nombre de la tarea: ");
            boolean duplicado = false;
            for (Tarea t : listaTareas) {
                if (t.getNombre().equalsIgnoreCase(nombre)) {
                    duplicado = true;
                    break;
                }
            }
            if (duplicado) {
                System.out.println("  [!] Ya existe una tarea con el nombre \"" + nombre
                        + "\". Por favor ingrese un nombre diferente.");
            } else {
                nombreValido = true;
            }
        } while (!nombreValido);

        String responsable = leerTextoNoVacio("Responsable: ");
        int    prioridad   = leerPrioridad();
        String fecha       = leerFecha();

        Tarea nueva = new Tarea(nombre, responsable, prioridad, fecha);
        listaTareas.add(nueva);      // INSERT en ArrayList
        colaPendientes.offer(nueva); // ENQUEUE en cola FIFO

        System.out.println("\nTarea registrada exitosamente.");
    }

    // ---------- 2. Listar todas las tareas (RECORRER ArrayList) ----------
    private static void listarTodasLasTareas() {
        System.out.println("\n-- Listado completo de tareas --");
        if (listaTareas.isEmpty()) {
            System.out.println("No hay tareas registradas.");
            return;
        }
        for (Tarea t : listaTareas) {
            System.out.println(t);
        }
    }

    // ---------- 3. Marcar finalizada (ELIMINAR de cola + PUSH en pila) ----------
    private static void marcarTareaFinalizada() {
        System.out.println("\n-- Marcar tarea como finalizada --");
        if (colaPendientes.isEmpty()) {
            System.out.println("No hay tareas pendientes.");
            return;
        }
        String nombre = leerTextoNoVacio("Nombre de la tarea a finalizar: ");

        Tarea encontrada = null;
        for (Tarea t : colaPendientes) {
            if (t.getNombre().equalsIgnoreCase(nombre)) {
                encontrada = t;
                break;
            }
        }

        if (encontrada == null) {
            System.out.println("\nNo se encontro una tarea pendiente con ese nombre.");
            return;
        }

        colaPendientes.remove(encontrada); // DEQUEUE (eliminar de la cola)
        encontrada.marcarFinalizada();
        pilaFinalizadas.push(encontrada);  // PUSH (apilar como finalizada)

        System.out.println("\nTarea \"" + encontrada.getNombre() + "\" marcada como finalizada.");
    }

    // ---------- 4. Consultar pendientes (RECORRER cola, peek primer elemento) ----------
    private static void consultarTareasPendientes() {
        System.out.println("\n-- Tareas pendientes (orden de llegada / FIFO) --");
        if (colaPendientes.isEmpty()) {
            System.out.println("No hay tareas pendientes.");
            return;
        }
        System.out.println(">> Primer elemento de la cola (proxima a atender): "
                + colaPendientes.peek().getNombre());

        // Identificar el ultimo elemento de la cola
        Tarea ultimo = null;
        for (Tarea t : colaPendientes) { ultimo = t; }
        System.out.println(">> Ultimo elemento de la cola: " + (ultimo != null ? ultimo.getNombre() : "N/A"));
        System.out.println();

        int i = 1;
        for (Tarea t : colaPendientes) {
            System.out.println(i + ". " + t);
            i++;
        }
    }

    // ---------- 5. Ultimas finalizadas (RECORRER pila LIFO) ----------
    private static void mostrarUltimasFinalizadas() {
        System.out.println("\n-- Ultimas tareas finalizadas (la mas reciente primero / LIFO) --");
        if (pilaFinalizadas.isEmpty()) {
            System.out.println("Aun no hay tareas finalizadas.");
            return;
        }
        for (int i = pilaFinalizadas.size() - 1; i >= 0; i--) {
            System.out.println(pilaFinalizadas.get(i));
        }
    }

    // ---------- 6. Buscar por nombre (LOCALIZAR en ArrayList) ----------
    private static void buscarTareaPorNombre() {
        System.out.println("\n-- Buscar tarea por nombre --");
        String nombre = leerTextoNoVacio("Nombre de la tarea a buscar: ");

        boolean encontrada = false;
        for (Tarea t : listaTareas) {
            if (t.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("\nTarea encontrada:");
                System.out.println(t);
                encontrada = true;
                break;
            }
        }
        if (!encontrada) {
            System.out.println("\nNo se encontro ninguna tarea con ese nombre.");
        }
    }
}