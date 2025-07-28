import org.json.JSONObject;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class PolynomialSolver {
    static final int PRIME = 2089;

    static int baseToInt(String str, int base) {
        int value = 0;
        for (char c : str.toCharArray()) {
            int digit;
            if (Character.isDigit(c)) digit = c - '0';
            else if (Character.isLetter(c)) digit = Character.toLowerCase(c) - 'a' + 10;
            else throw new IllegalArgumentException("Invalid digit in base string");

            if (digit >= base) throw new IllegalArgumentException("Digit exceeds base");

            value = (value * base + digit) % PRIME;
        }
        return value;
    }

    static int modInverse(int a) {
        int res = 1, b = PRIME - 2;
        a %= PRIME;
        while (b > 0) {
            if ((b & 1) != 0) res = (res * a) % PRIME;
            a = (a * a) % PRIME;
            b >>= 1;
        }
        return res;
    }

    static int interpolateAtZero(List<int[]> shares) {
        int secret = 0;
        int k = shares.size();

        for (int i = 0; i < k; i++) {
            int xi = shares.get(i)[0];
            int yi = shares.get(i)[1];
            int num = 1, den = 1;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                int xj = shares.get(j)[0];
                num = (num * ((-xj + PRIME) % PRIME)) % PRIME;
                den = (den * ((xi - xj + PRIME) % PRIME)) % PRIME;
            }

            int term = (((yi * num) % PRIME) * modInverse(den)) % PRIME;
            secret = (secret + term) % PRIME;
        }
        return secret;
    }

    static int evaluateAtX(List<int[]> shares, int x) {
        int result = 0;
        int k = shares.size();

        for (int i = 0; i < k; i++) {
            int xi = shares.get(i)[0];
            int yi = shares.get(i)[1];
            int num = 1, den = 1;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                int xj = shares.get(j)[0];
                num = (num * ((x - xj + PRIME) % PRIME)) % PRIME;
                den = (den * ((xi - xj + PRIME) % PRIME)) % PRIME;
            }

            int term = (((yi * num) % PRIME) * modInverse(den)) % PRIME;
            result = (result + term) % PRIME;
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            JSONObject json = new JSONObject(new FileReader("input.json"));
            int k = json.getJSONObject("keys").getInt("k");

            List<int[]> allShares = new ArrayList<>();

            for (String key : json.keySet()) {
                if (key.equals("keys")) continue;

                int x = Integer.parseInt(key);
                JSONObject yObj = json.getJSONObject(key);
                int base = Integer.parseInt(yObj.getString("base"));
                String valueStr = yObj.getString("value");

                try {
                    int y = baseToInt(valueStr, base);
                    allShares.add(new int[]{x, y});
                } catch (Exception e) {
                    System.out.println("Invalid secret: failed to parse or convert one of the keys");
                    return;
                }
            }

            if (allShares.size() < k) {
                System.out.println("Not enough shares to reconstruct the secret");
                return;
            }

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < allShares.size(); i++) indices.add(i);

            int finalSecret = -1;
            boolean found = false;

            outerLoop:
            for (List<Integer> comb : combinations(indices, k)) {
                List<int[]> selected = comb.stream().map(allShares::get).collect(Collectors.toList());
                int candidateSecret = interpolateAtZero(selected);

                boolean valid = true;
                for (int[] point : allShares) {
                    int x = point[0];
                    int actualY = point[1];
                    int expectedY = evaluateAtX(selected, x);
                    if (expectedY != actualY) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    finalSecret = candidateSecret;
                    found = true;
                    break outerLoop;
                }
            }

            if (found) {
                System.out.println("Secret key is: " + finalSecret);
            } else {
                System.out.println("Could not validate secret with any combination of shares");
            }

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<List<Integer>> combinations(List<Integer> input, int k) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(input, k, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<Integer> input, int k, int start, List<Integer> temp, List<List<Integer>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < input.size(); i++) {
            temp.add(input.get(i));
            backtrack(input, k, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }
}
