import org.json.JSONObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PolynomialSolver {

    // Helper class to represent a point (x, y)
    static class Point {
        int x;
        BigInteger y;

        public Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Solves for the constant term 'c' of a polynomial using Lagrange Interpolation.
     * The constant term 'c' is f(0).
     *
     * @param points The list of (x, y) points.
     * @param k The minimum number of points required (degree + 1).
     * @return The constant term 'c' as a BigInteger.
     */
    public static BigInteger findConstantTerm(List<Point> points, int k) {
        BigInteger constantTerm = BigInteger.ZERO;

        // Use only the first k points for interpolation.
        // The problem states n >= k, so we can always pick k points.
        List<Point> kPoints = points.stream().limit(k).collect(Collectors.toList());

        for (int j = 0; j < k; j++) {
            Point currentPoint = kPoints.get(j);
            BigInteger y_j = currentPoint.y;
            int x_j = currentPoint.x;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int i = 0; i < k; i++) {
                if (i == j) {
                    continue;
                }
                Point otherPoint = kPoints.get(i);
                int x_i = otherPoint.x;

                // Numerator: Product of (0 - x_i) for i!= j
                numerator = numerator.multiply(BigInteger.valueOf(0 - x_i));

                // Denominator: Product of (x_j - x_i) for i!= j
                denominator = denominator.multiply(BigInteger.valueOf(x_j - x_i));
            }

            // Calculate the term for this point: y_j * (numerator / denominator)
            // Given the problem constraints (positive integer coefficients),
            // the division is expected to be exact.
            BigInteger term = y_j.multiply(numerator).divide(denominator);
            constantTerm = constantTerm.add(term);
        }

        return constantTerm;
    }

    /**
     * Processes a single test case from a JSON file.
     *
     * @param jsonFilePath The path to the JSON file.
     * @return The calculated secret (constant term 'c').
     * @throws Exception if there's an issue reading the file or parsing JSON.
     */
    public static BigInteger processTestCase(String jsonFilePath) throws Exception {
        JSONObject jsonObject = new JSONObject(new FileReader(jsonFilePath));

        JSONObject keys = jsonObject.getJSONObject("keys");
        // int n = keys.getInt("n"); // n is the total number of roots provided, not directly used in calculation
        int k = keys.getInt("k"); // k is the minimum number of roots required (degree + 1)

        List<Point> points = new ArrayList<>();

        // Iterate through the keys (which are x-values) in the JSON object
        // Skip the "keys" object as it contains metadata, not points
        for (String key : jsonObject.keySet()) {
            if (!key.equals("keys")) {
                int x = Integer.parseInt(key);
                JSONObject yData = jsonObject.getJSONObject(key);
                String baseStr = yData.getString("base");
                String valueStr = yData.getString("value");

                int base = Integer.parseInt(baseStr);
                // Decode Y value from its given base to a BigInteger
                BigInteger y = new BigInteger(valueStr, base); 

                points.add(new Point(x, y));
            }
        }

        // Sort points by x-value. While Lagrange interpolation doesn't strictly require sorted points,
        // sorting ensures a consistent selection of the "first k" points if n > k.
        points.sort((p1, p2) -> Integer.compare(p1.x, p2.x));

        return findConstantTerm(points, k);
    }

    public static void main(String args) {
        // Create dummy JSON files for demonstration purposes.
        // In a real submission, these files would be provided externally.
        createDummyJsonFiles();

        try {
            BigInteger secret1 = processTestCase("testcase1.json");
            System.out.println("Secret for Test Case 1: " + secret1);

            BigInteger secret2 = processTestCase("testcase2.json");
            System.out.println("Secret for Test Case 2: " + secret2);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create the JSON test case files.
     * This is included to make the code runnable out-of-the-box for testing.
     * In a production environment, these files would be pre-existing.
     */
    private static void createDummyJsonFiles() {
        String testCase1Content = "{\n" +
                "    \"keys\": {\n" +
                "        \"n\": 4,\n" +
                "        \"k\": 3\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"4\"\n" +
                "    },\n" +
                "    \"2\": {\n" +
                "        \"base\": \"2\",\n" +
                "        \"value\": \"111\"\n" +
                "    },\n" +
                "    \"3\": {\n" +
                "        \"base\": \"10\",\n" +
                "        \"value\": \"12\"\n" +
                "    },\n" +
                "    \"6\": {\n" +
                "        \"base\": \"4\",\n" +
                "        \"value\": \"213\"\n" +
                "    }\n" +
                "}";

        String testCase2Content = "{\n" +
                "\"keys\": {\n" +
                "    \"n\": 10,\n" +
                "    \"k\": 7\n" +
                "  },\n" +
                "  \"1\": {\n" +
                "    \"base\": \"6\",\n" +
                "    \"value\": \"13444211440455345511\"\n" +
                "  },\n" +
                "  \"2\": {\n" +
                "    \"base\": \"15\",\n" +
                "    \"value\": \"aed7015a346d63\"\n" +
                "  },\n" +
                "  \"3\": {\n" +
                "    \"base\": \"15\",\n" +
                "    \"value\": \"6aeeb69631c227c\"\n" +
                "  },\n" +
                "  \"4\": {\n" +
                "    \"base\": \"16\",\n" +
                "    \"value\": \"e1b5e05623d881f\"\n" +
                "  },\n" +
                "  \"5\": {\n" +
                "    \"base\": \"8\",\n" +
                "    \"value\": \"316034514573652620673\"\n" +
                "  },\n" +
                "  \"6\": {\n" +
                "    \"base\": \"3\",\n" +
                "    \"value\": \"2122212201122002221120200210011020220200\"\n" +
                "  },\n" +
                "  \"7\": {\n" +
                "    \"base\": \"3\",\n" +
                "    \"value\": \"20120221122211000100210021102001201112121\"\n" +
                "  },\n" +
                "  \"8\": {\n" +
                "    \"base\": \"6\",\n" +
                "    \"value\": \"20220554335330240002224253\"\n" +
                "  },\n" +
                "  \"9\": {\n" +
                "    \"base\": \"12\",\n" +
                "    \"value\": \"45153788322a1255483\"\n" +
                "  },\n" +
                "  \"10\": {\n" +
                "    \"base\": \"7\",\n" +
                "    \"value\": \"1101613130313526312514143\"\n" +
                "  }\n" +
                "}";

        try {
            FileWriter writer1 = new FileWriter("testcase1.json");
            writer1.write(testCase1Content);
            writer1.close();

            FileWriter writer2 = new FileWriter("testcase2.json");
            writer2.write(testCase2Content);
            writer2.close();
        } catch (IOException e) {
            System.err.println("Error creating dummy JSON files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
