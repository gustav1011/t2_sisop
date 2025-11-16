public class Utilities {

    public static void printProgram(Word[] program) {
        if (program == null) {
            System.out.println("null");
            return;
        }

        System.out.println("=== PROGRAM ===");
        for (int i = 0; i < program.length; i++) {
            System.out.println(i + ": " + program[i]);
        }
        System.out.println("================");
    }

    public static void printArray(int[] arr, String title) {
        System.out.println("=== " + title + " ===");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println("\n====================");
    }

    public static void printMatrix(int[][] matrix, String title) {
        System.out.println("=== " + title + " ===");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%4d ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("====================");
    }

    public static void fillMatrix(int[][] matrix, int value) {
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = value;
    }

    public static void fillArray(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++)
            arr[i] = value;
    }

    public static String pad(int num, int digits) {
        return String.format("%0" + digits + "d", num);
    }

    public static String padRight(String s, int n) {
        if (s == null)
            s = "";
        return String.format("%1$-" + n + "s", s);
    }

    public static int b(boolean v) {
        return v ? 1 : 0;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
