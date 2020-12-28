package mytown.util;

public class TownBlockKey {
    int dim, x, z;
    public TownBlockKey(int dim, int x, int z) {
        this.dim = dim;
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return (this.dim + 13121) * (this.x | 65536) * (this.z | 65536);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TownBlockKey))
            return false;

        TownBlockKey key = (TownBlockKey)o;

        return key.dim == dim && key.x == x && key.z == z;
    }
}
