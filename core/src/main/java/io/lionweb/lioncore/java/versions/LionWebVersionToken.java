package io.lionweb.lioncore.java.versions;

public interface LionWebVersionToken{
    LionWebVersion getVersion();

    class V2023_1 implements LionWebVersionToken{
        public String toString() { return "2023.1"; }
        private V2023_1() {

        }
        private static V2023_1 instance = new V2023_1();
        public static V2023_1 getInstance() {
            return instance;
        }

        @Override
        public LionWebVersion getVersion() {
            return LionWebVersion.v2023_1;
        }
    }
    class V2024_1 implements LionWebVersionToken{
        public String toString() { return "2024.1"; }
        private V2024_1() {

        }
        private static V2024_1 instance = new V2024_1();
        public static V2024_1 getInstance() {
            return instance;
        }

        @Override
        public LionWebVersion getVersion() {
            return LionWebVersion.v2024_1;
        }
    }

    static LionWebVersionToken getCurrentVersion() {
        return V2024_1.getInstance();
    }
}


