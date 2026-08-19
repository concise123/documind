package my.documind.document.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class VectorUtils {
    public static String toVectorString(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(vector[i]);
        }
        return builder.append("]").toString();
    }
}
