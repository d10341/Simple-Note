package com.lx2td.simplenote.utils;

public class EqualityChecker {
    public EqualityChecker() {
    }

    public static synchronized boolean check(Object a, Object b) {
        boolean res = true;
        if (a != null) {
            res = res && a.equals(b);
        } else if (b != null) {
            res = res && b.equals(a);
        }

        return res;
    }

    public static synchronized boolean check(Object[] aArr, Object[] bArr) {
        boolean res = true;
        res = res && aArr.length == bArr.length;
        if (res) {
            for(int i = 0; i < aArr.length; ++i) {
                Object a = aArr[i];
                Object b = bArr[i];
                if (a != null) {
                    res = res && a.equals(b);
                } else if (b != null) {
                    res = res && b.equals(a);
                }

                if (!res) {
                    break;
                }
            }
        }

        return res;
    }
}
