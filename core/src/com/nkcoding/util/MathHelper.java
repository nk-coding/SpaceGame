package com.nkcoding.util;

import com.badlogic.gdx.math.Vector2;

public final class MathHelper {
    public static final Vector2 calcIntersection(Vector2 pos1, float dx1, float dy1, Vector2 pos2, float dx2, float dy2) {
        float a1 = dx1;
        float b1 = -dx2;
        float c1 = pos2.x - pos1.x;

        float a2 = dy1;
        float b2 = -dy2;
        float c2 = pos2.y - pos1.y;

        if (!(a1 == 0 && a2 == 0)) {
            if (a1 == 0) {
                // swap the two
                float a3 = a1;
                a1 = a2;
                a2 = a3;

                float b3 = b1;
                b1 = b2;
                b2 = b3;

                float c3 = c1;
                c1 = c2;
                c2 = c3;
            }
            //create the addition
            float fac = -a2 / a1;
            a1 *= fac;
            b1 *= fac;
            c1 *= fac;
        }

        //float a2b = a2 + a1;
        float b2b = b2 + b1;
        float c2b = c2 + c1;

        if (b2b == 0) return null;

        float z2 = c2b / b2b;

        return new Vector2(pos2.x + z2 * dx2, pos2.y + z2 * dy2);
    }
}
