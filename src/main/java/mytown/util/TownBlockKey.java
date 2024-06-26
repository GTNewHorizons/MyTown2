package mytown.util;

public class TownBlockKey {

    private int dim, x, z;

    public TownBlockKey(int dim, int x, int z) {
        this.dim = dim;
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int result = dim;
        result = 1023 * result + x;
        result = 1023 * result + z;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TownBlockKey)) return false;

        TownBlockKey key = (TownBlockKey) o;

        return key.dim == dim && key.x == x && key.z == z;
    }
}
