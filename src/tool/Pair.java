package tool;

public class Pair<K, V> {
    private K key;
    private V value;

    // 构造函数
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    // 获取键的方法
    public K getKey() {
        return key;
    }

    // 设置键的方法
    public void setKey(K key) {
        this.key = key;
    }

    // 获取值的方法
    public V getValue() {
        return value;
    }

    // 设置值的方法
    public void setValue(V value) {
        this.value = value;
    }

    // 覆盖 toString 方法以提供字符串表示
    @Override
    public String toString() {
        return "Pair{" + "key=" + key + ", value=" + value + '}';
    }

}

